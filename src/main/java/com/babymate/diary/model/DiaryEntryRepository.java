package com.babymate.diary.model;

import java.util.List;

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
}
