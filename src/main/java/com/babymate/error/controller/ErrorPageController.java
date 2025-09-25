package com.babymate.error.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController {

    @GetMapping("/error/403")
    public String forbidden(Model model) {
        model.addAttribute("errorMessage", "您沒有權限訪問此功能");
        return "error/403"; // 對應 templates/error/403.html
    }
}
