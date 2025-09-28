package com.babymate.babyrecord.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.babymate.babyrecord.model.BabyrecordService;
import com.babymate.babyrecord.model.BabyrecordVO;
import com.babymate.clinic.model.ClinicRepository;
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
@RequestMapping("/admin/babyrecord")
public class BabyrecordidController {

	@Autowired
	BabyhandbookService babyhandbookSvc;
	
	@Autowired
	 BabyrecordService  babyrecordSvc;
	
	@Autowired
	ClinicRepository clinicRepository;
	
	@PostMapping("getOne_For_Display")
	public String getOne_For_Display(
		/***************************1.接收請求參數 - 輸入格式的錯誤處理*************************/
		@NotEmpty(message="小孩手冊編號:請勿空白")
		@Digits(integer = 3, fraction = 0, message = "小孩手冊編號:請填數字-請勿超過{integer}位數")
		@Min(value = 1, message = "小孩手冊編號: 不能小於{value}")
		@Max(value = 100, message = "小孩手冊編號: 不能超過{value}")
		@RequestParam("babyrecordid") String babyrecordid,
		ModelMap model) {
		
		
		/***************************2.開始查詢資料*********************************************/

	    // 查詢單筆
		BabyrecordVO babyrecordVO = babyrecordSvc.getOneBabyrecord(Integer.valueOf(babyrecordid));
		
		// 查詢全部 babyrecord
		List<BabyrecordVO> list = babyrecordSvc.getAll();
		
		// 建立 babyrecordid -> clinicDto 的 map
	    Map<Integer, com.babymate.clinic.model.ClinicDto> clinicDtoMap = new HashMap<>();
	    for (BabyrecordVO record : list) {
	        if (record.getClinicid() != null) {
	            var dto = clinicRepository.findDtoById(record.getClinicid());
	            if (dto != null) {
	                clinicDtoMap.put(record.getBabyrecordid(), dto);
	            }
	        }
	    }
	    
		model.addAttribute("babyrecordList", list);
		model.addAttribute("clinicDtoMap", clinicDtoMap); 
		model.addAttribute("babyhandbookVO", new BabyhandbookVO());
		List<BabyhandbookVO> list2 = babyhandbookSvc.getAll();
		model.addAttribute("babyhandbookList", list2);
		
		if(babyrecordVO == null) {
			model.addAttribute("errorMessage", "查無資料");
			return "admin/babyrecord/select_page";
		}
		
		/***************************3.查詢完成,準備轉交(Send the Success view)*****************/
		model.addAttribute("babyrecordVO", babyrecordVO);
		
		return "admin/babyrecord/select_page";
		
	}
	
	@ExceptionHandler(value = { ConstraintViolationException.class})
	public ModelAndView handleError(HttpServletRequest req, ConstraintViolationException e, Model model) {
		Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
		StringBuilder strBuilder = new StringBuilder();
		for(ConstraintViolation<?> violation : violations) {
			strBuilder.append(violation.getMessage() + "<br>");
		}
		
		List<BabyrecordVO> list = babyrecordSvc.getAll();
		model.addAttribute("babyrecordList", list);
		model.addAttribute("babyrecordVO", new BabyrecordVO());
		List<BabyhandbookVO> list2 = babyhandbookSvc.getAll();
		model.addAttribute("recordList", list2);
		String message = strBuilder.toString();
		return new ModelAndView("admin/babyrecord/select_page", "errorMessage", "請修正以下錯誤:<br>" + message);
		
		
	}
	
}
