package com.babymate.forum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.babymate.forum.model.ReportVO;
import com.babymate.forum.service.ReportService;
@Controller
@RequestMapping("/admin/forum/report")
public class ReportAdminController {

    @Autowired
    private ReportService reportService;

    // 1. 顯示檢舉列表頁面
    @GetMapping
    public String showReportList(@RequestParam(defaultValue = "0") int page, Model model) {
        // 設定分頁，每頁 10 筆，按檢舉時間倒序排列
        Pageable pageable = PageRequest.of(page, 10, Sort.by("reportTime").descending());
        Page<ReportVO> reportPage = reportService.findAllReports(pageable);
        
        model.addAttribute("reportPage", reportPage);
        return "admin/forum/report_management"; // 指向我們的後台模板頁面
    }

    // 2. 處理狀態更新的請求
    @PostMapping("/update/{reportId}")
    public String updateReportStatus(@PathVariable("reportId") Integer reportId, 
                                     @RequestParam("status") Byte newStatus) {
        reportService.updateReportStatus(reportId, newStatus);
        return "redirect:/admin/forum/report"; // 處理完後，重新導向回列表頁
    }
}