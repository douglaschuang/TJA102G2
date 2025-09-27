package com.babymate.preg.model;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

public interface PregnancyRecordRepository extends JpaRepository<PregnancyRecord, Integer> {

	// 依媽媽手冊ID查該媽媽所有紀錄（就診日新到舊）
	List<PregnancyRecord> findByMotherHandbookIdOrderByVisitDateDesc(Integer motherHandbookId);

	// 統計每位媽媽的紀錄數（回傳投影介面）
	@Query("""
			select pr.motherHandbookId as mhbId, count(pr) as cnt
			from PregnancyRecord pr
			group by pr.motherHandbookId
			""")
	List<MhbCount> countGroupByMhbId();

	interface MhbCount {
		Integer getMhbId();

		Long getCnt();
	}

	@Query("""
			  select pr
			  from PregnancyRecord pr
			  left join fetch pr.clinic c
			  where pr.motherHandbookId = :mhbId
			  order by pr.visitDate desc
			""")
	List<PregnancyRecord> findAllByMhbIdFetchClinic(@Param("mhbId") Integer mhbId);
}
