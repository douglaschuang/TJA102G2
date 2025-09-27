package com.babymate.adminindex.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.babymate.adminindex.model.DashboardService;
import com.babymate.adminindex.model.SalesService;

@Controller
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {
  private final DashboardService dashboardService;
  private final SalesService salesService;

  public AdminDashboardController(DashboardService d, SalesService s) {
    this.dashboardService = d; this.salesService = s;
  }

  @GetMapping
  public String index(Model model){
    model.addAttribute("pageTitle", "後台管理系統");
    model.addAttribute("stats",         dashboardService.fetchStats());
    model.addAttribute("latestOrders",  dashboardService.latestOrders());
    model.addAttribute("latestMembers", dashboardService.latestMembers());
    model.addAttribute("hotProducts",   dashboardService.hotProducts30d()); // 🔁
    model.addAttribute("salesMonths",   salesService.monthLabels());
    model.addAttribute("salesSeries",   salesService.series());
    return "admin/index";
  }
}
