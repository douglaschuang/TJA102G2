package com.babymate.mhbfrontend.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.babymate.member.model.MemberVO;
import com.babymate.mhb.model.MhbService;
import com.babymate.mhb.model.MhbVO;
import com.babymate.preg.model.PregnancyRecord;
import com.babymate.preg.model.PregnancyRecordService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/u/preg")
public class FrontPregController {

    private final PregnancyRecordService prService;
    private final MhbService mhbService;

    public FrontPregController(PregnancyRecordService prService, MhbService mhbService) {
        this.prService = prService;
        this.mhbService = mhbService;
    }

    // 顯示新增表單
    @GetMapping("/new")
    public String newForm(@RequestParam(value = "mhbId", required = false) Integer mhbId,
                          HttpSession session, Model model) {
        MemberVO login = (MemberVO) session.getAttribute("loginMember");
        if (login == null) return "redirect:/shop/login";

        MhbVO mhb = (mhbId != null) ? mhbService.getOneMhb(mhbId)
                                    : mhbService.findActiveByMemberId(login.getMemberId());
        // 擁有權檢查
        if (mhb == null || !mhb.getMemberId().equals(login.getMemberId())) {
            return "redirect:/blog/full-grid-left?tab=mhb"; // 或顯示錯誤訊息
        }

        PregnancyRecord rec = new PregnancyRecord();
        rec.setMotherHandbookId(mhb.getMotherHandbookId());
        rec.setVisitDate(LocalDate.now());
        rec.setPregnancyWeek(calcWeekOnDate(mhb, rec.getVisitDate()));

        model.addAttribute("mhb", mhb);
        model.addAttribute("record", rec);
        return "frontend/u/preg/new_record";
    }

    // 接收建立
    @PostMapping
    public String create(@ModelAttribute("record") PregnancyRecord record,
                         @RequestParam("mhbId") Integer mhbId,
                         HttpSession session, Model model) {
        MemberVO login = (MemberVO) session.getAttribute("loginMember");
        if (login == null) return "redirect:/shop/login";

        MhbVO mhb = mhbService.getOneMhb(mhbId);
        if (mhb == null || !mhb.getMemberId().equals(login.getMemberId())) {
            return "redirect:/blog/full-grid-left?tab=mhb"; // 非本人禁止寫入
        }

        if (record.getVisitDate() == null) record.setVisitDate(LocalDate.now());
        record.setMotherHandbookId(mhbId);
        record.setPregnancyWeek(calcWeekOnDate(mhb, record.getVisitDate()));

        prService.save(record);
        return "redirect:/blog/full-grid-left?tab=mhb-records&mhbId=" + mhbId;
    }

    // 以指定日期計算孕週
    private Integer calcWeekOnDate(MhbVO mhb, LocalDate date) {
        if (date == null) return null;
        LocalDate lmp = mhb.getLastMcDate();
        LocalDate edd = mhb.getExpectedBirthDate();
        if (lmp == null && edd != null) lmp = edd.minusWeeks(40);
        if (lmp == null) return null;
        long days = ChronoUnit.DAYS.between(lmp, date);
        return days < 0 ? null : (int)(days / 7) + 1;
    }
}
