package com.babymate.sleep.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sleep")
public class SleepQuizController {
    @GetMapping("/quiz")
    public String quiz() {
        return "frontend/sleep-quiz";
    }
}
