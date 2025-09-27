package com.babymate.sleep.model;

import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class QuizStatsService {
    private static final String KEY_TOTAL = "stats:sleepquiz:total";
    private static final String KEY_DAILY_PREFIX = "stats:sleepquiz:date:"; // yyyy-MM-dd

    private final StringRedisTemplate redis;

    public QuizStatsService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public long incTotal() {
        return redis.opsForValue().increment(KEY_TOTAL);
    }

    public long incToday() {
        String today = LocalDate.now(ZoneId.of("Asia/Taipei")).toString();
        return redis.opsForValue().increment(KEY_DAILY_PREFIX + today);
    }

    public long getTotal() {
        String v = redis.opsForValue().get(KEY_TOTAL);
        return v == null ? 0L : Long.parseLong(v);
    }

    public long getToday() {
        String today = LocalDate.now(ZoneId.of("Asia/Taipei")).toString();
        String v = redis.opsForValue().get(KEY_DAILY_PREFIX + today);
        return v == null ? 0L : Long.parseLong(v);
    }
}
