package com.babymate.babyhandbook.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.babymate.babyhandbook.model.BabyhandbookService;
import com.babymate.babyhandbook.model.BabyhandbookVO;
import com.babymate.member.service.MemberService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/babyhandbook")
public class BabyhandbookController {

	@Autowired
	BabyhandbookService babyhandbookSvc;

	@Autowired
	MemberService memberSvc;

	@GetMapping("addBabyhandbook")
	public String addBabyhandbook(ModelMap model) {
		BabyhandbookVO babyhandbookVO = new BabyhandbookVO();
		model.addAttribute("babyhandbookVO", babyhandbookVO); // model.addAttribute("變數名稱", 變數值);
		return "back-end/babyhandbook/listAllBabyhandbook";
	}

	@PostMapping("insert")
	public String insert(@Valid BabyhandbookVO babyhandbookVO, BindingResult result, ModelMap model,
			@RequestParam("babyhandbookfiles") MultipartFile[] parts) throws IOException {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 去除BindingResult中upFiles欄位的FieldError紀錄 --> 見第143行
		result = removeFieldError(babyhandbookVO, result, "babyhandbookfiles");

		if (!parts[0].isEmpty()) { // 使用者不一定要上傳的圖片
			for (MultipartFile multipartFile : parts) {
				byte[] buf = multipartFile.getBytes();
				babyhandbookVO.setBabyhandbookfiles(buf);
			}
		}
		if (result.hasErrors()) {
			return "back-end/babyhandbook/addBabyhandbook";
		}

		/*************************** 2.開始新增資料 *****************************************/

		babyhandbookSvc.addBabyhandbook(babyhandbookVO);

		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/

		List<BabyhandbookVO> list = babyhandbookSvc.getAll();
		model.addAttribute("babyhandbookListData", list);
		model.addAttribute("success", "(新增成功)");
		return "redirect:/babyhandbook/listAllBabyhandbook";
	}

	@PostMapping("getOne_For_Update")
	public String getOne_For_Update(@RequestParam("babyhandbookid") String babyhandbookid, ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始查詢資料 *****************************************/
		BabyhandbookVO babyhandbookVO = babyhandbookSvc.getOneBabyhandbook(Integer.valueOf(babyhandbookid));

		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("babyhandbookVO", babyhandbookVO);
		return "back-end/babyhandbook/update_babyhandbook_input";
	}

	@PostMapping("update")
	public String update(@Valid BabyhandbookVO babyhandbookVO, BindingResult result, ModelMap model,
			@RequestParam("babyhandbookfiles") MultipartFile[] parts)throws IOException {
		
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 去除BindingResult中upFiles欄位的FieldError紀錄 --> 見第143行
		result = removeFieldError(babyhandbookVO, result, "babyhandbookfiles");
		
		if(parts[0].isEmpty()) { // 使用者未選擇要上傳的新圖片時
			byte[] babyhandbookfiles = babyhandbookSvc.getOneBabyhandbook(babyhandbookVO.getBabyhandbookid()).getBabyhandbookfiles();
			babyhandbookVO.setBabyhandbookfiles(babyhandbookfiles);
		}else {
			for(MultipartFile mutipartFile : parts) {
				byte[] babyhandbookfiles = mutipartFile.getBytes();
				babyhandbookVO.setBabyhandbookfiles(babyhandbookfiles);
			}
		}
		if(result.hasErrors()) {
			return"back-end/babyhandbook/update_babyhandbook_input";
		}
		/*************************** 2.開始修改資料 *****************************************/
		
		babyhandbookSvc.updateBabyhandbook(babyhandbookVO);
		
		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/
		
		model.addAttribute("success","修改成功");
		babyhandbookVO = babyhandbookSvc.getOneBabyhandbook(Integer.valueOf(babyhandbookVO.getBabyhandbookid()));
		model.addAttribute("babyhandbookVO", babyhandbookVO);
		return "back-end/babyhandbook/listOneBabyhandbook";
			
	}
	
	
	@PostMapping("delete")
	public String delete(@RequestParam("babyhandbookid") String babyhandbookid, ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始刪除資料 *****************************************/
		
		babyhandbookSvc.deleteBabyhandbook(Integer.valueOf(babyhandbookid));
		/*************************** 3.刪除完成,準備轉交(Send the Success view) **************/
	
		List<BabyhandbookVO> list = babyhandbookSvc.getAll();
		model.addAttribute("babyhandbookListData", list);
		model.addAttribute("success", "刪除成功");
		return "back-end/babyhandbook/listAllBabyhandbook";
	}
	
	/*
	 * Method used to populate the Map Data in view. 如 : 
	 * <form:select path="babyhandbookid" id="babyhandbookid" items="${babyhandbookMapData}" />
	 */
	
	@ModelAttribute("babyhandbookMapData")
	protected Map < String, String> referenceMapData(){
		Map< String, String> map = new LinkedHashMap< String, String>();
		map.put("男生", "男生");
		map.put("女生", "女生");
		return map;
	}
	
	// 去除BindingResult中某個欄位的FieldError紀錄
	public BindingResult removeFieldError(BabyhandbookVO babyhandbookVO, BindingResult result, String removedFieldname) {
		List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
			.filter(fieldname -> !fieldname.getField().equals(removedFieldname))
			.collect(Collectors.toList());
		result = new BeanPropertyBindingResult(babyhandbookVO, "babyhandbookVO");
		for(FieldError fieldError : errorsListToKeep) {
			result.addError(fieldError);
		}
		return result;
	}

}
