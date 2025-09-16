package com.babymate.mhb.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.babymate.member.model.MemberVO;
import com.babymate.mhb.model.MhbService;
import com.babymate.mhb.model.MhbVO;
import com.babymate.preg.model.PregnancyRecord;
import com.babymate.preg.model.PregnancyRecordService;

// 新增這兩個 import
import com.babymate.album.model.AlbumPhotoService;
import com.babymate.diary.model.DiaryEntryService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/blog")
public class DashboardController {

    private final MhbService mhbService;
    private final PregnancyRecordService prService;

    // 新增：相簿 / 日記 service
    private final AlbumPhotoService albumPhotoService;
    private final DiaryEntryService diaryEntryService;

    // 建構子注入，多兩個參數
    public DashboardController(MhbService mhbService,
                               PregnancyRecordService prService,
                               AlbumPhotoService albumPhotoService,
                               DiaryEntryService diaryEntryService) {
        this.mhbService = mhbService;
        this.prService = prService;
        this.albumPhotoService = albumPhotoService;
        this.diaryEntryService = diaryEntryService;
    }

    @GetMapping("/full-grid-left")
    public String dashboard(@RequestParam(value = "tab", required = false) String tab,
                            @RequestParam(value = "mhbId", required = false) Integer mhbId,
                            HttpSession session,
                            Model model) {

        model.addAttribute("tab", tab);

        // 先把 login 取出，下面各分支都可能會用到
        MemberVO login = (MemberVO) session.getAttribute("loginMember");
        
     // 🔹新增：側邊要顯示的「最新三篇日記」
        if (login != null) {
            model.addAttribute("latestDiary", diaryEntryService.latest3(login.getMemberId()));
        } else {
            model.addAttribute("latestDiary", java.util.Collections.emptyList());
        }

        boolean needMhb = "mhb".equals(tab)
                || "mhb-records".equals(tab)
                || "mhb-charts".equals(tab)
                || "todos".equals(tab);

        MhbVO mhb = null;
        if (needMhb && login != null) {
            if (mhbId != null) {
                mhb = mhbService.getOneMhb(mhbId);
            } else {
                // 這個方法請見下方補充，如果你尚未在 service/repo 實作
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

        // 圖表資料（日期/體重/血壓/胎心音）
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

        // 代辦（先空）
        if ("todos".equals(tab) && mhb != null) {
            model.addAttribute("todos", Collections.emptyList());
        }

        // 相簿 / 日記（需要登入）
        if ("album".equals(tab) && login != null) {
            model.addAttribute("photos", albumPhotoService.findByMember(login.getMemberId()));
        } else if ("diary".equals(tab) && login != null) {
            model.addAttribute("entries", diaryEntryService.findByMember(login.getMemberId()));
        }

        return "frontend/blog-full-then-grid-left-sidebar";
    }

    private Integer calcPregnancyWeek(MhbVO mhb) {
        if (mhb == null) return null;
        LocalDate lmp = mhb.getLastMcDate();
        LocalDate edd = mhb.getExpectedBirthDate();
        if (lmp == null && edd != null) lmp = edd.minusWeeks(40);
        if (lmp == null) return null;
        long days = java.time.temporal.ChronoUnit.DAYS.between(lmp, LocalDate.now());
        return days < 0 ? null : (int) (days / 7) + 1;
    }

    private Integer parseFhsToInt(String f) {
        if (f == null) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{2,3})").matcher(f);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }
}
