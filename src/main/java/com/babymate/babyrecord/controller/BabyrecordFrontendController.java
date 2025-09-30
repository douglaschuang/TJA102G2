package com.babymate.babyrecord.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
//
@Controller
@RequestMapping("/u/baby")
public class BabyrecordFrontendController {

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
	public String updateBabyrecord(
	        @PathVariable ("id") Integer babyrecordid,
	        @Valid @ModelAttribute("babyrecordVO") BabyrecordVO babyrecordVO,
	        BindingResult result,
	        @RequestParam("up") MultipartFile file,
	        HttpSession session,
	        RedirectAttributes redirectAttributes,
	        Model model
	) {
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
	    return "redirect:/u/baby/babyrecord/full-grid-left-br?tab=babyrecord&babyhandbookid=" + existing.getBabyhandbook().getBabyhandbookid();
	    
	}
	
	@GetMapping("/babyrecord/photo/{id}")
	public ResponseEntity<byte[]> getPhoto(@PathVariable("id") Integer id) {
	    BabyrecordVO babyrecord = babyrecordSvc.getOneBabyrecord(id);
	    byte[] imageData = babyrecord.getBabyrecordfiles();

	    if (imageData == null || imageData.length == 0) {
	        return ResponseEntity.notFound().build();
	    }

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.IMAGE_JPEG); // 或改成 IMAGE_PNG
	    return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
	}

	
	
	@PostMapping("/babyrecord-add")
	public String addBabyrecord(
	        @Valid @ModelAttribute("babyrecordVO") BabyrecordVO babyrecordVO,
	        BindingResult result,
	        @RequestParam("up") MultipartFile file,
	        HttpSession session,
	        RedirectAttributes redirectAttributes,
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

	    // 儲存資料
	    babyrecordSvc.addBabyrecord(babyrecordVO);

	    // 成功提示 + 導回原頁
	    redirectAttributes.addFlashAttribute("success", "成功新增寶寶紀錄！");
	    Integer handbookid = babyrecordVO.getBabyhandbook().getBabyhandbookid();
	    return "redirect:/u/baby/babyrecord/full-grid-left-br?tab=babyrecord&babyhandbookid=" + handbookid;

	}
	
	
	@GetMapping("/babyrecord-add")
	public String showAddBabyrecordForm(@RequestParam("babyhandbookid") Integer babyhandbookid,
	                                    Model model, HttpSession session) {

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
	                                  Model model,
	                                  HttpSession session) {

	    MemberVO member = (MemberVO) session.getAttribute("member");
	    if (member == null) {
	        return "redirect:/shop/login";
	    }
	    
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

	    Map<String, List<BigDecimal>> heightPercentile = "男".equals(gender) ? getMaleHeightPercentile() : getFemaleHeightPercentile();
	    Map<String, List<BigDecimal>> weightPercentile = "男".equals(gender) ? getMaleWeightPercentile() : getFemaleWeightPercentile();
	    Map<String, List<BigDecimal>> hcPercentile = "男".equals(gender) ? getMaleHcPercentile() : getFemaleHcPercentile();

	    
	    List<Double> ageWeeks = List.of(0.0, 26.0, 52.0, 78.0);
	    model.addAttribute("ageWeeks", ageWeeks);
	    
	    model.addAttribute("heightPercentile", heightPercentile);
	    model.addAttribute("weightPercentile", weightPercentile);
	    model.addAttribute("hcPercentile", hcPercentile);

//	    return "frontend/u/baby/baby-chart"; 
	    return "frontend/blog-full-then-grid-left-sidebar";
	}

	//以下是hardcode
	
	private Map<String, List<BigDecimal>> getMaleHeightPercentile() {
	    Map<String, List<BigDecimal>> map = new HashMap<>();
	    map.put("P3", List.of(
	        BigDecimal.valueOf(46.3),
	        BigDecimal.valueOf(63.6),
	        BigDecimal.valueOf(71.3),
	        BigDecimal.valueOf(77.2)
	    ));
	    map.put("P15", List.of(
	        BigDecimal.valueOf(47.9),
	        BigDecimal.valueOf(65.4),
	        BigDecimal.valueOf(73.3),
	        BigDecimal.valueOf(79.5)
	    ));
	    map.put("P50", List.of(
	        BigDecimal.valueOf(49.9),
	        BigDecimal.valueOf(67.6),
	        BigDecimal.valueOf(75.7),
	        BigDecimal.valueOf(82.3)
	    ));
	    map.put("P85", List.of(
	        BigDecimal.valueOf(51.8),
	        BigDecimal.valueOf(69.8),
	        BigDecimal.valueOf(78.2),
	        BigDecimal.valueOf(85.1)
	    ));
	    map.put("P97", List.of(
	        BigDecimal.valueOf(53.4),
	        BigDecimal.valueOf(71.6),
	        BigDecimal.valueOf(80.2),
	        BigDecimal.valueOf(87.3)
	    ));
	    return map;
	}

	private Map<String, List<BigDecimal>> getMaleWeightPercentile() {
	    Map<String, List<BigDecimal>> map = new HashMap<>();
	    map.put("P3", List.of(
	        BigDecimal.valueOf(2.5),
	        BigDecimal.valueOf(6.4),
	        BigDecimal.valueOf(7.8),
	        BigDecimal.valueOf(8.9)
	    ));
	    map.put("P15", List.of(
	        BigDecimal.valueOf(2.9),
	        BigDecimal.valueOf(7.1),
	        BigDecimal.valueOf(8.6),
	        BigDecimal.valueOf(9.7)
	    ));
	    map.put("P50", List.of(
	        BigDecimal.valueOf(3.3),
	        BigDecimal.valueOf(7.9),
	        BigDecimal.valueOf(9.6),
	        BigDecimal.valueOf(10.9)
	    ));
	    map.put("P85", List.of(
	        BigDecimal.valueOf(3.9),
	        BigDecimal.valueOf(8.9),
	        BigDecimal.valueOf(10.8),
	        BigDecimal.valueOf(12.3)
	    ));
	    map.put("P97", List.of(
	        BigDecimal.valueOf(4.3),
	        BigDecimal.valueOf(9.7),
	        BigDecimal.valueOf(11.8),
	        BigDecimal.valueOf(13.5)
	    ));
	    return map;
	}

	private Map<String, List<BigDecimal>> getMaleHcPercentile() {
	    Map<String, List<BigDecimal>> map = new HashMap<>();
	    map.put("P3", List.of(
	        BigDecimal.valueOf(32.0),
	        BigDecimal.valueOf(41.0),
	        BigDecimal.valueOf(43.5),
	        BigDecimal.valueOf(44.7)
	    ));
	    map.put("P15", List.of(
	        BigDecimal.valueOf(33.0),
	        BigDecimal.valueOf(42.0),
	        BigDecimal.valueOf(44.7),
	        BigDecimal.valueOf(46.0)
	    ));
	    map.put("P50", List.of(
	        BigDecimal.valueOf(34.5),
	        BigDecimal.valueOf(43.0),
	        BigDecimal.valueOf(46.0),
	        BigDecimal.valueOf(47.4)
	    ));
	    map.put("P85", List.of(
	        BigDecimal.valueOf(36.0),
	        BigDecimal.valueOf(44.5),
	        BigDecimal.valueOf(47.5),
	        BigDecimal.valueOf(48.7)
	    ));
	    map.put("P97", List.of(
	        BigDecimal.valueOf(37.0),
	        BigDecimal.valueOf(45.5),
	        BigDecimal.valueOf(48.5),
	        BigDecimal.valueOf(50.0)
	    ));
	    return map;
	}

	private Map<String, List<BigDecimal>> getFemaleHeightPercentile() {
	    Map<String, List<BigDecimal>> map = new HashMap<>();
	    map.put("P3", List.of(
	        BigDecimal.valueOf(45.6),
	        BigDecimal.valueOf(61.5),
	        BigDecimal.valueOf(69.2),
	        BigDecimal.valueOf(75.2)
	    ));
	    map.put("P15", List.of(
	        BigDecimal.valueOf(47.2),
	        BigDecimal.valueOf(63.4),
	        BigDecimal.valueOf(71.3),
	        BigDecimal.valueOf(77.7)
	    ));
	    map.put("P50", List.of(
	        BigDecimal.valueOf(49.1),
	        BigDecimal.valueOf(65.7),
	        BigDecimal.valueOf(74.0),
	        BigDecimal.valueOf(80.7)
	    ));
	    map.put("P85", List.of(
	        BigDecimal.valueOf(51.1),
	        BigDecimal.valueOf(68.1),
	        BigDecimal.valueOf(76.7),
	        BigDecimal.valueOf(83.7)
	    ));
	    map.put("P97", List.of(
	        BigDecimal.valueOf(52.7),
	        BigDecimal.valueOf(70.0),
	        BigDecimal.valueOf(78.9),
	        BigDecimal.valueOf(86.2)
	    ));
	    return map;
	}

	private Map<String, List<BigDecimal>> getFemaleWeightPercentile() {
	    Map<String, List<BigDecimal>> map = new HashMap<>();
	    map.put("P3", List.of(
	        BigDecimal.valueOf(2.4),
	        BigDecimal.valueOf(5.8),
	        BigDecimal.valueOf(7.1),
	        BigDecimal.valueOf(8.2)
	    ));
	    map.put("P15", List.of(
	        BigDecimal.valueOf(2.8),
	        BigDecimal.valueOf(6.4),
	        BigDecimal.valueOf(7.9),
	        BigDecimal.valueOf(9.0)
	    ));
	    map.put("P50", List.of(
	        BigDecimal.valueOf(3.2),
	        BigDecimal.valueOf(7.3),
	        BigDecimal.valueOf(8.9),
	        BigDecimal.valueOf(10.2)
	    ));
	    map.put("P85", List.of(
	        BigDecimal.valueOf(3.7),
	        BigDecimal.valueOf(8.3),
	        BigDecimal.valueOf(10.2),
	        BigDecimal.valueOf(11.6)
	    ));
	    map.put("P97", List.of(
	        BigDecimal.valueOf(4.2),
	        BigDecimal.valueOf(9.2),
	        BigDecimal.valueOf(11.3),
	        BigDecimal.valueOf(13.0)
	    ));
	    return map;
	}

	private Map<String, List<BigDecimal>> getFemaleHcPercentile() {
	    Map<String, List<BigDecimal>> map = new HashMap<>();
	    map.put("P3", List.of(
	        BigDecimal.valueOf(31.8),
	        BigDecimal.valueOf(39.8),
	        BigDecimal.valueOf(42.2),
	        BigDecimal.valueOf(43.5)
	    ));
	    map.put("P15", List.of(
	        BigDecimal.valueOf(32.8),
	        BigDecimal.valueOf(40.8),
	        BigDecimal.valueOf(43.5),
	        BigDecimal.valueOf(44.8)
	    ));
	    map.put("P50", List.of(
	        BigDecimal.valueOf(34.0),
	        BigDecimal.valueOf(42.1),
	        BigDecimal.valueOf(45.0),
	        BigDecimal.valueOf(46.2)
	    ));
	    map.put("P85", List.of(
	        BigDecimal.valueOf(35.0),
	        BigDecimal.valueOf(43.5),
	        BigDecimal.valueOf(46.3),
	        BigDecimal.valueOf(47.8)
	    ));
	    map.put("P97", List.of(
	        BigDecimal.valueOf(36.0),
	        BigDecimal.valueOf(44.5),
	        BigDecimal.valueOf(47.5),
	        BigDecimal.valueOf(48.9)
	    ));
	    return map;
	}

	
	
}


