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
import java.util.Objects;
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
import com.babymate.mhb.model.MhbVO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
//
@Controller
@RequestMapping("/blog")
public class BabyhandbookFrontendController {

	@Autowired
	BabyhandbookService babyhandbookSvc;

	@Autowired
	MemberService memberSvc;

	@Autowired
	private BabyrecordService babyrecordSvc;

	public BabyhandbookFrontendController(BabyhandbookService babyhandbookSvc, MemberService memberSvc,
			BabyrecordService babyrecordSvc) {
		this.babyhandbookSvc = babyhandbookSvc;
		this.memberSvc = memberSvc;
		this.babyrecordSvc = babyrecordSvc;
	}

	@ModelAttribute("babyhandbookMap")
	protected Map<String, String> referenceMapData() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("男", "男");
		map.put("女", "女");
		return map;
	}

	@GetMapping("/full-grid-left-bb")
	public String showBlogPage(@RequestParam(required = false, defaultValue = "babyhandbook") String tab,
			@RequestParam(name = "babyhandbookid", required = false) Integer babyhandbookid, Model model,
			HttpSession session) {

		// 檢查登入
		MemberVO member = (MemberVO) session.getAttribute("member");
		if (member == null) {
			return "redirect:/shop/login";
		}

		//查出該會員所有手冊
	    List<BabyhandbookVO> handbookList = babyhandbookSvc.findAllByMemberId(member.getMemberId());

	    // 沒有手冊 → 導到新增頁面
	    if (handbookList.isEmpty()) {
	        return "redirect:/u/baby/babyhandbook-add";
	    }
	    
	    // 查詢指定的或最新的寶寶手冊
		BabyhandbookVO babyhandbook;

		if (babyhandbookid != null) {
			babyhandbook = babyhandbookSvc.findByIdAndMemberId(babyhandbookid, member.getMemberId());
			if (babyhandbook == null) {
	            babyhandbook = handbookList.get(0); // 指定的沒找到 → 用最新一筆
	        }
			
		} else {
			babyhandbook = handbookList.get(0); // 預設用最新
		}

	
		// 有手冊 → 計算週齡並塞入 model
		Integer babyweek = calculateBabyWeek(babyhandbook.getBabybirthday());

		model.addAttribute("activeTab", tab);
	    model.addAttribute("babyhandbook", babyhandbook); // 單一手冊（當前顯示用）
	    model.addAttribute("babyhandbookList", handbookList); // 所有手冊（給前端選擇用）
		model.addAttribute("babyWeek", babyweek);
		
		if ("babyrecord".equals(tab)) {
			List<BabyrecordVO> babyrecordList = babyrecordSvc.findByBabyhandbookId(babyhandbook.getBabyhandbookid());
			model.addAttribute("babyrecord", babyrecordList);
		}

		return "frontend/blog-full-then-grid-left-sidebar";

	}
	

	// 🔧 計算週齡的方法
	private Integer calculateBabyWeek(Date babybirthday) {
		if (babybirthday == null)
			return null;

		LocalDate birthdate = babybirthday.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		long weeks = ChronoUnit.WEEKS.between(birthdate, LocalDate.now());
		return (int) Math.max(0, weeks); // 不讓週數為負數
	}
	

	@GetMapping("/u/baby/babyhandbook/{id}/edit")
	public String editBabyhandbook(@PathVariable("id") Integer id, Model model, HttpSession session) {
		
		 System.out.println("✔ POST進來了，id = " + id); 
		 
		 
		MemberVO member = (MemberVO) session.getAttribute("member");
		if (member == null) {
			return "redirect:/shop/login";
		}
		
		

		BabyhandbookVO babyhandbook = babyhandbookSvc.findByIdAndMemberId(id, member.getMemberId());
	    if (babyhandbook == null) {
	        return "redirect:/blog/full-grid-left-bb"; 
	    }
	    
	    
	    model.addAttribute("babyhandbookVO", babyhandbook);
	    model.addAttribute("babyhandbookMap", Map.of( "男", "女"));
	    
	    
	    return "frontend/u/baby/babyhandbook-edit"; 
	}

	@PostMapping("/u/baby/babyhandbook/{id}/edit")
	public String updateBabyhandbook(
	        @PathVariable Integer id,
	        @Valid @ModelAttribute("babyhandbookVO") BabyhandbookVO babyhandbookVO,
	        BindingResult result,
	        @RequestParam("up") MultipartFile file,
	        HttpSession session,
	        RedirectAttributes redirectAttributes
	) {
	    MemberVO member = (MemberVO) session.getAttribute("member");
	    if (member == null) {
	        return "redirect:/shop/login";
	    }

	    // 驗證有錯誤 → 返回表單
	    if (result.hasErrors()) {
	    	 result.getAllErrors().forEach(error -> System.out.println(error.toString()));
	        return "frontend/u/baby/babyhandbook";
	        
	    }

	    // 驗證是否為該會員的手冊
	    BabyhandbookVO existing = babyhandbookSvc.findByIdAndMemberId(id, member.getMemberId());
	    if (existing == null) {
	        return "redirect:/blog/full-grid-left-bb";
	    }

	    // 更新資料
	    existing.setBabyname(babyhandbookVO.getBabyname());
	    existing.setBabygender(babyhandbookVO.getBabygender());
	    existing.setBabybirthday(babyhandbookVO.getBabybirthday());

	    // 處理圖片上傳（可選）
	    if (file != null && !file.isEmpty()) {
	        try {
	            existing.setBabyhandbookfiles(file.getBytes());
	        } catch (IOException e) {
	            e.printStackTrace(); // 可換成 log
	        }
	    }

	    // 呼叫 Service 更新
	    babyhandbookSvc.updateBabyhandbook(existing);

	    // 回傳或重導
	    redirectAttributes.addFlashAttribute("success", "手冊更新成功！");
	    return "redirect:/blog/full-grid-left-bb?tab=babyhandbook&babyhandbookid=" + id;
	    
	}
	
	
	@GetMapping("/u/baby/babyhandbook")
	public String showAddBabyHandbookPage(Model model) {
		model.addAttribute("babyhandbookVO", new BabyhandbookVO());
		model.addAttribute("babyhandbookMap", Map.of("男", "女"));
		return "frontend/u/baby/babyhandbook-add";
	}
	
	@PostMapping("/u/baby/babyhandbook")
	public String addBabyHandbook(
	        @Valid @ModelAttribute("babyhandbookVO") BabyhandbookVO babyhandbookVO,
	        BindingResult result,
	        @RequestParam("up") MultipartFile file,
	        HttpSession session,
	        RedirectAttributes redirectAttributes) {

	    MemberVO member = (MemberVO) session.getAttribute("member");
	    if (member == null) {
	        return "redirect:/shop/login";
	    }

	    // 驗證錯誤
	    if (result.hasErrors()) {
	        return "frontend/u/baby/babyhandbook-add";
	    }

	    // 設定會員
	    babyhandbookVO.setMember(member);

	    // 上傳圖片
	    if (!file.isEmpty()) {
	        try {
	            babyhandbookVO.setBabyhandbookfiles(file.getBytes());
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    // 新增資料
	    babyhandbookSvc.addBabyhandbook(babyhandbookVO);

	    // 成功跳轉
	    redirectAttributes.addFlashAttribute("success", "成功新增寶寶手冊！");
	    return "redirect:/blog/full-grid-left-bb?tab=babyhandbook&babyhandbookid=" + babyhandbookVO.getBabyhandbookid();
	}

		// 軟刪除
		@PostMapping("/u/baby/babyhandbook/delete")
		public String delete(@RequestParam("babyhandbookid") Integer id, RedirectAttributes ra) {

			babyhandbookSvc.softDelete(id);

			ra.addFlashAttribute("success", "- (刪除成功)");
			return "redirect:/blog/full-grid-left-bb?tab=babyhandbook&babyhandbookid=" + id;
		}
	
}
