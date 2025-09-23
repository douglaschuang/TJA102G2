// PregnancyRecordController.java
package com.babymate.preg.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.babymate.mhb.model.MhbService;
import com.babymate.mhb.model.MhbVO;
import com.babymate.preg.model.PregnancyRecord;
import com.babymate.preg.model.PregnancyRecordService;

@Controller
@RequestMapping("/pregnancy")
public class PregnancyRecordController {

    private final PregnancyRecordService prService;
    private final MhbService mhbService;

    public PregnancyRecordController(PregnancyRecordService prService, MhbService mhbService) {
        this.prService = prService;
        this.mhbService = mhbService;
    }

    // 懷孕紀錄列表頁：
    @GetMapping("/records")
    public String listByMother(@RequestParam("mhbId") Integer mhbId, Model model) {
        MhbVO mhb = mhbService.getOneMhb(mhbId);
        if (mhb == null) {
            // 找不到時回主清單並顯示訊息
            model.addAttribute("error", "查無此媽媽手冊 #" + mhbId);
            return "redirect:/admin/mhb/list";
        }
        List<PregnancyRecord> records = prService.findByMhbId(mhbId);

        model.addAttribute("mhb", mhb);
        model.addAttribute("records", records);
        model.addAttribute("pageTitle", "懷孕紀錄");
        return "admin/preg/records_list";
    }
}
