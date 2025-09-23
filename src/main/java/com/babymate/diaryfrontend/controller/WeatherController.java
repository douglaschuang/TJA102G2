package com.babymate.diaryfrontend.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.babymate.diary.model.WeatherService;

@RestController
public class WeatherController {

    private final WeatherService svc;

    public WeatherController(WeatherService svc) {
        this.svc = svc;
    }

    // /api/weather/now?lat=25.04&lon=121.56
    @GetMapping("/api/weather/now")
    public Map<String, Object> now(double lat, double lon) {
        var w = svc.fetchNow(lat, lon);
        return Map.of(
            "ok", true,
            "tempC", w.tempC(),
            "code", w.code(),
            "text", w.text()
        );
    }
}
