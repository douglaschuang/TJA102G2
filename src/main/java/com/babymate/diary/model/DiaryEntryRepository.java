package com.babymate.diary.model;


import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Integer> {

	@Query("""
			  select d from DiaryEntry d
			  where d.memberId = :memberId
			  order by coalesce(d.writtenAt, d.createdAt) desc
			""")
	List<DiaryEntry> findRecentTop3(@Param("memberId") Integer memberId,
			org.springframework.data.domain.Pageable pageable);

	List<DiaryEntry> findByMemberIdOrderByWrittenAtDesc(Integer memberId);
	
	// 針對可能是 CLOB 的欄位，一律先 cast 成 string 再 lower()
    @Query("""
      select d from DiaryEntry d
      where d.memberId = :memberId
        and (
          lower(coalesce(cast(d.title   as string), '')) like concat('%', lower(:kw), '%') or
          lower(coalesce(cast(d.content as string), '')) like concat('%', lower(:kw), '%') or
          lower(coalesce(cast(d.tags    as string), '')) like concat('%', lower(:kw), '%')
        )
      order by coalesce(d.writtenAt, d.createdAt) desc
    """)
    List<DiaryEntry> searchTopN(@Param("memberId") Integer memberId,
                                @Param("kw") String kw,
                                Pageable pageable);

    @Query("""
      select d from DiaryEntry d
      where d.memberId = :memberId
        and (
          lower(coalesce(cast(d.title   as string), '')) like concat('%', lower(:kw), '%') or
          lower(coalesce(cast(d.content as string), '')) like concat('%', lower(:kw), '%') or
          lower(coalesce(cast(d.tags    as string), '')) like concat('%', lower(:kw), '%')
        )
      order by coalesce(d.writtenAt, d.createdAt) desc
    """)
    List<DiaryEntry> searchAll(@Param("memberId") Integer memberId,
                               @Param("kw") String kw);
}
