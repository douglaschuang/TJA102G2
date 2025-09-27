package com.babymate.diaryfrontend.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.*;
import com.babymate.diary.model.WeatherService;

@RestController("diaryWeatherController")           // ← 關鍵：改 bean 名稱
@RequestMapping("/api/weather")                      // ← 建議加：統一路徑前綴
public class WeatherController {

    private final WeatherService svc;
    public WeatherController(WeatherService svc) { this.svc = svc; }

    // GET /api/weather/now?lat=25.04&lon=121.56
    @GetMapping("/now")
    public Map<String, Object> now(@RequestParam double lat, @RequestParam double lon) {
        var w = svc.fetchNow(lat, lon);
        return Map.of("ok", true, "tempC", w.tempC(), "code", w.code(), "text", w.text());
    }
}
