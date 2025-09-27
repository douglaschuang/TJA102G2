package com.babymate.analytics;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface VisitSessionRepository extends JpaRepository<VisitSessionEntity, Long> {

  @Modifying
  @Query(value = """
    INSERT INTO visit_session (session_id, ip, user_agent, started_at, last_seen_at)
    VALUES (:sid, :ip, :ua, :now, :now)
    ON DUPLICATE KEY UPDATE
      last_seen_at = VALUES(last_seen_at),
      ip          = VALUES(ip),
      user_agent  = VALUES(user_agent)
  """, nativeQuery = true)
  void upsert(@Param("sid") String sid,
              @Param("ip")  String ip,
              @Param("ua")  String userAgent,
              @Param("now") LocalDateTime now);

  @Query(value = """
    SELECT COUNT(*) FROM visit_session
    WHERE started_at >= :start AND started_at < :end
  """, nativeQuery = true)
  long countStartedBetween(@Param("start") LocalDateTime start,
                           @Param("end")   LocalDateTime end);

  @Query(value = """
    SELECT COUNT(*) FROM visit_session
    WHERE last_seen_at >= :from
  """, nativeQuery = true)
  long countDistinctByLastSeenSince(@Param("from") LocalDateTime from);
}
