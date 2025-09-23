package com.babymate.sleep.model;

import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sleep-quiz")
public class SleepQuizApi {

    private final QuizStatsService stats;

    public SleepQuizApi(QuizStatsService stats) {
        this.stats = stats;
    }

    // 使用者看完結果就呼叫這支，進位 + 回傳最新統計
    @PostMapping("/submit")
    public Map<String, Object> submit() {
        long total = stats.incTotal();
        long today = stats.incToday();
        return Map.of("ok", true, "total", total, "today", today);
    }

    // 頁面載入時抓目前數字
    @GetMapping("/count")
    public Map<String, Object> count() {
        return Map.of("total", stats.getTotal(), "today", stats.getToday());
    }
}
