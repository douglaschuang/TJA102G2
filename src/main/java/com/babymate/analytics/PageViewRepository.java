package com.babymate.analytics;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface PageViewRepository extends JpaRepository<PageViewEntity, Long> {

  @Modifying
  @Query(value = """
    INSERT INTO page_view (session_id, path, viewed_at)
    VALUES (:sid, :path, :ts)
  """, nativeQuery = true)
  void insert(@Param("sid") String sessionId,
              @Param("path") String path,
              @Param("ts")   LocalDateTime ts);

  // 今日「只有 1 次瀏覽」的 session 數（bounce）
  @Query(value = """
    SELECT COUNT(*) FROM (
      SELECT session_id, COUNT(*) AS pv_cnt
      FROM page_view
      WHERE viewed_at >= :start AND viewed_at < :end
      GROUP BY session_id
      HAVING pv_cnt = 1
    ) t
  """, nativeQuery = true)
  long countSessionsWithExactlyOnePV(@Param("start") LocalDateTime start,
                                     @Param("end")   LocalDateTime end);
}
