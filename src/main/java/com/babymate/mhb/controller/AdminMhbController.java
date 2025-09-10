// AdminMhbController.java
package com.babymate.mhb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.babymate.mhb.model.MhbService;
import com.babymate.preg.model.PregnancyRecordService;

@Controller
@RequestMapping("/admin/mhb")
public class AdminMhbController {

    private final MhbService mhbSvc;
    private final PregnancyRecordService prService; // ← 新增

    public AdminMhbController(MhbService mhbSvc, PregnancyRecordService prService) {
        this.mhbSvc = mhbSvc;
        this.prService = prService;
    }

    // 主列表（未刪除）
    @GetMapping("/list")
    public String list(ModelMap model) {
        model.addAttribute("mhbListData", mhbSvc.findAllActive());
        model.addAttribute("recordCountMap", prService.getCountMap()); // ← 讓頁面拿得到筆數
        model.addAttribute("pageTitle", "媽媽手冊｜列表");
        return "admin/mhb/mhb_list";
    }

    // 垃圾桶（已刪除）
    @GetMapping("/deleted")
    public String deleted(ModelMap model) {
        model.addAttribute("mhbDeletedList", mhbSvc.findAllDeleted());
        model.addAttribute("pageTitle", "媽媽手冊｜垃圾桶");
        return "admin/mhb/mhb_deleted";
    }
}
