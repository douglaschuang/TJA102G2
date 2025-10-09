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
	public String loginCheck(@RequestParam String account,
	                           @RequestParam String password,
	                           HttpSession session,
	                           RedirectAttributes redirectAttributes) {
//		System.out.println("loginCheck trigger.");
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
//	        return "redirect:/admin";
	        return "redirect:/staff/permission";
	    } else {
	    	redirectAttributes.addFlashAttribute("errorMessage", "帳號或密碼錯誤");
//	        model.addAttribute("error", "帳號或密碼錯誤");
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
		List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
				.filter(fieldname -> !fieldname.getField().equals(removedFieldname))
				.collect(Collectors.toList());
		result = new BeanPropertyBindingResult(staffVO, "staffVO");
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
		List<StaffVO> list = staffSvc.getAll();
		model.addAttribute("staffListData", list);     // for select_page.html 第97 109行用
		String message = strBuilder.toString();
	    return new ModelAndView("admin/staff/stafflist", "errorMessage", "請修正以下錯誤:<br>"+message);
	}
	
}
