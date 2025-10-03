package com.babymate.todo.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MhbTodoRepository extends JpaRepository<MhbTodo, Integer> {
    List<MhbTodo> findByMotherHandbookIdOrderByDoneAscDueDateAsc(Integer mhbId);
 // 關鍵字搜尋（聚合頁 Top N）
    @Query("""
      select t from MhbTodo t
      where t.motherHandbookId = :mhbId
        and (
          lower(coalesce(t.title, '')) like concat('%', lower(:kw), '%') or
          lower(coalesce(t.note , '')) like concat('%', lower(:kw), '%')
        )
      order by t.done asc, t.dueDate asc, t.createdAt desc
    """)
    List<MhbTodo> searchTopN(@Param("mhbId") Integer mhbId,
                             @Param("kw") String kw,
                             // 👇 用完全限定名，徹底避開 java.awt.print.Pageable
                             org.springframework.data.domain.Pageable pageable);
    
 // 關鍵字搜尋（完整清單）
    @Query("""
      select t from MhbTodo t
      where t.motherHandbookId = :mhbId
        and (
          lower(coalesce(t.title, '')) like concat('%', lower(:kw), '%') or
          lower(coalesce(t.note , '')) like concat('%', lower(:kw), '%')
        )
      order by t.done asc, t.dueDate asc, t.createdAt desc
    """)
    List<MhbTodo> searchAll(@Param("mhbId") Integer mhbId,
                            @Param("kw") String kw);
    
    
 // ★ 新增：以 memberId 跨所有手冊搜尋 TopN（nativeQuery）
    //    —— 請確認實際資料表與欄位名稱；以下用常見 snake_case 命名。
    @Query(value = """
      SELECT t.*
      FROM mhb_todo t
      JOIN mother_handbook m
        ON m.mother_handbook_id = t.mother_handbook_id
      WHERE m.member_id = :memberId
        AND (
          LOWER(COALESCE(t.title, '')) LIKE CONCAT('%', LOWER(:kw), '%')
          OR LOWER(COALESCE(t.note , '')) LIKE CONCAT('%', LOWER(:kw), '%')
        )
      ORDER BY t.done ASC, t.due_date ASC, t.created_at DESC
      LIMIT :limit
    """, nativeQuery = true)
    List<MhbTodo> searchTopNByMemberNative(@Param("memberId") Integer memberId,
                                           @Param("kw") String kw,
                                           @Param("limit") int limit);
}
