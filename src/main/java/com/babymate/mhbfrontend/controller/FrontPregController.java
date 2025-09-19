package com.babymate.mhbfrontend.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        MemberVO login = (MemberVO) session.getAttribute("member");
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
        MemberVO login = (MemberVO) session.getAttribute("member");
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
    
 // ====== 編輯表單 ======
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Integer id,
                           @RequestParam("mhbId") Integer mhbId,
                           HttpSession session, Model model) {
        MemberVO login = (MemberVO) session.getAttribute("member");
        if (login == null) return "redirect:/shop/login";

        var record = prService.getOne(id);
        var mhb = mhbService.getOneMhb(mhbId);
        // 擁有權 & 關聯檢查
        if (record == null || mhb == null
                || !mhb.getMemberId().equals(login.getMemberId())
                || !record.getMotherHandbookId().equals(mhb.getMotherHandbookId())) {
            return "redirect:/blog/full-grid-left?tab=mhb-records&mhbId=" + mhbId;
        }

        model.addAttribute("mhb", mhb);
        model.addAttribute("record", record);
        return "frontend/u/preg/edit_record"; // 你前台的編輯頁
    }

    // ====== 接收編輯 ======
    @PostMapping("/{id}/edit")
    public String update(@PathVariable("id") Integer id,
                         @RequestParam("mhbId") Integer mhbId,
                         @ModelAttribute PregnancyRecord form,
                         HttpSession session) {
        MemberVO login = (MemberVO) session.getAttribute("member");
        if (login == null) return "redirect:/shop/login";

        PregnancyRecord record = prService.getOne(id);
        MhbVO mhb = mhbService.getOneMhb(mhbId);
        if (record == null || mhb == null
                || !mhb.getMemberId().equals(login.getMemberId())
                || !record.getMotherHandbookId().equals(mhb.getMotherHandbookId())) {
            return "redirect:/blog/full-grid-left?tab=mhb-records&mhbId=" + mhbId;
        }

        // 更新允許的欄位
        record.setVisitDate(form.getVisitDate());
        record.setPregnancyWeek(form.getPregnancyWeek());
        record.setWeight(form.getWeight());
        record.setSp(form.getSp());
        record.setDp(form.getDp());
        record.setFhs(form.getFhs());
        record.setBodyCondition(form.getBodyCondition());
        record.setNextCheckDate(form.getNextCheckDate());
        record.setNextReminder(form.getNextReminder());

        prService.save(record);
        return "redirect:/blog/full-grid-left?tab=mhb-records&mhbId=" + mhbId;
    }

    // ====== 刪除 ======
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Integer id,
                         @RequestParam("mhbId") Integer mhbId,
                         HttpSession session) {
        MemberVO login = (MemberVO) session.getAttribute("member");
        if (login == null) return "redirect:/shop/login";

        var record = prService.getOne(id);
        var mhb = mhbService.getOneMhb(mhbId);
        if (record != null && mhb != null
                && mhb.getMemberId().equals(login.getMemberId())
                && record.getMotherHandbookId().equals(mhb.getMotherHandbookId())) {
            prService.delete(id);
        }
        return "redirect:/blog/full-grid-left?tab=mhb-records&mhbId=" + mhbId;
    }

}
