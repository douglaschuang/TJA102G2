package com.babymate.member.controller;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.babymate.cart.service.CartService;
import com.babymate.member.model.MemberVO;
import com.babymate.member.service.MemberService;
import com.babymate.staff.model.StaffVO;
import com.babymate.staff.service.StaffService;
import com.babymate.util.EncodingUtil;
import com.babymate.util.MailService;
import com.babymate.util.SimpleCaptchaGenerator;
import com.babymate.valid.UpdateGroup;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@Validated
@RequestMapping("/member")
public class MemberController {
	
	@Autowired
	MemberService memberSvc; 
	
	@Autowired
    CartService cartService;

	/*
	 * This method will be called on select_page.html form submission, handling POST
	 * request It also validates the user input
	 */
	@PostMapping("getOne_For_Display")
	public String getOne_For_Display(
		/***************************1.接收請求參數 - 輸入格式的錯誤處理*************************/
		@NotEmpty(message="會員ID: 請勿空白")
		@RequestParam("memberId") String memberId,
		ModelMap model) {
		
		/***************************2.開始查詢資料*********************************************/
//		EmpService empSvc = new EmpService();
		MemberVO memberVO = memberSvc.getOneMember(Integer.valueOf(memberId));
		
		List<MemberVO> list = memberSvc.getAll();
		model.addAttribute("memberListData", list);     // for select_page.html 第97 109行用
	
		if (memberVO == null) {
			model.addAttribute("errorMessage", "查無資料");
			return "admin/member/memberlist";
		}
		
		/***************************3.查詢完成,準備轉交(Send the Success view)*****************/
		model.addAttribute("memberVO", memberVO); 
        model.addAttribute("pageTitle", "會員管理｜檢視會員");
		return "admin/member/memberview";   // 查詢完成後轉交listOneEmp.html
	}
	
	@PostMapping("update")
	public String update(@Validated(UpdateGroup.class) @ModelAttribute("MemberVO") MemberVO memberVO, BindingResult result, ModelMap model,
			@RequestParam(name = "pic", required = false) MultipartFile[] parts, HttpSession session, RedirectAttributes redirectAttrs) throws IOException {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 去除BindingResult中pic欄位的FieldError紀錄 --> 見第172行
		result = removeFieldError(memberVO, result, "pic");

		if (parts == null || parts.length == 0 || parts[0].isEmpty()) { // 使用者未選擇要上傳的新圖片時
			// EmpService empSvc = new EmpService();
			byte[] pic = memberSvc.getOneMember(memberVO.getMemberId()).getProfilePicture();
			memberVO.setProfilePicture(pic);
		} else {
			for (MultipartFile multipartFile : parts) {
				byte[] pic = multipartFile.getBytes();
				memberVO.setProfilePicture(pic);
			}
		}
		
		// 因為input type=password會帶出時會自動改為null, 所以若未重新輸入, 則維持原密碼
		MemberVO originalMember = memberSvc.getOneMember(memberVO.getMemberId());
//		// 若密碼欄位空白，保留原始密碼
	    if (memberVO.getPassword() == null || memberVO.getPassword().isEmpty()) {
	        memberVO.setPassword(originalMember.getPassword());
//	        System.out.println("staffVO="+staffVO.getPassword()+" original="+originalStaff.getPassword());
	    } else {
	    	// Hash加密編碼
	    	memberVO.setPassword(EncodingUtil.hashMD5(memberVO.getPassword()));
	    }
		
		if (result.hasErrors()) {
			model.addAttribute("MemberVO", memberVO);
			return "my-account";
		}
		/*************************** 2.開始修改資料 *****************************************/
		// EmpService empSvc = new EmpService();
		memberSvc.updateMember(memberVO);

		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/
		model.addAttribute("success", "- (修改成功)");
		memberVO = memberSvc.getOneMember(Integer.valueOf(memberVO.getMemberId()));
		model.addAttribute("memberVO", memberVO);
		
		session.setAttribute("member", memberVO);
		redirectAttrs.addFlashAttribute("successMessage", "會員資料已更新！");
		
		return "redirect:/my-account";
	}
	
	@PostMapping("updateBasicInfo")
	public String updateBasicInfo(@Validated(UpdateGroup.class) @ModelAttribute("MemberVO") MemberVO memberVO, BindingResult result, ModelMap model,
			@RequestParam(name = "pic", required = false) MultipartFile[] parts, HttpSession session, RedirectAttributes redirectAttrs) throws IOException {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 去除BindingResult中pic欄位的FieldError紀錄 --> 見第172行
		result = removeFieldError(memberVO, result, "pic");

		if (parts == null || parts.length == 0 || parts[0].isEmpty()) { // 使用者未選擇要上傳的新圖片時
			// EmpService empSvc = new EmpService();
			byte[] pic = memberSvc.getOneMember(memberVO.getMemberId()).getProfilePicture();
			memberVO.setProfilePicture(pic);
		} else {
			for (MultipartFile multipartFile : parts) {
				byte[] pic = multipartFile.getBytes();
				memberVO.setProfilePicture(pic);
			}
		}
		
		// 因為input type=password會帶出時會自動改為null, 所以若未重新輸入, 則維持原密碼
		MemberVO originalMember = memberSvc.getOneMember(memberVO.getMemberId());
//		// 若密碼欄位空白，保留原始密碼
//	    if (memberVO.getPassword() == null || memberVO.getPassword().isEmpty()) {
//	        memberVO.setPassword(originalMember.getPassword());
//	    } else {
//	    	// Hash加密編碼
//	    	memberVO.setPassword(EncodingUtil.hashMD5(memberVO.getPassword()));
//	    }
		
		if (result.hasErrors()) {
			model.addAttribute("MemberVO", memberVO);
			return "my-account#account-info";
		}
		/*************************** 2.開始修改資料 *****************************************/
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

		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/
		model.addAttribute("success", "- (修改成功)");
		memberVO = memberSvc.getOneMember(Integer.valueOf(memberVO.getMemberId()));
		model.addAttribute("memberVO", memberVO);
		
		session.setAttribute("member", memberVO);
		redirectAttrs.addFlashAttribute("successMessage", "會員資料已更新！");
		
		return "redirect:/my-account#account-info";
	}

	@PostMapping("updatePassword")
	public String updatePassword(@Validated(UpdateGroup.class) @ModelAttribute("MemberVO") MemberVO memberVO, BindingResult result, ModelMap model,
			@RequestParam(name = "newPassword", required = true) String newPassword,
	        @RequestParam(name = "confirmPassword", required = true) String confirmPassword,
	        HttpSession session, RedirectAttributes redirectAttrs) throws IOException {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 去除BindingResult中password欄位的FieldError紀錄
		result = removeFieldError(memberVO, result, "password");

		// 新密碼和確認密碼不相同
		if (!newPassword.equals(confirmPassword)) {
			redirectAttrs.addFlashAttribute("errorMessage", "新密碼與確認新密碼不相同, 請確認.");
			redirectAttrs.addFlashAttribute("newPassword", newPassword);
			redirectAttrs.addFlashAttribute("confirmPassword", confirmPassword);
			return "redirect:/my-account#password-info";
		}
		
		// 取得會員
		MemberVO originalMember = memberSvc.getOneMember(memberVO.getMemberId());
		// 密碼驗證失敗
		if (!originalMember.getPassword().equals(EncodingUtil.hashMD5(memberVO.getPassword()))) {
			redirectAttrs.addFlashAttribute("errorMessage", "原密碼輸入錯誤, 請確認.");
			redirectAttrs.addFlashAttribute("newPassword", newPassword);
			redirectAttrs.addFlashAttribute("confirmPassword", confirmPassword);
			return "redirect:/my-account#password-info";
		} 
		
		if (result.hasErrors()) {
			model.addAttribute("MemberVO", memberVO);
			return "my-account";
		}
		/*************************** 2.開始修改資料 *****************************************/
     	// Hash加密編碼(新密碼)
		originalMember.setPassword(EncodingUtil.hashMD5(newPassword));
		// 更新時間
		originalMember.setUpdateDate(LocalDateTime.now());
		memberSvc.updateMember(originalMember);

		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/
		model.addAttribute("success", "- (修改成功)");
		memberVO = memberSvc.getOneMember(Integer.valueOf(originalMember.getMemberId()));
		model.addAttribute("memberVO", originalMember);
		
		session.setAttribute("member", originalMember);
		redirectAttrs.addFlashAttribute("successMessage", "密碼已更新！");
		
		return "redirect:/my-account#password-info";
	}
	
	@PostMapping("memberSuspend")
	public String memberSuspend(@RequestParam("memberId") String memberId, ModelMap model)
	{
		MemberVO memberVO = memberSvc.getOneMember(Integer.valueOf(memberId));

	    if (memberVO != null) {
	        // 切換狀態
		  byte currentStatus = memberVO.getAccountStatus();
          if (currentStatus == 1) {
            memberVO.setAccountStatus((byte) 2); // 停用
          } else if (currentStatus == 2) {
            memberVO.setAccountStatus((byte) 1); // 啟用
          }
	    }

		memberSvc.updateMember(memberVO);
		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("successMessage", "ID: "+memberId+" 狀態變更成功");
		model.addAttribute("memberVO", memberVO);
//		return "back-end/staff/listAllStaff"; // 修改成功後轉交listAllStaff.html
//		return "redirect:/backend/member/listAllMember";
		
		// 返回會員清單, 重新抓取所有會員資料
		List<MemberVO> memberList = memberSvc.getAll();
		model.addAttribute("memberListData", memberList);
		model.addAttribute("pageTitle", "會員管理｜列表");
		
		return "admin/member/memberlist";
//		return "redirect:/admin/member/listAllMember";
	}
	
	@PostMapping("memberResetPwd")
	public String memberResetPwd(@RequestParam("memberId") String memberId, ModelMap model)
	{
		MemberVO memberVO = memberSvc.getOneMember(Integer.valueOf(memberId));
		String newPwd = SimpleCaptchaGenerator.generateCaptcha(6); // 產生新密碼
		memberVO.setPassword(EncodingUtil.hashMD5(newPwd)); // 將密碼Hash編碼後寫入memberVO
		memberVO.setPwdResetToken(newPwd);
		memberVO.setUpdateDate(LocalDateTime.now());
		
		memberSvc.updateMember(memberVO);
		
		String messageText = "Hello! " + " 您的密碼已更新為: " + newPwd + "\n" + " 並返回BabyMate登入後至會員資料變更密碼.";
		MailService mailSvc = new MailService();
		mailSvc.sendMail(memberVO.getAccount(), "BabyMate - 密碼變更通知", messageText);
		
		model.addAttribute("successMessage", "ID: "+memberId+" 密碼重設成功");
		model.addAttribute("memberVO", memberVO);
		
		// 返回會員清單, 重新抓取所有會員資料
		List<MemberVO> memberList = memberSvc.getAll();
		model.addAttribute("memberListData", memberList);
		model.addAttribute("pageTitle", "會員管理｜列表");
		
		return "admin/member/memberlist";
//		return "redirect:/admin/member/listAllMember";
	}
	
	@PostMapping("registerCheck")
	public String registerCheck(@RequestParam String account,
	                           @RequestParam String password,
	                           @RequestParam String captcha,
	                           HttpSession session,
	                           RedirectAttributes redirectAttributes, ModelMap model) {
		// 檢查account是否重複
		MemberVO memberTemp = memberSvc.getOneMember(account);
		if ((memberTemp != null) && (memberTemp.getAccountStatus() != 0)) {
//			model.addAttribute("errorMessage", "會員帳號: 已存在, 請重新輸入");
			redirectAttributes.addFlashAttribute("errorMessage", "會員帳號: 已存在, 請重新輸入");
			redirectAttributes.addFlashAttribute("errorSource", "register");
			redirectAttributes.addFlashAttribute("account", account);
		    redirectAttributes.addFlashAttribute("captcha", captcha);
			return "redirect:/shop/login";
		};
		
		// 帳號不重複, 發出驗證信件, 並新增會員後 (儲存本次密碼, 驗證碼, 驗證碼有效期限), 轉回View輸入驗證碼
		if (memberTemp == null) { // 新會員
			String newCaptcha = SimpleCaptchaGenerator.generateCaptcha(6);
			memberSvc.initMember(account, newCaptcha);
			
			String messageText = "Hello! " + " 請謹記此密碼: " + newCaptcha + "\n" + " 並返回BabyMate註冊頁面輸入";
			MailService mailSvc = new MailService();
			mailSvc.sendMail(account, "請驗證您的信箱 - BabyMate", messageText);
//			model.addAttribute("errorMessage", "驗證碼已發出, 請輸入驗證碼點選驗證完成");
			redirectAttributes.addFlashAttribute("errorMessage", "驗證碼已發出, 請輸入驗證碼");
			redirectAttributes.addFlashAttribute("errorSource", "register");
			redirectAttributes.addFlashAttribute("account", account);
		    redirectAttributes.addFlashAttribute("captcha", captcha);
			return "redirect:/shop/login";
		} else { 
			// 會員已存在
			// 未輸入驗證碼
			if (captcha.isEmpty()) {
				redirectAttributes.addFlashAttribute("errorMessage", "請輸入驗證碼");
				redirectAttributes.addFlashAttribute("errorSource", "register");
				redirectAttributes.addFlashAttribute("account", account);
				return "redirect:/shop/login";
			} else {
				// 有輸入captcha進行驗證
				// 取得會員存於系統的驗證碼進行比對
				if (!(memberTemp.getEmailAuthToken().equals(captcha))) {
//					model.addAttribute("errorMessage", "驗證碼不相符, 請確認");
					redirectAttributes.addFlashAttribute("errorMessage", "驗證碼不相符, 請確認");
					redirectAttributes.addFlashAttribute("errorSource", "register");
					redirectAttributes.addFlashAttribute("account", account);
				    redirectAttributes.addFlashAttribute("captcha", captcha);
					return "redirect:/shop/login";
				}
			}
		}
		 
		// 驗證碼通過, 儲存本次輸入密碼
	    String hashedPwd = EncodingUtil.hashMD5(password);
	    memberTemp.setPassword(hashedPwd);
	    // 驗證碼通過, 會員狀態更新為啟用(1)
	    memberTemp.setAccountStatus((byte) 1); // 啟用 (通過驗證)
	    memberSvc.updateMember(memberTemp);
	    
//	    model.addAttribute("success", "- (註冊成功)");
//		model.addAttribute("memberVO", memberTemp);
		
		redirectAttributes.addFlashAttribute("logoutMessage", "註冊成功, 請至登入頁面登入");
		redirectAttributes.addFlashAttribute("logoutSource", "register");
    
		// 回登入頁面, 引導用戶登入
	    return "redirect:/shop/login";
	}
	
	@PostMapping("resendAuthCode")
	public ResponseEntity<Map<String, Object>> memberResendAuthCode(@RequestBody Map<String, String> request)
	{
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		String account = request.get("account");
		Map<String, Object> response = new HashMap<>();
		System.out.println("account:"+account);
		
		if (account.isEmpty()) {
		  response.put("success", false);
          response.put("message", "未取得正確的E-Mail, 請重新輸入.");
//          return ResponseEntity.badRequest().body(response);
          return ResponseEntity.ok(response);
//		  model.addAttribute("errorMessage", "未取得正確的E-Mail");
//		  return "redirect:/shop/login";
		}
		/*************************** 2.開始查詢資料 *****************************************/
		MemberVO memberVO = memberSvc.getOneMember(account);
		String authCode = "";
		
		if (memberVO != null) {
			
			Byte accountStatus = memberVO.getAccountStatus();
			if ( accountStatus == 0) {
				System.out.println("accountStatus == 0");
				authCode = memberVO.getEmailAuthToken();
			} else {
				System.out.println("accountStatus != 0");
				response.put("success", false);
		        response.put("message", account+"已經完成註冊驗證, 請前往登入.");
		        return ResponseEntity.ok(response);
//		        return ResponseEntity.badRequest().body(response);
//				model.addAttribute("errorMessage", "會員已經完成註冊驗證, 請登入.");
//				return "redirect:/shop/login";
			}
		} else {
			authCode = SimpleCaptchaGenerator.generateCaptcha(6);
			memberVO = memberSvc.initMember(account, authCode);
		}
		
		String messageText = "Hello! " + " 請謹記此密碼: " + authCode + "\n" + " 並返回BabyMate註冊頁面輸入";
		MailService mailSvc = new MailService();
		mailSvc.sendMail(memberVO.getAccount(), "請驗證您的信箱 - BabyMate", messageText);
//		mailSvc.sendMail("douglas.chuang@gmail.com", "請驗證您的信箱 - BabyMate", messageText);
		
		response.put("success", true);
        response.put("message", "驗證碼已寄出，請檢查您的信箱.");
        return ResponseEntity.ok(response);
        
//		model.addAttribute("errorMessage", "驗證碼已發出, 請輸入驗證碼點選驗證完成");
		
		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
//		model.addAttribute("success", "- (修改成功)");
//		model.addAttribute("memberVO", memberVO);
//		return "redirect:/shop/login";
	}
	
	@PostMapping("loginCheck")
	public String loginCheck(@RequestParam String account,
	                           @RequestParam String password,
	                           HttpSession session,
	                           RedirectAttributes redirectAttributes) {
	    String hashedPwd = EncodingUtil.hashMD5(password);
	    System.out.println(account +"," + password+ "," + hashedPwd);

	    Optional<MemberVO> member = Optional.ofNullable(memberSvc.login(account, hashedPwd));
	    if (member.isPresent()) {
    	
	    	MemberVO loginMember = member.get();
	        
	        // 設定 session
	        session.setAttribute("member", loginMember);
	        System.out.println("memberId: "+loginMember.getMemberId());

	        // 合併購物車 (登入成功才做)
	        cartService.loginMergeCart(session.getId(), loginMember.getMemberId());

	        System.out.println("Login success, cart merged for memberId=" + loginMember.getMemberId());
	    	
	        session.setAttribute("member", member.get());
	        System.out.println(member);
	        return "redirect:/my-account";
	    } else {
	    	redirectAttributes.addFlashAttribute("errorMessage", "帳號或密碼錯誤");
	    	redirectAttributes.addFlashAttribute("errorSource", "login");
//	        model.addAttribute("error", "帳號或密碼錯誤");
	        return "redirect:/shop/login";
	    }
	}
	
	@GetMapping("/logout")
	public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
		
	    if (session != null) {
		session.removeAttribute("member");
	    session.invalidate(); // 讓 session 失效（登出）
	    }
	 
	    redirectAttributes.addFlashAttribute("logoutMessage", "您已成功登出");
	    redirectAttributes.addFlashAttribute("logoutMessage", "login");
	    return "redirect:/shop/login"; // 返回登入頁
	}
	
	// 去除BindingResult中某個欄位的FieldError紀錄
	public BindingResult removeFieldError(MemberVO memberVO, BindingResult result, String removedFieldname) {
		List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
				.filter(fieldname -> !fieldname.getField().equals(removedFieldname))
				.collect(Collectors.toList());
		result = new BeanPropertyBindingResult(memberVO, "memberVO");
		for (FieldError fieldError : errorsListToKeep) {
			result.addError(fieldError);
		}
		return result;
	}
	
	@ExceptionHandler(value = { ConstraintViolationException.class })
	//@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public ModelAndView handleError(HttpServletRequest req,ConstraintViolationException e,Model model) {
	    Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
	    StringBuilder strBuilder = new StringBuilder();
	    for (ConstraintViolation<?> violation : violations ) {
	          strBuilder.append(violation.getMessage() + "<br>");
	    }
	    //==== 以下第92~96行是當前面第77行返回 /src/main/resources/templates/back-end/emp/select_page.html用的 ====   
//	    model.addAttribute("empVO", new EmpVO());
//    	EmpService empSvc = new EmpService();
		List<MemberVO> list = memberSvc.getAll();
		model.addAttribute("memberListData", list);     // for select_page.html 第97 109行用
		String message = strBuilder.toString();
	    return new ModelAndView("admin/member/select_page", "errorMessage", "請修正以下錯誤:<br>"+message);
	}
	
}