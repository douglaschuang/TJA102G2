package com.babymate.member.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.babymate.cart.service.CartService;
import com.babymate.member.model.MemberVO;
import com.babymate.member.service.MemberService;
import com.babymate.util.EncodingUtil;
import com.babymate.util.MailService;
import com.babymate.util.SimpleCaptchaGenerator;
import com.babymate.valid.UpdateGroup;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotEmpty;

@Controller
@Validated
@RequestMapping("/member")
public class MemberController {

	private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

	@Autowired
	MemberService memberSvc;

	@Autowired
	CartService cartService;

	@Autowired
	private MailService mailSvc;

	/**
	 * 處理「查詢單一會員」的請求。
	 *
	 * @param memberId 前端表單傳入的會員編號（必填）
	 * @param model 用來傳遞資料到前端頁面
	 * @return 對應的 Thymeleaf 頁面名稱
	 */
	@PostMapping("getOne_For_Display")
	public String getOne_For_Display(@NotEmpty(message = "會員ID: 請勿空白") @RequestParam("memberId") String memberId,
			ModelMap model) {

		MemberVO memberVO = memberSvc.getOneMember(Integer.valueOf(memberId));

		List<MemberVO> list = memberSvc.getAll();
		model.addAttribute("memberListData", list);

		// 若查無會員資料，顯示錯誤訊息並返回列表頁
		if (memberVO == null) {
			model.addAttribute("errorMessage", "查無資料");
			return "admin/member/memberlist";
		}

		model.addAttribute("memberVO", memberVO);
		model.addAttribute("pageTitle", "會員管理｜檢視會員");
		// 導向會員檢視畫面
		return "admin/member/memberview";
	}

	/**
	 * <p>處理會員資料更新的控制器方法。</p>
	 *
	 * @param memberVO 表單綁定的會員物件
	 * @param result 驗證結果容器
	 * @param model 用於傳遞資料至前端
	 * @param parts 上傳的頭像圖片（可為空）
	 * @param session 用於更新目前登入會員資訊
	 * @param redirectAttrs 用於轉址時帶入提示訊息
	 * @return 轉址至「我的帳號」頁面
	 * @throws IOException 若圖片讀取或轉換過程發生錯誤
	 */
	@PostMapping("update")
	public String update(@Validated(UpdateGroup.class) @ModelAttribute("MemberVO") MemberVO memberVO,
			BindingResult result, ModelMap model, @RequestParam(name = "pic", required = false) MultipartFile[] parts,
			HttpSession session, RedirectAttributes redirectAttrs) throws IOException {

		logger.info("開始處理會員資料更新，會員ID: {}", memberVO.getMemberId());
		
		// 移除圖片欄位的驗證錯誤（選填）
		result = removeFieldError(memberVO, result, "pic");

		// 處理會員頭像圖片上傳邏輯
		if (parts == null || parts.length == 0 || parts[0].isEmpty()) {
			// 沒有上傳新圖片，保留原圖片
			byte[] pic = memberSvc.getOneMember(memberVO.getMemberId()).getProfilePicture();
			memberVO.setProfilePicture(pic);
			logger.debug("未上傳新頭像，保留原有圖片。");
		} else {
			// 上傳新圖片
			for (MultipartFile multipartFile : parts) {
				byte[] pic = multipartFile.getBytes();
				memberVO.setProfilePicture(pic);
				logger.info("已接收到新上傳的會員頭像。");
			}
		}

		MemberVO originalMember = memberSvc.getOneMember(memberVO.getMemberId());
		
		// 處理密碼欄位（若空白則保留原密碼）
		if (memberVO.getPassword() == null || memberVO.getPassword().isEmpty()) {
			memberVO.setPassword(originalMember.getPassword());
			logger.debug("密碼欄位為空，使用原密碼。");
		} else {
			memberVO.setPassword(EncodingUtil.hashMD5(memberVO.getPassword()));
			logger.info("會員密碼已更新（以 MD5 雜湊處理）。");
		}

		// 若表單驗證有錯誤，回傳原頁面
		if (result.hasErrors()) {
			logger.warn("會員更新驗證失敗：{}", result.getAllErrors());
			model.addAttribute("MemberVO", memberVO);
			return "my-account";
		}
		
		// 更新會員資料至資料庫
		memberSvc.updateMember(memberVO);
		logger.info("會員資料更新成功，會員ID: {}", memberVO.getMemberId());
		
		// 重新查詢最新資料，更新至 model 與 session
		memberVO = memberSvc.getOneMember(Integer.valueOf(memberVO.getMemberId()));
		model.addAttribute("success", "- (修改成功)");
		model.addAttribute("memberVO", memberVO);
		session.setAttribute("member", memberVO);
		
		// 設定提示訊息並轉址
		redirectAttrs.addFlashAttribute("successMessage", "會員資料已更新！");
		logger.info("會員更新完成，導向 /my-account 頁面。");
		return "redirect:/my-account";
	}

	/**
	 * 更新會員基本資料的控制器方法。
	 *
	 * @param memberVO  綁定的會員資料物件，由表單提交
	 * @param result Spring 驗證結果物件，儲存欄位驗證錯誤
	 * @param model 用於傳遞資料至前端頁面
	 * @param parts 上傳的會員頭像檔案陣列，可為空
	 * @param session 目前的使用者 Session，用於更新登入會員資訊
	 * @param redirectAttrs 用於在重新導向後傳遞一次性提示訊息
	 * @return 若成功更新，導向「/my-account#account-info」頁面；若失敗則重新導向同頁顯示錯誤
	 * @throws IOException 若在圖片讀取過程中發生 I/O 錯誤
	 */
	@PostMapping("updateBasicInfo")
	public String updateBasicInfo(@Validated(UpdateGroup.class) @ModelAttribute("MemberVO") MemberVO memberVO,
			BindingResult result, ModelMap model, @RequestParam(name = "pic", required = false) MultipartFile[] parts,
			HttpSession session, RedirectAttributes redirectAttrs) throws IOException {

		logger.info("updateBasicInfo - Member basic info start checking.");
		
		if (memberVO == null) {
			logger.info("updateBasicInfo - Cannot get MemberVO.");
			model.addAttribute("errorMessage", "查無資料");
		} else {
			LocalDate birthday = memberVO.getBirthday();
			logger.info("updateBasicInfo - 生日預期修改為{}", birthday);

			if (birthday != null && birthday.isAfter(LocalDate.now())) {
				// 生日在未來日期
				logger.info("updateBasicInfo - 當前日期為{}, 生日不可為未來日期", LocalDate.now());
				redirectAttrs.addFlashAttribute("errorMessage", "生日必須小於當前日期");
				result.rejectValue("birthday", "birthday.invalid", "生日必須小於當前日期");
			}
		}

		// 移除圖片欄位的驗證錯誤
		result = removeFieldError(memberVO, result, "pic");

		if (parts == null || parts.length == 0 || parts[0].isEmpty()) {
			// 若使用者未上傳新圖片，維持原本圖片
			byte[] pic = memberSvc.getOneMember(memberVO.getMemberId()).getProfilePicture();
			memberVO.setProfilePicture(pic);
			logger.debug("updateBasicInfo - 未上傳新圖片，使用原有頭像。");
		} else {
			for (MultipartFile multipartFile : parts) {
				byte[] pic = multipartFile.getBytes();
				memberVO.setProfilePicture(pic);
				logger.info("updateBasicInfo - 成功接收到新圖片上傳，檔案大小：{} bytes", pic.length);
			}
		}

		// 載入原始會員資料
		MemberVO originalMember = memberSvc.getOneMember(memberVO.getMemberId());
		
		if (originalMember == null) {
	        logger.error("updateBasicInfo - 查無原始會員資料，memberId: {}", memberVO.getMemberId());
	        redirectAttrs.addFlashAttribute("errorMessage", "查無會員資料");
	        return "redirect:/my-account#account-info";
	    }

		// 若驗證失敗則返回原頁面
		if (result.hasErrors()) {
			logger.warn("updateBasicInfo - 驗證失敗，返回修改頁面。錯誤數：{}", result.getErrorCount());
			model.addAttribute("MemberVO", memberVO);
			return "redirect:/my-account#account-info";
		}
		
		// 載入其他欄位資訊
		memberVO.setAccount(originalMember.getAccount());
		memberVO.setPassword(originalMember.getPassword());
		memberVO.setEmailAuthToken(originalMember.getEmailAuthToken());
		memberVO.setEmailVerified(originalMember.getEmailVerified());
		memberVO.setLastLoginTime(originalMember.getLastLoginTime());
		memberVO.setPwdResetExpire(originalMember.getPwdResetExpire());
		memberVO.setPwdResetToken(originalMember.getPwdResetToken());
		memberVO.setRegisterDate(originalMember.getRegisterDate());
		
		// 更新時間
		memberVO.setUpdateDate(LocalDateTime.now());
		memberSvc.updateMember(memberVO);
		logger.info("updateBasicInfo - 會員資料更新完成，memberId: {}", memberVO.getMemberId());

		// 更新Model與Session
		memberVO = memberSvc.getOneMember(Integer.valueOf(memberVO.getMemberId()));

		session.setAttribute("member", memberVO);
		redirectAttrs.addFlashAttribute("successMessage", "會員資料已更新！");
		logger.info("updateBasicInfo - 更新成功訊息已加入 RedirectAttributes。");

		return "redirect:/my-account#account-info";
	}

	/**
	 * 更新會員密碼的控制器方法。
	 *
	 * @param memberVO  綁定的會員資料物件，由表單提交
	 * @param result Spring 驗證結果物件，儲存欄位驗證錯誤
	 * @param model 用於傳遞資料至前端頁面
	 * @param newPassword 新密碼
	 * @param confirmPassword 密碼確認
	 * @param session 目前的使用者 Session，用於更新登入會員資訊
	 * @param redirectAttrs 用於在重新導向後傳遞一次性提示訊息
	 * @return 若成功更新，導向「/my-account#account-info」頁面；若失敗則重新導向同頁顯示錯誤
	 */
	@PostMapping("updatePassword")
	public String updatePassword(@Validated(UpdateGroup.class) @ModelAttribute("MemberVO") MemberVO memberVO,
			BindingResult result, ModelMap model,
			@RequestParam(name = "newPassword", required = true) String newPassword,
			@RequestParam(name = "confirmPassword", required = true) String confirmPassword, HttpSession session,
			RedirectAttributes redirectAttrs) {

		// step 1: 驗證表單格式
		logger.debug("updatePassword - 開始驗證表單格式");
		result = removeFieldError(memberVO, result, "password");
		
		if (result.hasErrors()) {
			logger.warn("updatePassword - 表單驗證失敗，錯誤數：{}", result.getErrorCount());
			model.addAttribute("MemberVO", memberVO);
			return "my-account";
		}

		// step 2: 新舊密碼檢查
		if (!newPassword.equals(confirmPassword)) {
			redirectAttrs.addFlashAttribute("errorMessage", "新密碼與確認新密碼不相同, 請確認.");
			redirectAttrs.addFlashAttribute("newPassword", newPassword);
			redirectAttrs.addFlashAttribute("confirmPassword", confirmPassword);
			return "redirect:/my-account#password-info";
		}

		// step 3: 取得會員並驗證原密碼
		MemberVO originalMember = memberSvc.getOneMember(memberVO.getMemberId());
		// 密碼驗證失敗
		if (!originalMember.getPassword().equals(EncodingUtil.hashMD5(memberVO.getPassword()))) {
			redirectAttrs.addFlashAttribute("errorMessage", "原密碼輸入錯誤, 請確認.");
			redirectAttrs.addFlashAttribute("newPassword", newPassword);
			redirectAttrs.addFlashAttribute("confirmPassword", confirmPassword);
			return "redirect:/my-account#password-info";
		}

		// step 4: 更新密碼/
		// Hash加密編碼(新密碼)
		originalMember.setPassword(EncodingUtil.hashMD5(newPassword));
		// 更新時間
		originalMember.setUpdateDate(LocalDateTime.now());
		memberSvc.updateMember(originalMember);
		logger.info("updatePassword - 會員密碼變更完成，memberId: {}", memberVO.getMemberId());

		// step 5: 更新session與回應訊息
		model.addAttribute("success", "- (修改成功)");
		memberVO = memberSvc.getOneMember(Integer.valueOf(originalMember.getMemberId()));
		model.addAttribute("memberVO", originalMember);

		session.setAttribute("member", originalMember);
		redirectAttrs.addFlashAttribute("successMessage", "密碼已更新！");
		logger.info("updateBasicInfo - 密碼已更新並加入 RedirectAttributes。");

		return "redirect:/my-account#password-info";
	}

	/**
	 * 停用會員的控制器方法。
	 *
	 * @param memberId 要停用的會員Id
	 * @param model 用於傳遞資料至前端頁面
	 * @return 返回「admin/member/memberlist」頁面
	 */
	@PostMapping("memberSuspend")
	public String memberSuspend(@RequestParam("memberId") String memberId, ModelMap model) {
		MemberVO memberVO = memberSvc.getOneMember(Integer.valueOf(memberId));

		if (memberVO != null) {
			// 切換狀態
			byte currentStatus = memberVO.getAccountStatus();
			logger.info("Member {} current status={}", memberId, String.valueOf(currentStatus));
			if (currentStatus == 1) {
				memberVO.setAccountStatus((byte) 2); // 停用
				logger.info("Member {} status changed to 2 (suspend).", memberId);
			} else if (currentStatus == 2) {
				memberVO.setAccountStatus((byte) 1); // 啟用
				logger.info("Member {} status changed to 1 (active).", memberId);
			}
		}

		memberSvc.updateMember(memberVO);
		model.addAttribute("successMessage", "ID: " + memberId + " 狀態變更成功");
		model.addAttribute("memberVO", memberVO);

		// 返回會員清單, 重新抓取所有會員資料
		List<MemberVO> memberList = memberSvc.getAll();
		model.addAttribute("memberListData", memberList);
		model.addAttribute("pageTitle", "會員管理｜列表");

		return "admin/member/memberlist";
	}

	/**
	 * 會員忘記密碼的控制器方法。
	 *
	 * @param account 忘記密碼需要重新設定的會員account
	 * @param model 用於傳遞資料至前端頁面
	 * @return 返回「frontend/shop-customer-login」頁面
	 */
	@PostMapping("memberForgotPwd")
	public String memberForgotPwd(@RequestParam("account") String account, ModelMap model) {
		MemberVO memberVO = memberSvc.getOneMember(account);
		logger.info("Member forgot password processing for {}", account);

		if (memberVO == null) {
			model.addAttribute("errorMessage", "帳號不存在，請重新輸入");
			model.addAttribute("errorSource", "resetpwd");
			return "frontend/shop-customer-login";
		}

		logger.info("Member forgot password processing - account current status= {}", memberVO.getAccountStatus());
		if (memberVO.getAccountStatus() == 2) {
			model.addAttribute("errorMessage", "帳號停用，請聯繫管理員。");
			model.addAttribute("errorSource", "resetpwd");
			return "frontend/shop-customer-login";
		}

		// 正常帳號 → 導向重設密碼流程
		String newPwd = SimpleCaptchaGenerator.generateCaptcha(6); // 產生新密碼
		memberVO.setPassword(EncodingUtil.hashMD5(newPwd)); // 將密碼Hash編碼後寫入memberVO
		memberVO.setPwdResetToken(newPwd);
		memberVO.setUpdateDate(LocalDateTime.now());

		memberSvc.updateMember(memberVO);

		mailSvc.sendMail(memberVO.getAccount(), "BabyMate - 密碼變更通知",
				getHtmlMailContent("密碼變更通知", "請以此密碼返回BabyMate登入後至會員資料變更密碼。", newPwd), true);
		logger.info("(Member) Already sent password changed e-mail to Member {}", account);

		model.addAttribute("logoutMessage", "帳號: " + memberVO.getAccount() + " 密碼重設成功, 請檢查信箱.");
		model.addAttribute("logoutSource", "resetpwd");

		return "frontend/shop-customer-login";
	}

//	/**
//	 * 會員重設密碼的控制器方法。
//	 *
//	 * @param memberId 忘記密碼需要重新設定的會員Id
//	 * @param model 用於傳遞資料至前端頁面
//	 * @return 返回「frontend/shop-customer-login」頁面
//	 */
//	@PostMapping("memberResetPwd4Member")
//	public String memberResetPwd4Member(@RequestParam("memberId") String memberId, ModelMap model) {
//		MemberVO memberVO = memberSvc.getOneMember(Integer.valueOf(memberId));
//		String newPwd = SimpleCaptchaGenerator.generateCaptcha(6); // 產生新密碼
//		memberVO.setPassword(EncodingUtil.hashMD5(newPwd)); // 將密碼Hash編碼後寫入memberVO
//		memberVO.setPwdResetToken(newPwd);
//		memberVO.setUpdateDate(LocalDateTime.now());
//
//		memberSvc.updateMember(memberVO);
//
//		mailSvc.sendMail(memberVO.getAccount(), "BabyMate - 密碼變更通知",
//				getHtmlMailContent("密碼變更通知", "請以此密碼返回BabyMate登入後至會員資料變更密碼。", newPwd), true);
//		logger.info("memberResetPwd4Member - account {} password already reset.", memberVO.getAccount());
//
//		model.addAttribute("logoutSource", "帳號: " + memberVO.getAccount() + " 密碼重設成功, 請檢查信箱.");
//		model.addAttribute("logoutSource", "resetpwd");
//		model.addAttribute("memberVO", memberVO);
//
//		return "frontend/shop-customer-login";
//	}

	/**
	 * 會員重設密碼的控制器方法(後台使用)。
	 *
	 * @param memberId 忘記密碼需要重新設定的會員Id
	 * @param model 用於傳遞資料至前端頁面
	 * @return 返回「admin/member/memberlist」頁面
	 */
	@PostMapping("memberResetPwd")
	public String memberResetPwd(@RequestParam("memberId") String memberId, ModelMap model) {
		
		MemberVO memberVO = memberSvc.getOneMember(Integer.valueOf(memberId));
		String newPwd = SimpleCaptchaGenerator.generateCaptcha(6); // 產生新密碼
		memberVO.setPassword(EncodingUtil.hashMD5(newPwd)); // 將密碼Hash編碼後寫入memberVO
		memberVO.setPwdResetToken(newPwd);
		memberVO.setUpdateDate(LocalDateTime.now());

		memberSvc.updateMember(memberVO);
		logger.info("Admin forced to reset member password processing for {}", memberVO.getAccount());

		mailSvc.sendMail(memberVO.getAccount(), "BabyMate - 密碼變更通知",
				getHtmlMailContent("密碼變更通知", "請以此密碼返回BabyMate登入後至會員資料變更密碼。", newPwd), true);
		logger.info("(Admin) Already sent password changed e-mail to Member {}", memberVO.getAccount());

		model.addAttribute("successMessage", "ID: " + memberId + " 密碼重設成功");
		model.addAttribute("memberVO", memberVO);

		// 返回會員清單, 重新抓取所有會員資料
		List<MemberVO> memberList = memberSvc.getAll();
		model.addAttribute("memberListData", memberList);
		model.addAttribute("pageTitle", "會員管理｜列表");

		return "admin/member/memberlist";
	}

	/**
	 * 會員註冊的控制器方法。
	 *
	 * @param account 欲註冊的會員帳號(email)，由表單提交
	 * @param password 註冊會員輸入的初始密碼
	 * @param captcha 註冊會員輸入的驗證碼(收email取得)
	 * @param session 目前的使用者 Session，用於更新登入會員資訊
	 * @param redirectAttributes 用於在重新導向後傳遞一次性提示訊息
	 * @param model 用於傳遞資料至前端頁面
	 * @return 若驗證成功，導向「/shop/login」頁面；若失敗則重新導向同頁顯示錯誤
	 */
	@PostMapping("registerCheck")
	public String registerCheck(@RequestParam String account, @RequestParam String password,
			@RequestParam String captcha, HttpSession session, RedirectAttributes redirectAttributes, ModelMap model) {
		
		// 根據輸入的account查找會員
		MemberVO member = memberSvc.getOneMember(account);

		// 會員不存在, 可以註冊
		if (member == null) {
			logger.info("registerCheck - 帳號 {} 不存在, 進入新會員註冊流程", account);
			// 新會員註冊流程
			String newCaptcha = SimpleCaptchaGenerator.generateCaptcha(6);
			// 初始化MemberVO並將會員資料儲存於資料庫
			memberSvc.initMember(account, newCaptcha);

			mailSvc.sendMail(account, "請驗證您的信箱 - BabyMate",
					getHtmlMailContent("驗證您的身份", "請輸入此驗證碼以完成您的登入或操作。", newCaptcha), true);
			logger.info("registerCheck - Already sent auth-code to register e-mail {}", account);

			redirectAttributes.addFlashAttribute("errorMessage", "驗證碼已發出, 請輸入驗證碼");
			redirectAttributes.addFlashAttribute("errorSource", "register");
			redirectAttributes.addFlashAttribute("account", account); // 回填帳號
			redirectAttributes.addFlashAttribute("captcha", captcha); // 回填驗證碼
			return "redirect:/shop/login";
		}

		// 會員已存在
		if ((member.getAccountStatus() == 1) || (member.getAccountStatus() == 2)) {
			logger.info("registerCheck - 帳號 {} 已存在且已完成註冊, 不可重複註冊", account);
			// 帳號已啟用
			redirectAttributes.addFlashAttribute("errorMessage", "會員帳號: 已存在, 請重新輸入");
			redirectAttributes.addFlashAttribute("errorSource", "register");
			redirectAttributes.addFlashAttribute("account", account); // 回填帳號
			redirectAttributes.addFlashAttribute("captcha", captcha); // 回填驗證碼
			return "redirect:/shop/login";
		}

		// 前端未輸入驗證碼
		if (captcha.isEmpty()) {
			logger.info("registerCheck - 未輸入驗證碼");
			redirectAttributes.addFlashAttribute("errorMessage", "請輸入驗證碼");
			redirectAttributes.addFlashAttribute("errorSource", "register");
			redirectAttributes.addFlashAttribute("account", account); // 回填帳號
			redirectAttributes.addFlashAttribute("captcha", captcha); // 回填驗證碼
			return "redirect:/shop/login";
		}

		// 檢查輸入驗證碼和系統驗證碼是否相符
		if (!captcha.equals(member.getEmailAuthToken())) {
			logger.info("registerCheck - 驗證碼不相符");
			redirectAttributes.addFlashAttribute("errorMessage", "驗證碼不相符, 請確認");
			redirectAttributes.addFlashAttribute("errorSource", "register");
			redirectAttributes.addFlashAttribute("account", account); // 回填帳號
			redirectAttributes.addFlashAttribute("captcha", captcha); // 回填驗證碼
			return "redirect:/shop/login";
		}

		// 驗證成功，啟用帳號 + 儲存密碼
		member.setPassword(EncodingUtil.hashMD5(password));
		member.setAccountStatus((byte) 1); // account status狀態變更為啟用
		member.setEmailVerified((byte) 1); // email驗證狀態變更為1
		memberSvc.updateMember(member);

		logger.info("registerCheck - {} 註冊成功", account);
		redirectAttributes.addFlashAttribute("logoutMessage", "註冊成功, 請至登入頁面登入");
		redirectAttributes.addFlashAttribute("logoutSource", "register");
		return "redirect:/shop/login";
	}

	/**
	 * 處理會員重新發送驗證碼的請求。
	 *
	 * @param request  前端傳入的 JSON 物件，需包含 key 為 "account" 的 E-Mail 帳號
	 * @return ResponseEntity<Map<String, Object>>  
	 *         回傳 JSON 格式結果：
	 *         <ul>
	 *           <li>success：是否成功寄出驗證信</li>
	 *           <li>message：操作結果訊息</li>
	 *         </ul>
	 */
	@PostMapping("resendAuthCode")
	public ResponseEntity<Map<String, Object>> memberResendAuthCode(@RequestBody Map<String, String> request) {
		
		// 取得前端傳入的帳號 (E-Mail)
		String account = request.get("account");
		Map<String, Object> response = new HashMap<>();
		logger.info("(resendAuthCode - Get acccount email: {}", account);

		// step 1: 檢查輸入參數
		if (account.isEmpty()) {
			response.put("success", false);
			response.put("message", "未取得正確的E-Mail, 請重新輸入.");
			return ResponseEntity.ok(response);
		}
		
		// step 2: 查詢資料庫是否已有該會員
		MemberVO memberVO = memberSvc.getOneMember(account);
		String authCode = "";

		if (memberVO != null) {
			// 會員存在: 判斷帳號狀態
			Byte accountStatus = memberVO.getAccountStatus();
			if (accountStatus == 0) {
				// 帳號尚未啟用: 取出原本驗證碼重新寄送
				logger.info("(resendAuthCode - account status = 0, get current authCode.");
				authCode = memberVO.getEmailAuthToken();
			} else {
				// 帳號已啟用: 不允許重新寄送驗證信
				logger.info("(resendAuthCode - account status = {}, member should be already finish the registeration.",
						accountStatus);
				response.put("success", false);
				response.put("message", account + "已經完成註冊驗證, 請前往登入.");
				return ResponseEntity.ok(response);
			}
		} else {
			// step 3: 帳號不存在: 產生新驗證碼 + 初始化會員
			authCode = SimpleCaptchaGenerator.generateCaptcha(6);
			memberVO = memberSvc.initMember(account, authCode);
		}

		// step 4: 寄出驗證信
		mailSvc.sendMail(memberVO.getAccount(), "請驗證您的信箱 - BabyMate",
				getHtmlMailContent("驗證您的身份", "請輸入此驗證碼以完成您的登入或操作。", authCode), true);
		logger.info("(resendAuthCode - authcode: {} already sent to {}", authCode, memberVO.getAccount());

		// step 5: 回傳成功結果
		response.put("success", true);
		response.put("message", "驗證碼已寄出，請檢查您的信箱.");
		return ResponseEntity.ok(response);
	}

	/**
	 * 處理會員登入的請求。
	 *
	 * @param accountLogin 使用者登入帳號（E-Mail）
	 * @param password 使用者登入密碼（原文）
	 * @param session 用於存取/設定會員登入狀態的 HttpSession
	 * @param redirectAttributes 用於攜帶錯誤訊息的重導屬性
	 * @return 若登入成功則導向會員中心頁面 `/my-account`，否則回到登入頁 `/shop/login`
	 */
	@PostMapping("loginCheck")
	public String loginCheck(@RequestParam String accountLogin, @RequestParam String password, HttpSession session,
			RedirectAttributes redirectAttributes) {
		
		// step 1: 將使用者輸入密碼進行 MD5 雜湊比對
		String hashedPwd = EncodingUtil.hashMD5(password);
		logger.info("loginCheck - account: {}", accountLogin);

		// step 2: 根據帳號查詢會員資料
		Optional<MemberVO> member = Optional.ofNullable(memberSvc.login(accountLogin));

		// step 3: 檢查會員是否存在
		if (member.isPresent()) {
			logger.info("loginCheck - account {} exists and get account info.", accountLogin);
			MemberVO loginMember = member.get();

			// 狀態為未通過驗證 (account status = 0)
			if (loginMember.getAccountStatus() == 0) {
				redirectAttributes.addFlashAttribute("errorMessage", "帳號未完成註冊, 請返回註冊輸入驗證碼完成確認.");
				redirectAttributes.addFlashAttribute("errorSource", "login");
				redirectAttributes.addFlashAttribute("accountLogin", accountLogin); // 回填帳號
				return "redirect:/shop/login";
			}

			// 狀態為停用 (account status = 2)
			if (loginMember.getAccountStatus() == 2) {
				redirectAttributes.addFlashAttribute("errorMessage", "帳號已停用, 請洽客服或網站管理員.");
				redirectAttributes.addFlashAttribute("errorSource", "login");
				redirectAttributes.addFlashAttribute("accountLogin", accountLogin); // 回填帳號
				return "redirect:/shop/login";
			}

			// step 4: 密碼比對
			if (!hashedPwd.equals(loginMember.getPassword())) {
				// 密碼不相等
				redirectAttributes.addFlashAttribute("errorMessage", "密碼錯誤, 請重新輸入.");
				redirectAttributes.addFlashAttribute("errorSource", "login");
				redirectAttributes.addFlashAttribute("accountLogin", accountLogin); // 回填帳號
				return "redirect:/shop/login";
			}

			// step 5: 驗證通過, 設定 session
			session.setAttribute("member", loginMember);
			logger.info("loginCheck - get login memberId: {}", loginMember.getMemberId());

			// step 6: 合併未登入時的購物車內容
			cartService.loginMergeCart(session.getId(), loginMember.getMemberId());
			
			// step 7: 更新最後登入時間
			loginMember.setLastLoginTime(LocalDateTime.now());
			memberSvc.updateMember(loginMember);

			logger.info("Login success, cart merged for memberId= {}", loginMember.getMemberId());

			session.setAttribute("member", member.get());
			return "redirect:/my-account";
		} else {
			// step 9: 若會員不存在, 回登入頁提示需註冊
			logger.info("loginCheck - account {} not found.", accountLogin);
			redirectAttributes.addFlashAttribute("errorMessage", "帳號不存在, 請先註冊.");
			redirectAttributes.addFlashAttribute("errorSource", "login");
			redirectAttributes.addFlashAttribute("accountLogin", accountLogin); // 回填帳號
			return "redirect:/shop/login";
		}
	}

	/**
	 * 處理會員登出請求。
	 *
	 * @param session 目前使用者的 HTTP Session，用於儲存登入資訊。
	 * @param redirectAttributes 用於在重導（redirect）後攜帶提示訊息的物件。
	 * @return 重新導向至登入頁面的字串路徑。
	 */
	@GetMapping("/logout")
	public String logout(HttpSession session, RedirectAttributes redirectAttributes) {

		if (session != null) {
			session.removeAttribute("member");
			session.invalidate(); // 讓 session 失效（登出）
		}

		redirectAttributes.addFlashAttribute("logoutMessage", "您已成功登出");
		redirectAttributes.addFlashAttribute("logoutSource", "login");
		return "redirect:/shop/login"; // 返回登入頁
	}

	/**
	 * 寄送HTML內容的EMail。
	 *
	 * @param mainSubject 信件主旨。
	 * @param secondSubject 信件次要內容。
	 * @param authcode 驗證碼或重設密碼。
	 * @return 回覆HTML信件內容。
	 */
	private String getHtmlMailContent(String mainSubject, String secondSubject, String authcode) {

		StringBuilder str = new StringBuilder();

		str.append("<!DOCTYPE html> " + "<html lang=\"zh-TW\"> " + "<head> " + "<meta charset=\"UTF-8\"> "
				+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"> "
				+ "<title>驗證您的身份</title> " + "</head> "
				+ "<body style=\"font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0;\"> "
				+ "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"background-color: #f4f4f4;\">"
				+ "<tr> " + "<td align=\"center\" style=\"padding: 20px 0;\"> "
				+ "<table width=\"600\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);\">"
				+ "<tr> " + "<td align=\"center\" style=\"padding: 20px 0;\"> "
				+ "<div style=\"font-size: 36px; font-weight: bold; color: brown; padding: 10px 0; letter-spacing: 2px; font-family: 'Arial Rounded MT Bold', Arial, sans-serif;\"> "
				+ "BABYMATE" + "</div> " + "</td> " + "</tr> " + "<tr> "
				+ "<td style=\"padding: 40px; text-align: center;\"> "
				+ "<h1 style=\"color: #333333; font-size: 24px; font-weight: bold; margin: 0 0 10px 0;\">" + mainSubject
				+ "</h1> " + "<p style=\"color: #666666; font-size: 16px; margin: 0 0 30px 0;\">" + secondSubject
				+ "</p> "
				+ "<div style=\"background-color: #f0f0f0; border: 1px solid #dddddd; padding: 20px; border-radius: 6px; display: inline-block;\"> "
				+ "<h2 style=\"color: #333333; font-size: 36px; font-weight: bold; margin: 0; letter-spacing: 5px;\">");

		str.append(authcode);

		str.append("</h2> " + "</div> " + "</td> " + "</tr> " + "<tr> "
				+ "<td style=\"padding: 40px; background-color: #f8f8f8; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px;\"> "
				+ "<p style=\"color: #333333; font-size: 14px; line-height: 1.5; margin: 0 0 10px 0;\"> "
				+ "如果您需要額外協助，請至BabyMate官網聯絡客服。 " + "</p> "
				+ "<p style=\"color: #666666; font-size: 14px; line-height: 1.5; margin: 0;\"> "
				+ "請注意，我們絕不會透過電子郵件向您索取密碼、信用卡或銀行帳號等個人資訊。如果您收到可疑信件，請不要點擊任何連結，並立即回報。" + "</p> " + "</td> " + "</tr> "
				+ "</table> " + "</td> " + "</tr> " + "</table> " + "</body> " + "</html>");

		return str.toString();
	}

	/**
	 * 移除指定欄位的驗證錯誤。
	 *
	 * @param memberVO 表單綁定的物件，用於建立新的 BindingResult。
	 * @param result 原始的 BindingResult，包含所有欄位的驗證結果。
	 * @param removedFieldname 要移除驗證錯誤的欄位名稱。
	 * @return 回傳一個新的 BindingResult，已移除指定欄位的錯誤訊息。
	 */
	public BindingResult removeFieldError(MemberVO memberVO, BindingResult result, String removedFieldname) {
		
		// 過濾掉指定欄位的錯誤，只保留其他欄位的錯誤
		List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
				.filter(fieldname -> !fieldname.getField().equals(removedFieldname)).collect(Collectors.toList());
		
		// 建立一個新的 BindingResult 物件，並重新加入保留的錯誤
		result = new BeanPropertyBindingResult(memberVO, "memberVO");
		for (FieldError fieldError : errorsListToKeep) {
			result.addError(fieldError);
		}
		return result;
	}

	/**
	 * 處理資料驗證過程中發生的例外。
	 *
	 * @param req 發生例外時的 HTTP 請求物件。
	 * @param e 驗證失敗所拋出的 ConstraintViolationException。
	 * @param model Spring MVC 的 Model 物件，用於傳遞資料到 View。
	 * @return 回傳帶有錯誤訊息的 {@link ModelAndView}，顯示在員工列表頁面上。
	 */
	@ExceptionHandler(value = { ConstraintViolationException.class })
	public ModelAndView handleError(HttpServletRequest req, ConstraintViolationException e, Model model) {
		// 取得所有驗證錯誤
		Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
		StringBuilder strBuilder = new StringBuilder();
		
		// 將所有錯誤訊息組合成 HTML 顯示格式
		for (ConstraintViolation<?> violation : violations) {
			strBuilder.append(violation.getMessage() + "<br>");
		}
		
		// 將會員列表資料重新載入，避免 View 資料遺失
		List<MemberVO> list = memberSvc.getAll();
		model.addAttribute("memberListData", list);
		
		// 組合錯誤訊息
		String message = strBuilder.toString();
		return new ModelAndView("admin/member/select_page", "errorMessage", "請修正以下錯誤:<br>" + message);
	}

}