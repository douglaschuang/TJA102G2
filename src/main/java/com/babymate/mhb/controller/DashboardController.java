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
			@RequestParam(value = "mhbId", required = false) Integer mhbId, HttpSession session, Model model) {

		model.addAttribute("tab", tab);

		// 用 "member"（並保留舊 key 的容錯）
		MemberVO login = (MemberVO) session.getAttribute("member");
		if (login == null) {
			login = (MemberVO) session.getAttribute("loginMember");
		}

		// 側欄最新日記
		if (login != null) {
			model.addAttribute("latestDiary", diaryEntryService.latest3(login.getMemberId()));
		} else {
			model.addAttribute("latestDiary", Collections.emptyList());
		}

		boolean needMhb = "mhb".equals(tab) || "mhb-records".equals(tab) || "mhb-charts".equals(tab)
				|| "todos".equals(tab);

		// 優先用 mhbId 直接載那一本；沒有才用會員找「最新一本」
		MhbVO mhb = null;
		if (needMhb) {
			if (mhbId != null) {
				// 如果你有 findActiveById(...) 就用它；沒有就 getOneMhb(mhbId)
				mhb = mhbService.getOneMhb(mhbId);
			}
			if (mhb == null && login != null) {
				mhb = mhbService.findActiveByMemberId(login.getMemberId());
			}
			model.addAttribute("mhb", mhb);

			if (mhb != null) {
				model.addAttribute("pregnancyWeek", calcPregnancyWeek(mhb));
			}
		}

		// 懷孕紀錄列表
		if ("mhb-records".equals(tab) && mhb != null) {
			List<PregnancyRecord> records = prService.findByMhbId(mhb.getMotherHandbookId());
			model.addAttribute("records", records);
		}

		// 圖表資料
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

		// 代辦事項清單
		if ("todos".equals(tab) && mhb != null) {
			model.addAttribute("todos", mhbTodoService.listByMhb(mhb.getMotherHandbookId())); // 你的查詢方法名稱依實作調整
		}

		// 相簿 / 日記（需登入）
		if ("album".equals(tab) && login != null) {
			model.addAttribute("photos", albumPhotoService.findByMember(login.getMemberId()));
		} else if ("diary".equals(tab) && login != null) {
			model.addAttribute("entries", diaryEntryService.findByMember(login.getMemberId()));
		}

		// 修正樣板路徑
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
