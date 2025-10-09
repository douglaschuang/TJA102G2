package com.babymate.babyrecord.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.util.Optional;
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

import com.babymate.clinic.model.Clinic;
import com.babymate.clinic.model.ClinicDto;
import com.babymate.clinic.model.ClinicRepository;
import com.babymate.member.model.MemberVO;
import com.babymate.member.service.MemberService;
import com.babymate.mhb.model.MhbVO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;



import com.babymate.diary.model.DiaryEntryService;

//
@Controller
@RequestMapping("/u/baby")
public class BabyrecordFrontendController {
	
	@Autowired
	private DiaryEntryService diaryEntryService;

	@Autowired
	BabyhandbookService babyhandbookSvc;

	@Autowired
	MemberService memberSvc;
	
	@Autowired
	BabyrecordService babyrecordSvc;
	
	@Autowired
	ClinicRepository clinicRepository;
	
	public BabyrecordFrontendController(BabyhandbookService babyhandbookSvc, MemberService memberSvc,
			BabyrecordService babyrecordSvc) {
		this.babyhandbookSvc = babyhandbookSvc;
		this.memberSvc = memberSvc;
		this.babyrecordSvc = babyrecordSvc;
	}

	@ModelAttribute("/babyrecord/babyhandbookMap")
	protected Map<String, String> referenceMapData() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("男", "男");
		map.put("女", "女");
		return map;
	}

	@GetMapping("/babyrecord/full-grid-left-br")
	public String showBabyrecordPage(@RequestParam(required = false, defaultValue = "babyrecord") String tab,
			@RequestParam(name = "babyhandbookid", required = false) Integer babyhandbookid, Model model,
			HttpSession session) {

		// 檢查登入
		MemberVO member = (MemberVO) session.getAttribute("member");
		if (member == null) {
			return "redirect:/shop/login";
		}
		
		 // <<< 新增：側邊欄需要的「最新日記」（三筆） >>>
	    model.addAttribute("latestDiary",
	            diaryEntryService.latest3(member.getMemberId()));
	  
        // 如果會員還沒選手冊id,找最新的手冊
	    if (babyhandbookid == null) {
	        BabyhandbookVO latest = babyhandbookSvc.findLatestByMemberId(member.getMemberId());
	        if (latest != null) {
	            babyhandbookid = latest.getBabyhandbookid(); 
	        }
	    }
	    
		if (babyhandbookid != null) {  

			// 取得手冊
			BabyhandbookVO babyhandbook = babyhandbookSvc.getOneBabyhandbook(babyhandbookid);
			model.addAttribute("babyhandbook", babyhandbook);

			List<BabyrecordVO> babyrecordList = babyrecordSvc.findByBabyhandbookId(babyhandbookid);
			model.addAttribute("babyrecord", babyrecordList);

			// Clinic Map
			Map<Integer, ClinicDto> clinicDtoMap = new HashMap<>();
			for (BabyrecordVO vo : babyrecordList) {
				Integer clinicId = vo.getClinicid();
				if (clinicId != null && !clinicDtoMap.containsKey(clinicId)) {
					try {
						ClinicDto dto = clinicRepository.findDtoById(clinicId);
						clinicDtoMap.put(clinicId, dto);
					} catch (Exception ex) {

					}
				}
			}

			model.addAttribute("clinicDtoMap", clinicDtoMap);
		}

		model.addAttribute("activeTab", tab);

		return "frontend/blog-full-then-grid-left-sidebar";
	}

	@GetMapping("/babyrecord/{id}/edit")
	public String editBabyrecord(@PathVariable("id") Integer id, Model model, HttpSession session) {

		MemberVO member = (MemberVO) session.getAttribute("member");
		if (member == null) {
			return "redirect:/shop/login";
		}

		BabyrecordVO babyrecord = babyrecordSvc.getOneBabyrecord(id);
		BabyhandbookVO handbook = babyrecord.getBabyhandbook();

		// 檢查手冊是否存在，且屬於當前會員
		if (handbook == null || !handbook.getMember().getMemberId().equals(member.getMemberId())) {
			return "redirect:/blog/full-grid-left-br";
		}

		model.addAttribute("babyrecordVO", babyrecord);
		model.addAttribute("babyhandbookVO", handbook);

		return "frontend/u/baby/babyrecord-edit";
	}

	@PostMapping("/babyrecord/{id}/delete")
	public String deleteBabyrecord(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {

		// 取得 babyhandbookid
		BabyrecordVO existing = babyrecordSvc.getOneBabyrecord(id);
		Integer handbookId = existing.getBabyhandbook().getBabyhandbookid();

		// 刪除紀錄
		babyrecordSvc.deleteBabyrecord(id);

		// 帶成功訊息
		redirectAttributes.addFlashAttribute("message", "已成功刪除紀錄！");

		return "redirect:/u/baby/babyrecord/full-grid-left-br?tab=babyrecord&babyhandbookid=" + handbookId;
	}

	@PostMapping("/babyrecord/{id}/edit")
	public String updateBabyrecord(@PathVariable("id") Integer babyrecordid,
			@Valid @ModelAttribute("babyrecordVO") BabyrecordVO babyrecordVO, BindingResult result,
			@RequestParam("up") MultipartFile file, HttpSession session, RedirectAttributes redirectAttributes,
			Model model) {
		MemberVO member = (MemberVO) session.getAttribute("member");
		if (member == null) {
			return "redirect:/shop/login";
		}

		// 驗證有錯誤 → 返回表單
		if (result.hasErrors()) {
			if (babyrecordVO.getBabyhandbook() != null) {
				model.addAttribute("babyhandbookVO", babyrecordVO.getBabyhandbook());
			}

			model.addAttribute("babyrecordVO", babyrecordVO);
			return "frontend/u/baby/babyrecord-edit";
		}

		// 驗證是否為該會員的手冊
		BabyrecordVO existing = babyrecordSvc.getOneBabyrecord(babyrecordid);
		if (existing == null) {
			return "redirect:/blog/full-grid-left-br";
		}

		// 更新資料
		existing.setBabyhandbook(babyrecordVO.getBabyhandbook());
		existing.setBabyweek(babyrecordVO.getBabyweek());
		existing.setVisitdate(babyrecordVO.getVisitdate());
		existing.setClinicid(babyrecordVO.getClinicid());
		existing.setBodycondition(babyrecordVO.getBodycondition());
		existing.setNextcheckdate(babyrecordVO.getNextcheckdate());
		existing.setNextreminder(babyrecordVO.getNextreminder());
		existing.setHeight(babyrecordVO.getHeight());
		existing.setWeight(babyrecordVO.getWeight());
		existing.setHc(babyrecordVO.getHc());
		existing.setBabyrecordfiles(babyrecordVO.getBabyrecordfiles());

		// 處理圖片上傳（可選）
		if (file != null && !file.isEmpty()) {
			try {
				existing.setBabyrecordfiles(file.getBytes());
			} catch (IOException e) {
				e.printStackTrace(); // 可換成 log
			}
		}

		// 呼叫 Service 更新
		babyrecordSvc.updateBabyrecord(existing);

		// 回傳或重導
		redirectAttributes.addFlashAttribute("success", "手冊更新成功！");
		return "redirect:/u/baby/babyrecord/full-grid-left-br?tab=babyrecord&babyhandbookid="
				+ existing.getBabyhandbook().getBabyhandbookid();

	}

	@GetMapping("/babyrecord/photo/{id}")
	public ResponseEntity<byte[]> getPhoto(@PathVariable("id") Integer id) {
		BabyrecordVO babyrecord = babyrecordSvc.getOneBabyrecord(id);
		byte[] imageData = babyrecord.getBabyrecordfiles();

		if (imageData == null || imageData.length == 0) {
//			return ResponseEntity.notFound().build();
			String defaultImageBase64 = "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";
			imageData = Base64.getDecoder().decode(defaultImageBase64);
		
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG); // 或改成 IMAGE_PNG
		return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
	}

	@PostMapping("/babyrecord-add")
	public String addBabyrecord(@Valid @ModelAttribute("babyrecordVO") BabyrecordVO babyrecordVO, BindingResult result,
			@RequestParam("up") MultipartFile file, HttpSession session, RedirectAttributes redirectAttributes,
			Model model) {

		MemberVO member = (MemberVO) session.getAttribute("member");
		if (member == null) {
			return "redirect:/shop/login";
		}

		// 設定會員給 babyhandbook
		if (babyrecordVO.getBabyhandbook() != null) {
			babyrecordVO.getBabyhandbook().setMember(member);
		}

		// 驗證錯誤：回原本的頁面顯示錯誤訊息
		if (result.hasErrors()) {
			model.addAttribute("error", "請修正表單錯誤");
			model.addAttribute("babyhandbook", babyrecordVO.getBabyhandbook());
			model.addAttribute("babyrecordVO", babyrecordVO);
			return "frontend/u/baby/babyrecord-add";
		}

		// 上傳圖片
		if (!file.isEmpty()) {
			try {
				babyrecordVO.setBabyrecordfiles(file.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

//		if (babyrecordVO.getClinic() != null && babyrecordVO.getClinic().getClinicName() != null) {
//			Optional<Clinic> clinicOpt = clinicRepository
//					.findByClinicName(babyrecordVO.getClinic().getClinicName().trim());
//			if (clinicOpt.isPresent()) {
//				babyrecordVO.setClinicid(clinicOpt.get().getClinicId()); // ✅ 存正確 FK
//			} else {
//				result.rejectValue("clinic.clinicName", "clinic.notfound", "找不到對應的院所名稱");
//				model.addAttribute("babyhandbook", babyrecordVO.getBabyhandbook());
//				return "frontend/u/baby/babyrecord-add";
//			}
//		}

		// 儲存資料
		babyrecordSvc.addBabyrecord(babyrecordVO);

		// 成功提示 + 導回原頁
		redirectAttributes.addFlashAttribute("success", "成功新增寶寶紀錄！");
		Integer handbookid = babyrecordVO.getBabyhandbook().getBabyhandbookid();
		return "redirect:/u/baby/babyrecord/full-grid-left-br?tab=babyrecord&babyhandbookid=" + handbookid;

	}

	@GetMapping("/babyrecord-add")
	public String showAddBabyrecordForm(@RequestParam("babyhandbookid") Integer babyhandbookid, Model model,
			HttpSession session) {

		MemberVO member = (MemberVO) session.getAttribute("member");
		if (member == null) {
			return "redirect:/shop/login";
		}

		BabyhandbookVO handbook = babyhandbookSvc.getOneBabyhandbook(babyhandbookid);
		if (handbook == null || !handbook.getMember().getMemberId().equals(member.getMemberId())) {
			return "redirect:/u/baby/babyrecord/full-grid-left-br";
		}

		BabyrecordVO babyrecordVO = new BabyrecordVO();
		babyrecordVO.setBabyhandbook(handbook);

		model.addAttribute("babyrecordVO", babyrecordVO);
		model.addAttribute("babyhandbook", handbook);

		return "frontend/u/baby/babyrecord-add";
	}

	@GetMapping("baby-chart/full-grid-left-ch")
	public String showBabyrecordChart(@RequestParam(value = "babyhandbookid", required = false) Integer babyhandbookid,
			Model model, HttpSession session) {

	    MemberVO member = (MemberVO) session.getAttribute("member");
	    if (member == null) {
	        return "redirect:/shop/login";
	    }
	    // <<< 新增：側邊欄需要的「最新日記」（三筆） >>>
	    model.addAttribute("latestDiary",
	            diaryEntryService.latest3(member.getMemberId()));
	    
	    // 如果會員還沒選手冊id,找最新的手冊
	    if (babyhandbookid == null) {
	        BabyhandbookVO latest = babyhandbookSvc.findLatestByMemberId(member.getMemberId());
	        if (latest != null) {
	            babyhandbookid = latest.getBabyhandbookid(); 
	        } else {
	            return "redirect:/u/baby/babyhandbook-add";
	        }
	    }
	    
	    // 取得手冊資訊
	    BabyhandbookVO babyhandbook = babyhandbookSvc.getOneBabyhandbook(babyhandbookid);
	    
	    // 沒有手冊或不是此會員的
	    if (babyhandbook == null || !babyhandbook.getMember().getMemberId().equals(member.getMemberId())) {
	        return "redirect:/blog/u/baby/babyhandbook-add";
	        
	    }
	    
	    model.addAttribute("babyhandbook", babyhandbook);


//		// 如果會員還沒選手冊id,找最新的手冊
//		if (babyhandbookid == null) {
//			BabyhandbookVO latest = babyhandbookSvc.findLatestByMemberId(member.getMemberId());
//			if (latest != null) {
//				babyhandbookid = latest.getBabyhandbookid();
//			} else {
//				return "redirect:/u/baby/babyhandbook-add";
//			}
//		}
//
//		// 取得手冊資訊
//		BabyhandbookVO babyhandbook = babyhandbookSvc.getOneBabyhandbook(babyhandbookid);
//
//		// 沒有手冊或不是此會員的
//		if (babyhandbook == null || !babyhandbook.getMember().getMemberId().equals(member.getMemberId())) {
//			return "redirect:/blog/u/baby/babyhandbook-add";
//
//		}

		model.addAttribute("babyhandbook", babyhandbook);

		// 取得紀錄資料
		List<BabyrecordVO> records = babyrecordSvc.findByBabyhandbookId(babyhandbookid);
		List<Integer> babyweek = records.stream().map(BabyrecordVO::getBabyweek).collect(Collectors.toList());
		List<BigDecimal> height = records.stream().map(BabyrecordVO::getHeight).collect(Collectors.toList());
		List<BigDecimal> weight = records.stream().map(BabyrecordVO::getWeight).collect(Collectors.toList());
		List<BigDecimal> hc = records.stream().map(BabyrecordVO::getHc).collect(Collectors.toList());

		model.addAttribute("activeTab", "baby-chart");
		model.addAttribute("tab", "baby-chart");
		model.addAttribute("babyweek", babyweek);
		model.addAttribute("height", height);
		model.addAttribute("weight", weight);
		model.addAttribute("hc", hc);

		// 根據性別決定參考曲線
		String gender = babyhandbook.getBabygender();

		Map<String, List<BigDecimal>> heightPercentile = "男".equals(gender) ? getMaleHeightPercentile()
				: getFemaleHeightPercentile();
		Map<String, List<BigDecimal>> weightPercentile = "男".equals(gender) ? getMaleWeightPercentile()
				: getFemaleWeightPercentile();
		Map<String, List<BigDecimal>> hcPercentile = "男".equals(gender) ? getMaleHcPercentile()
				: getFemaleHcPercentile();

		List<Double> ageWeeks = List.of(0.0, 26.0, 52.0, 78.0);
		model.addAttribute("ageWeeks", ageWeeks);

		model.addAttribute("heightPercentile", heightPercentile);
		model.addAttribute("weightPercentile", weightPercentile);
		model.addAttribute("hcPercentile", hcPercentile);

//	    return "frontend/u/baby/baby-chart"; 
		return "frontend/blog-full-then-grid-left-sidebar";
	}

	// 以下是hardcode

	private Map<String, List<BigDecimal>> getMaleHeightPercentile() {
		Map<String, List<BigDecimal>> map = new HashMap<>();
		map.put("P3", List.of(BigDecimal.valueOf(46.3), BigDecimal.valueOf(63.6), BigDecimal.valueOf(71.3),
				BigDecimal.valueOf(77.2)));
		map.put("P15", List.of(BigDecimal.valueOf(47.9), BigDecimal.valueOf(65.4), BigDecimal.valueOf(73.3),
				BigDecimal.valueOf(79.5)));
		map.put("P50", List.of(BigDecimal.valueOf(49.9), BigDecimal.valueOf(67.6), BigDecimal.valueOf(75.7),
				BigDecimal.valueOf(82.3)));
		map.put("P85", List.of(BigDecimal.valueOf(51.8), BigDecimal.valueOf(69.8), BigDecimal.valueOf(78.2),
				BigDecimal.valueOf(85.1)));
		map.put("P97", List.of(BigDecimal.valueOf(53.4), BigDecimal.valueOf(71.6), BigDecimal.valueOf(80.2),
				BigDecimal.valueOf(87.3)));
		return map;
	}

	private Map<String, List<BigDecimal>> getMaleWeightPercentile() {
		Map<String, List<BigDecimal>> map = new HashMap<>();
		map.put("P3", List.of(BigDecimal.valueOf(2.5), BigDecimal.valueOf(6.4), BigDecimal.valueOf(7.8),
				BigDecimal.valueOf(8.9)));
		map.put("P15", List.of(BigDecimal.valueOf(2.9), BigDecimal.valueOf(7.1), BigDecimal.valueOf(8.6),
				BigDecimal.valueOf(9.7)));
		map.put("P50", List.of(BigDecimal.valueOf(3.3), BigDecimal.valueOf(7.9), BigDecimal.valueOf(9.6),
				BigDecimal.valueOf(10.9)));
		map.put("P85", List.of(BigDecimal.valueOf(3.9), BigDecimal.valueOf(8.9), BigDecimal.valueOf(10.8),
				BigDecimal.valueOf(12.3)));
		map.put("P97", List.of(BigDecimal.valueOf(4.3), BigDecimal.valueOf(9.7), BigDecimal.valueOf(11.8),
				BigDecimal.valueOf(13.5)));
		return map;
	}

	private Map<String, List<BigDecimal>> getMaleHcPercentile() {
		Map<String, List<BigDecimal>> map = new HashMap<>();
		map.put("P3", List.of(BigDecimal.valueOf(32.0), BigDecimal.valueOf(41.0), BigDecimal.valueOf(43.5),
				BigDecimal.valueOf(44.7)));
		map.put("P15", List.of(BigDecimal.valueOf(33.0), BigDecimal.valueOf(42.0), BigDecimal.valueOf(44.7),
				BigDecimal.valueOf(46.0)));
		map.put("P50", List.of(BigDecimal.valueOf(34.5), BigDecimal.valueOf(43.0), BigDecimal.valueOf(46.0),
				BigDecimal.valueOf(47.4)));
		map.put("P85", List.of(BigDecimal.valueOf(36.0), BigDecimal.valueOf(44.5), BigDecimal.valueOf(47.5),
				BigDecimal.valueOf(48.7)));
		map.put("P97", List.of(BigDecimal.valueOf(37.0), BigDecimal.valueOf(45.5), BigDecimal.valueOf(48.5),
				BigDecimal.valueOf(50.0)));
		return map;
	}

	private Map<String, List<BigDecimal>> getFemaleHeightPercentile() {
		Map<String, List<BigDecimal>> map = new HashMap<>();
		map.put("P3", List.of(BigDecimal.valueOf(45.6), BigDecimal.valueOf(61.5), BigDecimal.valueOf(69.2),
				BigDecimal.valueOf(75.2)));
		map.put("P15", List.of(BigDecimal.valueOf(47.2), BigDecimal.valueOf(63.4), BigDecimal.valueOf(71.3),
				BigDecimal.valueOf(77.7)));
		map.put("P50", List.of(BigDecimal.valueOf(49.1), BigDecimal.valueOf(65.7), BigDecimal.valueOf(74.0),
				BigDecimal.valueOf(80.7)));
		map.put("P85", List.of(BigDecimal.valueOf(51.1), BigDecimal.valueOf(68.1), BigDecimal.valueOf(76.7),
				BigDecimal.valueOf(83.7)));
		map.put("P97", List.of(BigDecimal.valueOf(52.7), BigDecimal.valueOf(70.0), BigDecimal.valueOf(78.9),
				BigDecimal.valueOf(86.2)));
		return map;
	}

	private Map<String, List<BigDecimal>> getFemaleWeightPercentile() {
		Map<String, List<BigDecimal>> map = new HashMap<>();
		map.put("P3", List.of(BigDecimal.valueOf(2.4), BigDecimal.valueOf(5.8), BigDecimal.valueOf(7.1),
				BigDecimal.valueOf(8.2)));
		map.put("P15", List.of(BigDecimal.valueOf(2.8), BigDecimal.valueOf(6.4), BigDecimal.valueOf(7.9),
				BigDecimal.valueOf(9.0)));
		map.put("P50", List.of(BigDecimal.valueOf(3.2), BigDecimal.valueOf(7.3), BigDecimal.valueOf(8.9),
				BigDecimal.valueOf(10.2)));
		map.put("P85", List.of(BigDecimal.valueOf(3.7), BigDecimal.valueOf(8.3), BigDecimal.valueOf(10.2),
				BigDecimal.valueOf(11.6)));
		map.put("P97", List.of(BigDecimal.valueOf(4.2), BigDecimal.valueOf(9.2), BigDecimal.valueOf(11.3),
				BigDecimal.valueOf(13.0)));
		return map;
	}

	private Map<String, List<BigDecimal>> getFemaleHcPercentile() {
		Map<String, List<BigDecimal>> map = new HashMap<>();
		map.put("P3", List.of(BigDecimal.valueOf(31.8), BigDecimal.valueOf(39.8), BigDecimal.valueOf(42.2),
				BigDecimal.valueOf(43.5)));
		map.put("P15", List.of(BigDecimal.valueOf(32.8), BigDecimal.valueOf(40.8), BigDecimal.valueOf(43.5),
				BigDecimal.valueOf(44.8)));
		map.put("P50", List.of(BigDecimal.valueOf(34.0), BigDecimal.valueOf(42.1), BigDecimal.valueOf(45.0),
				BigDecimal.valueOf(46.2)));
		map.put("P85", List.of(BigDecimal.valueOf(35.0), BigDecimal.valueOf(43.5), BigDecimal.valueOf(46.3),
				BigDecimal.valueOf(47.8)));
		map.put("P97", List.of(BigDecimal.valueOf(36.0), BigDecimal.valueOf(44.5), BigDecimal.valueOf(47.5),
				BigDecimal.valueOf(48.9)));
		return map;
	}

}
