package com.babymate.analytics;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Service
public class TrackingService {

  private final VisitSessionRepository vsRepo;
  private final PageViewRepository pvRepo;

  public TrackingService(VisitSessionRepository vsRepo, PageViewRepository pvRepo) {
    this.vsRepo = vsRepo;
    this.pvRepo = pvRepo;
  }

  @Transactional
  public void record(String sid, String ip, String ua, String path, LocalDateTime now) {
    vsRepo.upsert(sid, ip, ua, now);
    pvRepo.insert(sid, path, now);
  }

  /** 回傳「百分比（0~100）」 */
  public double bounceRateToday(ZoneId tz) {
    LocalDateTime start = LocalDate.now(tz).atStartOfDay();
    LocalDateTime end   = start.plusDays(1);

    long sessions = vsRepo.countStartedBetween(start, end);
    if (sessions == 0) return 0.0;

    long bounced = pvRepo.countSessionsWithExactlyOnePV(start, end);
    return (bounced * 100.0) / sessions;
  }

  public long uniqueVisitors7d(ZoneId tz) {
    LocalDateTime from = LocalDate.now(tz).minusDays(6).atStartOfDay();
    return vsRepo.countDistinctByLastSeenSince(from);
  }
}
