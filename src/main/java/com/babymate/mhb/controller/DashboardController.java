package com.babymate.mhb.controller;

import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.babymate.member.model.MemberVO;
import com.babymate.mhb.model.MhbService;
import com.babymate.mhb.model.MhbVO;
import com.babymate.preg.model.PregnancyRecord;
import com.babymate.preg.model.PregnancyRecordService;
import com.babymate.album.model.AlbumPhotoService;
import com.babymate.diary.model.DiaryEntryService;
import com.babymate.todo.model.MhbTodoService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/blog")
public class DashboardController {

	private final MhbService mhbService;
	private final PregnancyRecordService prService;
	private final AlbumPhotoService albumPhotoService;
	private final DiaryEntryService diaryEntryService;
	// 新增：代辦 service
	private final MhbTodoService mhbTodoService;

	public DashboardController(MhbService mhbService, PregnancyRecordService prService,
			AlbumPhotoService albumPhotoService, DiaryEntryService diaryEntryService, MhbTodoService mhbTodoService) {
		this.mhbService = mhbService;
		this.prService = prService;
		this.albumPhotoService = albumPhotoService;
		this.diaryEntryService = diaryEntryService;
		this.mhbTodoService = mhbTodoService;
	}

	@GetMapping("/full-grid-left")
	public String dashboard(@RequestParam(value = "tab", required = false) String tab,
	                        @RequestParam(value = "mhbId", required = false) Integer mhbId,
	                        HttpSession session, Model model) {

	    model.addAttribute("tab", tab);

	    MemberVO login = (MemberVO) session.getAttribute("member");
	    if (login == null) login = (MemberVO) session.getAttribute("loginMember");

	    if (login != null) {
	        model.addAttribute("latestDiary", diaryEntryService.latest3(login.getMemberId()));
	    } else {
	        model.addAttribute("latestDiary", Collections.emptyList());
	    }

	    boolean needMhb = "mhb".equals(tab) || "mhb-records".equals(tab) || "mhb-charts".equals(tab) || "todos".equals(tab);

	    MhbVO mhb = null;

	    if (needMhb && login != null) {
	        // ★ 新增：查出此會員的所有手冊清單，丟給頁面使用
	        // 建議用 updateTime DESC 或 id DESC 排序，最新在前
	        List<MhbVO> mhbs = mhbService.findByMemberIdOrderByUpdateTimeDesc(login.getMemberId());
	        model.addAttribute("mhbs", mhbs);

	        // 既有選擇邏輯：先看有沒有指定 mhbId；沒有就取清單第 1 本
	        if (mhbId != null) {
	            mhb = mhbs.stream()
	                      .filter(x -> Objects.equals(x.getMotherHandbookId(), mhbId))
	                      .findFirst()
	                      .orElse(null);
	        }
	        if (mhb == null && !mhbs.isEmpty()) {
	            mhb = mhbs.get(0);
	        }
	    } else if (needMhb) {
	        // 未登入的容錯：維持原本邏輯
	        if (mhbId != null) mhb = mhbService.getOneMhb(mhbId);
	    }

	    model.addAttribute("mhb", mhb);
	    if (mhb != null) {
	        model.addAttribute("pregnancyWeek", calcPregnancyWeek(mhb));
	    }

	    if ("mhb-records".equals(tab) && mhb != null) {
	        List<PregnancyRecord> records = prService.findByMhbId(mhb.getMotherHandbookId());
	        model.addAttribute("records", records);
	    }

	    if ("mhb-charts".equals(tab) && mhb != null) {
	        List<PregnancyRecord> records = prService.findByMhbId(mhb.getMotherHandbookId());
	        records.sort(Comparator.comparing(PregnancyRecord::getVisitDate));

	        List<String> labels = new ArrayList<>();
	        List<Double> weights = new ArrayList<>();
	        List<Integer> sps = new ArrayList<>();
	        List<Integer> dps = new ArrayList<>();
	        List<Integer> fhs = new ArrayList<>();

	        for (PregnancyRecord r : records) {
	            labels.add(r.getVisitDate().toString());
	            weights.add(r.getWeight() != null ? r.getWeight().doubleValue() : null);
	            sps.add(r.getSp());
	            dps.add(r.getDp());
	            fhs.add(parseFhsToInt(r.getFhs()));
	        }

	        model.addAttribute("chartLabels", labels);
	        model.addAttribute("chartWeights", weights);
	        model.addAttribute("chartSp", sps);
	        model.addAttribute("chartDp", dps);
	        model.addAttribute("chartFhs", fhs);
	    }

	    if ("todos".equals(tab) && mhb != null) {
	        model.addAttribute("todos", mhbTodoService.listByMhb(mhb.getMotherHandbookId()));
	    }

	    if ("album".equals(tab) && login != null) {
	        model.addAttribute("photos", albumPhotoService.findByMember(login.getMemberId()));
	    } else if ("diary".equals(tab) && login != null) {
	        model.addAttribute("entries", diaryEntryService.findByMember(login.getMemberId()));
	    }

	    return "frontend/blog-full-then-grid-left-sidebar";
	}


	private Integer calcPregnancyWeek(MhbVO mhb) {
		if (mhb == null)
			return null;
		LocalDate lmp = mhb.getLastMcDate();
		LocalDate edd = mhb.getExpectedBirthDate();
		if (lmp == null && edd != null)
			lmp = edd.minusWeeks(40);
		if (lmp == null)
			return null;
		long days = java.time.temporal.ChronoUnit.DAYS.between(lmp, LocalDate.now());
		return days < 0 ? null : (int) (days / 7) + 1;
	}

	private Integer parseFhsToInt(String f) {
		if (f == null)
			return null;
		java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{2,3})").matcher(f);
		return m.find() ? Integer.valueOf(m.group(1)) : null;
	}
}
