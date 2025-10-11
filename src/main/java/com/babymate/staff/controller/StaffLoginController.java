package com.babymate.staff.controller;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.babymate.staff.model.StaffVO;
import com.babymate.staff.service.StaffService;
import com.babymate.util.EncodingUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@Controller
@Validated
@RequestMapping("/admin/checkin")
public class StaffLoginController {

	private static final Logger logger = LoggerFactory.getLogger(StaffLoginController.class);

	@Autowired
	StaffService staffSvc;

	@GetMapping("login")
	public String loginPage() {
		return "admin/login"; // 要有 login.html
	}

	@PostMapping("loginCheck")
	public String loginCheck(@RequestParam String account, @RequestParam String password, HttpSession session,
			RedirectAttributes redirectAttributes) {
		
		logger.info("loginCheck - loginCheck trigger.");
		String hashedPwd = EncodingUtil.hashMD5(password);

		Optional<StaffVO> staff = Optional.ofNullable(staffSvc.login(account, hashedPwd));
		if (staff.isPresent()) {
			// 判斷帳號是否有效
			StaffVO s = staff.get();
			if (s.getStatus() != 1) {
				redirectAttributes.addFlashAttribute("errorMessage", "帳號停用中, 請洽後台管理員.");
				return "redirect:/admin/login";
			}
			session.setAttribute("staff", staff.get());
			return "redirect:/staff/permission";
		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "帳號或密碼錯誤");
			return "redirect:/admin/login";
		}
	}

	@GetMapping("/logout")
	public String logout(HttpSession session, RedirectAttributes redirectAttributes) {

		if (session != null) {
			session.removeAttribute("staff");
			session.invalidate(); // 讓 session 失效（登出）
		}

		redirectAttributes.addFlashAttribute("logoutMessage", "您已成功登出");
		return "redirect:/admin/login"; // 返回登入頁
	}

	// 去除BindingResult中某個欄位的FieldError紀錄
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
		String message = strBuilder.toString();
		logger.info("handleError - 驗證欄位錯誤: {}",message);
		
		return new ModelAndView("admin/staff/stafflist", "errorMessage", "請修正以下錯誤:<br>" + message);
	}

}
