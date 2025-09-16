package com.babymate.adminindex.controller;

import com.babymate.adminindex.model.DashboardService;
import com.babymate.adminindex.model.SalesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/analytics")
public class AdminAnalyticsController {

  private final DashboardService dashboard;
  private final SalesService sales;
  public AdminAnalyticsController(DashboardService d, SalesService s){ this.dashboard=d; this.sales=s; }

  @GetMapping({"", "/"})
  public String index(Model model){
    model.addAttribute("pageTitle", "Analytics");
    model.addAttribute("stats",       dashboard.fetchStats());
    model.addAttribute("salesMonths", sales.monthLabels());
    model.addAttribute("salesSeries", sales.series());
    return "admin/analytics/analyticsindex"; 
  }
}
