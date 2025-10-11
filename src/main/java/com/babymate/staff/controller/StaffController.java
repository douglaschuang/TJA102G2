package com.babymate.staff.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.babymate.staff.model.StaffVO;
import com.babymate.staff.service.StaffService;
import com.babymate.util.EncodingUtil;
import com.babymate.valid.UpdateGroup;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Validated
@RequestMapping("/staff")
public class StaffController {

	private static final Logger logger = LoggerFactory.getLogger(StaffController.class);

	@Autowired
	StaffService staffSvc;

	/**
     * 根據員工編號查詢並顯示對應員工資料。
     *
     * @param staffId 使用者輸入的員工編號。
     * @param model 用於傳遞資料至 View 的 ModelMap 物件。
     * @return 返回對應的 View 名稱：
     *         - 若查無資料，返回 "admin/staff/select_page"
     *         - 查詢成功則返回 "admin/staff/listOneStaff"
     */
	@PostMapping("getOne_For_Display")
	public String getOne_For_Display(
			@NotEmpty(message = "員工編號: 請勿空白")
			@RequestParam("staffId") String staffId, ModelMap model) {

		StaffVO staffVO = staffSvc.getOneStaff(Integer.valueOf(staffId));

		List<StaffVO> list = staffSvc.getAll();
		model.addAttribute("staffListData", list); 

		if (staffVO == null) {
			logger.info("getOne_For_Display - 查無員工資料");
			model.addAttribute("errorMessage", "查無資料");
			return "admin/staff/select_page";
		}

		model.addAttribute("staffVO", staffVO);
		return "admin/staff/listOneStaff"; 
	}

	/**
     * 新增員工資料。
     *
     * @param model 用於傳遞資料至 View 的 Model 物件。
     * @return 返回對應的 View 名稱 admin/staff/staffadd，用於顯示新增員工頁面。
     */
	@GetMapping("/staffadd")
	public String staffAdd(Model model) {
		StaffVO staff = new StaffVO();
		model.addAttribute("StaffVO", staff);
		model.addAttribute("pageTitle", "員工管理｜新增員工");
		return "admin/staff/staffadd";
	}

	/**
	 * 處理新增員工的請求。
	 *
	 * @param staffVO 表單綁定的員工資料物件，包含新增的資料欄位。
	 * @param result 用於接收驗證結果，若有錯誤會回傳新增頁面。
	 * @param model 用來傳遞資料到 View，如錯誤訊息或成功提示等。
	 * @param parts 上傳的員工圖片（MultipartFile 陣列），只取第一張圖片儲存。
	 * @return 若新增成功，導向員工列表頁面（listAllStaff）；若有錯誤，返回新增頁面（staffadd）。
	 * @throws IOException 處理圖片上傳過程中若發生 I/O 錯誤會拋出此例外。
	 */
	@PostMapping("insert")
	public String insert(@Valid StaffVO staffVO, BindingResult result, ModelMap model,
			@RequestParam("pic") MultipartFile[] parts) throws IOException {

		StaffVO staffTemp = staffSvc.getOneStaffByAccount(staffVO.getAccount());
		
		// 確定員工是否已經存在, 若存在不允許新增
		if (staffTemp != null) {
			logger.info("insert - 員工帳號 {}: 已存在, 請重新輸入", staffTemp.getAccount());
			model.addAttribute("errorMessage", "員工帳號: 已存在, 請重新輸入");
			model.addAttribute("StaffVO", staffVO);
			return "admin/staff/staffadd";
		}

		// 移除圖片欄位的綁定錯誤（若有）
		result = removeFieldError(staffVO, result, "pic");

		// 如果有上傳圖片才處理圖片
		if (!parts[0].isEmpty()) {
			for (MultipartFile multipartFile : parts) {
				byte[] buf = multipartFile.getBytes();
				staffVO.setPic(buf);
			}
		}

		// 密碼欄位轉碼MD5 Hash
		staffVO.setPassword(EncodingUtil.hashMD5(staffVO.getPassword()));

		if (result.hasErrors()) {
			model.addAttribute("StaffVO", staffVO);
			return "admin/staff/staffadd";
		}
		
		staffSvc.addStaff(staffVO);
		List<StaffVO> list = staffSvc.getAll();
		
		model.addAttribute("staffListData", list); 
		model.addAttribute("success", "- (新增成功)");
		logger.info("insert - 員工帳號 {}: 新增成功", staffVO.getAccount());
		
		return "redirect:/admin/staff/listAllStaff"; 
	}

	/**
     * 編輯員工資料。
     *
     * @param staffId 前端欲編輯的員工Id
     * @param model 用於傳遞資料至 View 的 ModelMap 物件。
     * @return 返回對應的 View 名稱 admin/staff/staffedit，用於顯示編輯員工頁面。
     */
	@PostMapping("staffedit")
	public String getOne_For_Update(@RequestParam("staffId") String staffId, ModelMap model) {
		
		StaffVO staffVO = staffSvc.getOneStaff(Integer.valueOf(staffId));

		model.addAttribute("StaffVO", staffVO);
		model.addAttribute("pageTitle", "員工管理｜編輯員工");
		return "admin/staff/staffedit"; 
	}

	/**
	 * 處理更新員工資料的請求。
	 *
	 * @param staffVO 綁定的員工資料物件，包含更新內容。使用 {@code UpdateGroup} 驗證群組進行欄位驗證。
	 * @param result Spring 的驗證結果，包含是否有欄位錯誤。
	 * @param model 用於傳遞資料至前端 View，例如成功訊息或錯誤資料。
	 * @param parts 上傳的員工圖片檔案陣列，僅處理第一張圖片。
	 * @return 若驗證失敗則返回編輯頁面 {@code admin/staff/staffedit}；
	 *         成功則重導至 {@code /admin/staff/listAllStaff} 員工列表頁。
	 * @throws IOException 處理圖片檔案時若發生 I/O 錯誤會拋出此例外。
	 */
	@PostMapping("update")
	public String update(@Validated(UpdateGroup.class) @ModelAttribute("StaffVO") StaffVO staffVO, BindingResult result,
			ModelMap model, @RequestParam("pic") MultipartFile[] parts) throws IOException {

		// 移除圖片欄位的驗證錯誤（若有）
		result = removeFieldError(staffVO, result, "pic");

		// 若使用者未上傳新圖片，則保留原圖片
		if (parts[0].isEmpty()) {
			byte[] pic = staffSvc.getOneStaff(staffVO.getStaffId()).getPic();
			staffVO.setPic(pic);
		} else {
			for (MultipartFile multipartFile : parts) {
				byte[] pic = multipartFile.getBytes();
				staffVO.setPic(pic);
			}
		}

	    // 若前端未重新輸入密碼（密碼為 null 或空字串），保留原密碼
		StaffVO originalStaff = staffSvc.getOneStaff(staffVO.getStaffId());
		// 若密碼欄位空白，保留原始密碼
		if (staffVO.getPassword() == null || staffVO.getPassword().isEmpty()) {
			staffVO.setPassword(originalStaff.getPassword());
		} else {
			// Hash加密編碼
			staffVO.setPassword(EncodingUtil.hashMD5(staffVO.getPassword()));
		}

		// 如果資料檢核有錯誤時, 返回員工編輯頁面
		if (result.hasErrors()) {
			model.addAttribute("StaffVO", staffVO);
			return "admin/staff/staffedit";
		}

		staffVO.setUpdateDate(LocalDateTime.now()); // 資料更新時間
		staffSvc.updateStaff(staffVO);

		logger.info("update - 修改員工{} 成功", staffVO.getAccount());
		model.addAttribute("success", "- (修改成功)");
		staffVO = staffSvc.getOneStaff(Integer.valueOf(staffVO.getStaffId()));
		model.addAttribute("staffVO", staffVO);
		
		return "redirect:/admin/staff/listAllStaff";
	}

	/**
     * 變更員工密碼。
     *
     * @param staffId 前端欲編輯的員工Id
     * @param model 用於傳遞資料至 View 的 ModelMap 物件。
     * @return 返回對應的 View 名稱 admin/staff/staffchangepwd，用於顯示編輯員工密碼變更頁面。
     */
	@PostMapping("staffchangepwd")
	public String getOneForChangePwd(@RequestParam("staffId") String staffId, ModelMap model) {
		StaffVO staffVO = staffSvc.getOneStaff(Integer.valueOf(staffId));

		model.addAttribute("StaffVO", staffVO);
		model.addAttribute("pageTitle", "員工管理｜密碼變更");
		return "admin/staff/staffchangepwd"; 
	}

	/**
     * 刪除指定員工。
     *
     * @param staffId 前端欲刪除的員工Id
     * @param model 用於傳遞資料至 View 的 ModelMap 物件。
     * @return 返回對應的 View 名稱 admin/staff/listAllStaff，用於顯示員工列表頁面。
     */
	@PostMapping("delete")
	public String delete(@RequestParam("staffId") String staffId, ModelMap model) {
		staffSvc.deleteStaff(Integer.valueOf(staffId));
		List<StaffVO> list = staffSvc.getAll();
		
		logger.info("delete - 刪除員工ID: {} 成功",staffId);
		model.addAttribute("staffListData", list);
		model.addAttribute("success", "- (刪除成功)");
		
		return "redirect:/admin/staff/listAllStaff";
	}

	/**
	 * 移除指定欄位的驗證錯誤。
	 *
	 * @param staffVO 表單綁定的物件，用於建立新的 BindingResult。
	 * @param result 原始的 BindingResult，包含所有欄位的驗證結果。
	 * @param removedFieldname 要移除驗證錯誤的欄位名稱。
	 * @return 回傳一個新的 BindingResult，已移除指定欄位的錯誤訊息。
	 */
	public BindingResult removeFieldError(StaffVO staffVO, BindingResult result, String removedFieldname) {
		
		// 過濾掉指定欄位的錯誤，只保留其他欄位的錯誤
		List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
				.filter(fieldname -> !fieldname.getField().equals(removedFieldname)).collect(Collectors.toList());
		
		// 建立一個新的 BindingResult 物件，並重新加入保留的錯誤
		result = new BeanPropertyBindingResult(staffVO, "staffVO");
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
		
		// 將員工列表資料重新載入，避免 View 資料遺失
		List<StaffVO> list = staffSvc.getAll();
		model.addAttribute("staffListData", list);
		
		// 組合錯誤訊息
		String message = strBuilder.toString();
		logger.info("handleError - 驗證欄位錯誤: {}",message);
		
		return new ModelAndView("admin/staff/stafflist", "errorMessage", "請修正以下錯誤:<br>" + message);
	}

}