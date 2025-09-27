package com.babymate.babyhandbook.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.babymate.babyhandbook.model.BabyhandbookService;
import com.babymate.babyhandbook.model.BabyhandbookVO;
import com.babymate.babyrecord.model.BabyrecordService;
import com.babymate.babyrecord.model.BabyrecordVO;
import com.babymate.member.model.MemberVO;
import com.babymate.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;

@Controller
@RequestMapping("/admin/babyhandbook")
public class BabyhandbookController {

	@Autowired
	BabyhandbookService babyhandbookSvc;

	@Autowired
	MemberService memberSvc;
	
	@Autowired
	private BabyrecordService babyrecordSvc;

	public BabyhandbookController(BabyhandbookService babyhandbookSvc, MemberService memberSvc, BabyrecordService babyrecordSvc) {
		this.babyhandbookSvc = babyhandbookSvc;
		this.memberSvc = memberSvc;
		this.babyrecordSvc = babyrecordSvc;
	}

	@GetMapping("addBabyhandbook")
	public String addBabyhandbook(ModelMap model) {
		BabyhandbookVO babyhandbookVO = new BabyhandbookVO();
		
		MemberVO m = new MemberVO(); //初始化member
		m.setMemberId(1); 
		babyhandbookVO.setMember(m);
		
		model.addAttribute("babyhandbookVO", babyhandbookVO); // model.addAttribute("變數名稱", 變數值);
		return "admin/babyhandbook/addBabyhandbook";
	}

	@PostMapping("insert")
	public String insert(@Valid BabyhandbookVO babyhandbookVO, BindingResult result, ModelMap model,
			@RequestParam("babyhandbookfiles") MultipartFile[] parts) throws IOException {
		
		//先初始化 member 綁定 member.memberId
		if (babyhandbookVO.getMember() == null) {
	        babyhandbookVO.setMember(new MemberVO());
	    }
		
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 去除BindingResult中upFiles欄位的FieldError紀錄 --> 見第143行
		result = removeFieldError(babyhandbookVO, result, "babyhandbookfiles");

		if (!parts[0].isEmpty()) { // 使用者不一定要上傳的圖片
			for (MultipartFile multipartFile : parts) {
				byte[] buf = multipartFile.getBytes();
				babyhandbookVO.setBabyhandbookfiles(buf);
			}
		}
		
		
		if (result.hasErrors()) {  //有表單錯誤就返回原頁面
			return "admin/babyhandbook/addBabyhandbook";
		}

		
		Integer memberId = babyhandbookVO.getMember() != null ? babyhandbookVO.getMember().getMemberId() : null; // 把 memberId 從 babyhandbookVO 裡拿出來
		
		if (memberId == null) {
	        result.rejectValue("member.memberId", null, "會員資料有誤，請重新登入或重填表單");
	        return "admin/babyhandbook/addBabyhandbook";
	    }
		
		MemberVO member = memberSvc.getOneMember(memberId); // 從 DB 查出來
		babyhandbookVO.setMember(member); // 把查出來的 member 設回去
		
		/*************************** 2.開始新增資料 *****************************************/

		babyhandbookSvc.addBabyhandbook(babyhandbookVO); //繼續存檔

		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/

		List<BabyhandbookVO> list = babyhandbookSvc.getAll();
		model.addAttribute("babyhandbookList", list);
		model.addAttribute("success", "(新增成功)");
		return "redirect:/admin/babyhandbook/list";
	}

	@PostMapping("getOne_For_Update")
	public String getOne_For_Update(@RequestParam("babyhandbookid") String babyhandbookid, ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始查詢資料 *****************************************/
		BabyhandbookVO babyhandbookVO = babyhandbookSvc.getOneBabyhandbook(Integer.valueOf(babyhandbookid));

		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("babyhandbookVO", babyhandbookVO);
		return "admin/babyhandbook/update_babyhandbook_input";
	}

	@PostMapping("update")
	public String update(@Valid BabyhandbookVO babyhandbookVO, BindingResult result, ModelMap model,
			@RequestParam("babyhandbookfiles") MultipartFile[] parts) throws IOException {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 去除BindingResult中upFiles欄位的FieldError紀錄 --> 見第143行
		result = removeFieldError(babyhandbookVO, result, "babyhandbookfiles");

		if (parts[0].isEmpty()) { // 使用者未選擇要上傳的新圖片時
			byte[] babyhandbookfiles = babyhandbookSvc.getOneBabyhandbook(babyhandbookVO.getBabyhandbookid())
					.getBabyhandbookfiles();
			babyhandbookVO.setBabyhandbookfiles(babyhandbookfiles);
		} else {
			for (MultipartFile mutipartFile : parts) {
				byte[] babyhandbookfiles = mutipartFile.getBytes();
				babyhandbookVO.setBabyhandbookfiles(babyhandbookfiles);
			}
		}
		if (result.hasErrors()) {
			return "admin/babyhandbook/update_babyhandbook_input";
		}
		/*************************** 2.開始修改資料 *****************************************/

		babyhandbookSvc.updateBabyhandbook(babyhandbookVO);

		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/

		model.addAttribute("success", "修改成功");
		babyhandbookVO = babyhandbookSvc.getOneBabyhandbook(Integer.valueOf(babyhandbookVO.getBabyhandbookid()));
		model.addAttribute("babyhandbookVO", babyhandbookVO);
		return "redirect:/admin/babyhandbook/list";

	}

	// 軟刪除
	@PostMapping("delete")
	public String delete(@RequestParam("babyhandbookid") Integer id, RedirectAttributes ra) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始刪除資料 *****************************************/

		babyhandbookSvc.softDelete(id);
		/*************************** 3.刪除完成,準備轉交(Send the Success view) **************/

		ra.addFlashAttribute("success", "- (刪除成功)");
		return "redirect:/admin/babyhandbook/list";
	}

	// 還原刪除的資料
	@PostMapping("restore")
	public String restore(@RequestParam("babyhandbookid") Integer id, RedirectAttributes ra) {
		int i = babyhandbookSvc.restore(id); // UPDATE babyhandbook_handbook SET deleted=0 WHERE id=? AND deleted=1
		ra.addFlashAttribute("success", i > 0 ? "- (已復原 #" + id + ")" : "- (未找到或已復原)");
		return "redirect:/admin/babyhandbook/deleted"; // 復原後回垃圾桶
	}

	// 查詢所有未被刪除的資料（Admin用）
	@GetMapping("list")
	public String listAll(ModelMap model) {
		List<BabyhandbookVO> list = babyhandbookSvc.findAllActive();
		model.addAttribute("babyhandbookList", list);
		model.addAttribute("recordCountMap", babyrecordSvc.getCountMap()); // ← 讓頁面拿得到筆數
		model.addAttribute("pageTitle", "小孩手冊｜列表");
		return "admin/babyhandbook/list";

	}


	// 查詢所有已被軟刪除的資料（Admin用）
	@GetMapping("deleted")
	public String deleted(ModelMap model) {
		List<BabyhandbookVO> list = babyhandbookSvc.findAllDeleted();
		model.addAttribute("deletedList", list);
		model.addAttribute("pageTitle", "小孩手冊｜列表");
		return "admin/babyhandbook/deleted";
	}

	// 取得照片的原始 Byte 資料
	@GetMapping("photo/{id}")
	public ResponseEntity<byte[]> getPhotoBytesRaw(@PathVariable("id") Integer id) {
		byte[] photoBytes = babyhandbookSvc.getPhotoBytesRaw(id);
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

	@ModelAttribute("babyhandbookMap")
	protected Map<String, String> referenceMapData() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("男", "男");
		map.put("女", "女");
		return map;
	}

	// 去除BindingResult中某個欄位的FieldError紀錄
	public BindingResult removeFieldError(BabyhandbookVO babyhandbookVO, BindingResult result,
			String removedFieldname) {
		List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
				.filter(fieldname -> !fieldname.getField().equals(removedFieldname)).collect(Collectors.toList());
		result = new BeanPropertyBindingResult(babyhandbookVO, "babyhandbookVO");
		for (FieldError fieldError : errorsListToKeep) {
			result.addError(fieldError);
		}
		return result;
	}


}
