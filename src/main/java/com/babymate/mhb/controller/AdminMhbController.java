package com.babymate.mhb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import com.babymate.mhb.model.MhbFilter;
import com.babymate.mhb.model.MhbService;
import com.babymate.preg.model.PregnancyRecordService;

@Controller
@RequestMapping("/admin/mhb")
public class AdminMhbController {

    private final MhbService mhbSvc;
    private final PregnancyRecordService prService;

    public AdminMhbController(MhbService mhbSvc, PregnancyRecordService prService) {
        this.mhbSvc = mhbSvc;
        this.prService = prService;
    }

    /** 讓每次進來都有一個空的 filter 可供表單綁定（相當於預設值） */
    @ModelAttribute("filter")
    public MhbFilter defaultFilter() {
        return new MhbFilter();
    }

    /** 主列表（含複合查詢，GET 參數綁到 filter 上） */
    @GetMapping("/list")
    public String list(@ModelAttribute("filter") MhbFilter filter, ModelMap model) {
        model.addAttribute("mhbListData", mhbSvc.search(filter));   // ← 關鍵：改用 search()
        model.addAttribute("recordCountMap", prService.getCountMap());
        model.addAttribute("pageTitle", "媽媽手冊｜列表");
        return "admin/mhb/mhb_list";
    }

    /** 垃圾桶（已刪除）－維持不變 */
    @GetMapping("/deleted")
    public String deleted(ModelMap model) {
        model.addAttribute("mhbDeletedList", mhbSvc.findAllDeleted());
        model.addAttribute("pageTitle", "媽媽手冊｜垃圾桶");
        return "admin/mhb/mhb_deleted";
    }
    
  //垃圾桶計數
  	@ModelAttribute("deletedCount")
  	public long deletedCount() {
  	    return mhbSvc.countDeleted();
  	}
}
