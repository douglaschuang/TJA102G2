package com.babymate.diary.model;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WeatherService {

    private final StringRedisTemplate redis;
    private final RestClient http = RestClient.create();
    private static final ZoneId TAIPEI = ZoneId.of("Asia/Taipei");
    private static final DateTimeFormatter HOUR_FMT = DateTimeFormatter.ofPattern("yyyyMMddHH");

    public WeatherService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public record WeatherNow(double tempC, int code, String text) {}

    public WeatherNow fetchNow(double lat, double lon) {
        // key：到小時，同地點同小時快取
        String hourKey = LocalDateTime.now(TAIPEI).format(HOUR_FMT);
        String key = String.format("weather:openmeteo:%.3f:%.3f:%s", lat, lon, hourKey);

        String cached = redis.opsForValue().get(key);
        if (cached != null) {
            return parseCompact(cached);
        }

        // Open-Meteo：免金鑰
        String url = String.format(
            "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current_weather=true&timezone=Asia%%2FTaipei",
            lat, lon);

        ResponseEntity<Map> resp = http.get().uri(URI.create(url)).retrieve().toEntity(Map.class);
        Map body = resp.getBody();
        if (body == null || body.get("current_weather") == null) {
            // 失敗回退
            return new WeatherNow(Double.NaN, -1, "無法取得天氣");
        }
        Map cur = (Map) body.get("current_weather");
        double temp = toDouble(cur.get("temperature")); // 攝氏
        int code = (int) Math.round(toDouble(cur.get("weathercode")));
        String text = toText(code);

        String compact = temp + "|" + code + "|" + text;
        redis.opsForValue().set(key, compact, java.time.Duration.ofMinutes(15));
        return new WeatherNow(temp, code, text);
    }

    private double toDouble(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        return Double.parseDouble(String.valueOf(o));
    }

    private WeatherNow parseCompact(String s) {
        String[] parts = s.split("\\|", 3);
        return new WeatherNow(Double.parseDouble(parts[0]), Integer.parseInt(parts[1]), parts[2]);
        // parts[2] 已是翻譯後文字
    }

    // 簡單把 Open-Meteo weathercode 轉成中文敘述
    private String toText(int code) {
        return switch (code) {
            case 0 -> "晴朗";
            case 1, 2 -> "多雲時晴";
            case 3 -> "陰天";
            case 45, 48 -> "霧/霜霧";
            case 51, 53, 55 -> "毛毛雨";
            case 56, 57 -> "凍毛毛雨";
            case 61, 63, 65 -> "小/中/大雨";
            case 66, 67 -> "凍雨";
            case 71, 73, 75 -> "小/中/大雪";
            case 77 -> "霰雪";
            case 80, 81, 82 -> "陣雨";
            case 85, 86 -> "陣雪";
            case 95 -> "雷雨";
            case 96, 99 -> "強雷雨/冰雹";
            default -> "不明";
        };
    }
}
