package com.babymate.babyrecord.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.babymate.babyhandbook.model.BabyhandbookService;
import com.babymate.babyhandbook.model.BabyhandbookVO;
import com.babymate.babyrecord.model.BabyrecordService;
import com.babymate.babyrecord.model.BabyrecordVO;
import com.babymate.clinic.model.ClinicDto;
import com.babymate.clinic.model.ClinicRepository;
import com.babymate.member.model.MemberVO;
import com.babymate.member.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
//
@Controller
@RequestMapping("/admin/babyrecord")
public class BabyrecordController {

	@Autowired
	BabyrecordService babyrecordSvc;

	@Autowired
	BabyhandbookService babyhandbookSvc;
	
	@Autowired
	ClinicRepository clinicRepository;

	public BabyrecordController(BabyrecordService babyrecordSvc, BabyhandbookService babyhandbookSvc) {
		this.babyrecordSvc = babyrecordSvc;
		this.babyhandbookSvc = babyhandbookSvc;
	}

	@GetMapping("addBabyrecord")
	public String record(ModelMap model) {
		BabyrecordVO babyrecordVO = new BabyrecordVO();
		
		BabyhandbookVO b = new BabyhandbookVO(); //初始化member
		b.setBabyhandbookid(1); 
		babyrecordVO.setBabyhandbook(b);
		
		model.addAttribute("babyrecordVO", babyrecordVO); // model.addAttribute("變數名稱", 變數值);
		return "admin/babyrecord/addBabyrecord";
	}

	@PostMapping("insert")
	public String insert(@Valid BabyrecordVO babyrecordVO, BindingResult result, ModelMap model,
			@RequestParam("babyrecordfiles") MultipartFile[] parts) throws IOException {
		
		//先初始化 member 綁定 member.memberId
		if (babyrecordVO.getBabyhandbook() == null) {
	        babyrecordVO.setBabyhandbook(new BabyhandbookVO());
	    }
		
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 去除BindingResult中upFiles欄位的FieldError紀錄 --> 見第143行
		result = removeFieldError(babyrecordVO, result, "babyrecordfiles");

		if (!parts[0].isEmpty()) { // 使用者不一定要上傳的圖片
			for (MultipartFile multipartFile : parts) {
				byte[] buf = multipartFile.getBytes();
				babyrecordVO.setBabyrecordfiles(buf);
			}
		}
		
		
		if (result.hasErrors()) {  //有表單錯誤就返回原頁面
			return "admin/babyrecord/addBabyrecord";
		}

		Integer babyhandbookid = babyrecordVO.getBabyhandbook() != null ? babyrecordVO.getBabyhandbook().getBabyhandbookid() : null; // 把 memberId 從 babyhandbookVO 裡拿出來
		
		if (babyhandbookid == null) {
	        result.rejectValue("babyhandbook.babyhandbookid", null, "寶寶資料有誤，請重新登入或重填表單");
	        return "admin/babyrecord/addBabyrecord";
	    }
		
		BabyhandbookVO babyhandbook = babyhandbookSvc.getOneBabyhandbook(babyhandbookid); // 從 DB 查出來
		babyrecordVO.setBabyhandbook(babyhandbook); // 把查出來的  設回去
		
		/*************************** 2.開始新增資料 *****************************************/

		babyrecordSvc.addBabyrecord(babyrecordVO); //繼續存檔

		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/

		List<BabyrecordVO> list = babyrecordSvc.getAll();
		model.addAttribute("babyrecord", list);
		model.addAttribute("success", "(新增成功)");
		
		return "redirect:/admin/babyrecord/records?babyhandbookid=" + babyrecordVO.getBabyhandbook().getBabyhandbookid();
	}

	@PostMapping("getOne_For_Update")
	public String getOne_For_Update(@RequestParam("babyrecordid") String babyrecordid, ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始查詢資料 *****************************************/
		BabyrecordVO babyrecordVO = babyrecordSvc.getOneBabyrecord(Integer.valueOf(babyrecordid));

		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("babyrecordVO", babyrecordVO);
		return "admin/babyrecord/update_babyrecord_input";
	}

	@PostMapping("update")
	public String update(@Valid @ModelAttribute("babyrecordVO") BabyrecordVO babyrecordVO, BindingResult result, ModelMap model,
		    @RequestParam("babyrecordfiles") MultipartFile[] parts) throws IOException {
	
		BabyhandbookVO handbook = new BabyhandbookVO();
	    handbook.setBabyhandbookid(babyrecordVO.getBabyhandbook().getBabyhandbookid());
	    babyrecordVO.setBabyhandbook(handbook);

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 去除BindingResult中upFiles欄位的FieldError紀錄 --> 見第143行
		result = removeFieldError(babyrecordVO, result, "babyrecordfiles");

		if (parts[0].isEmpty()) { // 使用者未選擇要上傳的新圖片時
			
				byte[] babyrecordfiles = babyrecordSvc.getOneBabyrecord(babyrecordVO.getBabyrecordid()).getBabyrecordfiles();
				babyrecordVO.setBabyrecordfiles(babyrecordfiles);

		} else {
			for (MultipartFile mutipartFile : parts) {
				byte[] babyrecordfiles = mutipartFile.getBytes();
				babyrecordVO.setBabyrecordfiles(babyrecordfiles);
			}
		}
		if (result.hasErrors()) {
			return "admin/babyrecord/update_babyrecord_input";
		}
		/*************************** 2.開始修改資料 *****************************************/

		babyrecordSvc.updateBabyrecord(babyrecordVO);

		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/

		model.addAttribute("success", "修改成功");

	    return "redirect:/admin/babyrecord/records?babyhandbookid=" + babyrecordVO.getBabyhandbook().getBabyhandbookid();

	}

	// 取得照片的原始 Byte 資料
	@GetMapping("photo/{id}")
	public ResponseEntity<byte[]> getPhotoBytesRaw(@PathVariable("id") Integer id) {
		byte[] photoBytes = babyrecordSvc.getPhotoBytesRaw(id);
		if (photoBytes == null || photoBytes.length == 0) {
			return ResponseEntity.notFound().build(); // 404 Not Found
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG); // 根據實際圖片格式調整
		return new ResponseEntity<>(photoBytes, headers, HttpStatus.OK);
	}

	/*
	 * Method used to populate the Map Data in view. 如 : <form:select
	 * path="babyhandbookid" id="babyhandbookid" items="${babyhandbookMapData}" />
	 */

	@ModelAttribute("babyrecordMap")
	protected Map<String, String> referenceMapData() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("男", "男");
		map.put("女", "女");
		return map;
	}

	// 去除BindingResult中某個欄位的FieldError紀錄
	public BindingResult removeFieldError(BabyrecordVO babyrecordVO, BindingResult result,
			String removedFieldname) {
		List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
				.filter(fieldname -> !fieldname.getField().equals(removedFieldname)).collect(Collectors.toList());
		result = new BeanPropertyBindingResult(babyrecordVO, "babyrecordVO");
		for (FieldError fieldError : errorsListToKeep) {
			result.addError(fieldError);
		}
		return result;
	}
	
	@GetMapping("records")
	public String listAllBabyrecord(@RequestParam(value = "babyhandbookid", required = false ) Integer babyhandbookid, Model model,
			 RedirectAttributes redirectAttributes) {
		
	    if (babyhandbookid == null) {
			redirectAttributes.addFlashAttribute("error", "缺少必要的 babyhandbookid");
	        return "redirect:/admin/babyhandbook/list"; // 導向list頁面
	    }
	    
	 // 防止畫面會 null pointer
	    BabyhandbookVO babyhandbook = babyhandbookSvc.getOneBabyhandbook(babyhandbookid);
	    model.addAttribute("babyhandbook", babyhandbook);
	    
		List<BabyrecordVO> list = babyrecordSvc.findByBabyhandbookId(babyhandbookid);
		
		// 建立 clinicDtoMap
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
	    model.addAttribute("babyhandbookid", babyhandbookid);
		return "admin/babyrecord/records";
	}
	
	@PostMapping("delete")
	public String delete(@RequestParam("babyrecordid") Integer babyrecordid,
	                     RedirectAttributes redirectAttributes) {

	    // 查出 babyhandbookid
	    Integer babyhandbookid = babyrecordSvc.getOneBabyrecord(babyrecordid).getBabyhandbook().getBabyhandbookid();

	    babyrecordSvc.deleteBabyrecord(babyrecordid);
	    redirectAttributes.addAttribute("babyhandbookid", babyhandbookid);
	    
	    return "redirect:/admin/babyrecord/records?babyhandbookid=" + babyhandbookid;
	}

	
	
}
