package com.babymate.adminindex.controller;

import com.babymate.adminindex.model.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/order")
public class AdminOrderController {

  private final DashboardService dashboard;
  public AdminOrderController(DashboardService dashboard){ this.dashboard = dashboard; }

  // /admin/order 或 /admin/order/list 都可以進
  @GetMapping({"", "/", "/list"})
  public String list(Model model){
    model.addAttribute("pageTitle", "訂單管理");
    model.addAttribute("latestOrders", dashboard.latestOrders()); // 先重用
    return "admin/order/listindexorder";
  }
}
