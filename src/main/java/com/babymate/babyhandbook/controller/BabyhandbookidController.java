package com.babymate.babyhandbook.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.Model;

import com.babymate.babyhandbook.model.BabyhandbookService;
import com.babymate.babyhandbook.model.BabyhandbookVO;
import com.babymate.member.model.MemberVO;
import com.babymate.member.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
//
@Controller
@Validated
@RequestMapping("/admin/babyhandbook")
public class BabyhandbookidController {

	@Autowired
	BabyhandbookService babyhandbookSvc;
	
	@Autowired
	MemberService memberSvc;
	
	
	@PostMapping("getOne_For_Display")
	public String getOne_For_Display(
		/***************************1.接收請求參數 - 輸入格式的錯誤處理*************************/
		@NotEmpty(message="小孩編號:請勿空白")
		@Digits(integer = 3, fraction = 0, message = "小孩編號:請填數字-請勿超過{integer}位數")
		@Min(value = 1, message = "小孩編號: 不能小於{value}")
		@Max(value = 100, message = "小孩編號: 不能超過{value}")
		@RequestParam("babyhandbookid") String babyhandbookid,
		ModelMap model) {
		
		
		/***************************2.開始查詢資料*********************************************/
		BabyhandbookVO babyhandbookVO = babyhandbookSvc.getOneBabyhandbook(Integer.valueOf(babyhandbookid));
		
		List<BabyhandbookVO> list = babyhandbookSvc.getAll();
		model.addAttribute("babyhandbookList", list);
		model.addAttribute("memberVO", new MemberVO());
		List<MemberVO> list2 = memberSvc.getAll();
		model.addAttribute("memberListData", list2);
		
		if(babyhandbookVO == null) {
			model.addAttribute("errorMessage", "查無資料");
			return "admin/babyhandbook/select_page";
		}
		
		/***************************3.查詢完成,準備轉交(Send the Success view)*****************/
		model.addAttribute("babyhandbookVO", babyhandbookVO);
		
		return "admin/babyhandbook/select_page";
		
	}
	
	@ExceptionHandler(value = { ConstraintViolationException.class})
	public ModelAndView handleError(HttpServletRequest req, ConstraintViolationException e, Model model) {
		Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
		StringBuilder strBuilder = new StringBuilder();
		for(ConstraintViolation<?> violation : violations) {
			strBuilder.append(violation.getMessage() + "<br>");
		}
		
		List<BabyhandbookVO> list = babyhandbookSvc.getAll();
		model.addAttribute("babyhandbookList", list);
		model.addAttribute("babyhandbookVO", new BabyhandbookVO());
		List<MemberVO> list2 = memberSvc.getAll();
		model.addAttribute("memberListData", list2);
		String message = strBuilder.toString();
		return new ModelAndView("admin/babyhandbook/select_page", "errorMessage", "請修正以下錯誤:<br>" + message);
		
		
	}
	
}
