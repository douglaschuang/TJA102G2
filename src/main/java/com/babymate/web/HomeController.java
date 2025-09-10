package com.babymate.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/admin", "/admin/"})
    public String adminHome() {
        return "admin/index";
    }
}
