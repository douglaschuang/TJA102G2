package com.babymate.babyrecord.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;

public interface BabyrecordRepository extends JpaRepository<BabyrecordVO, Integer>{

	@Transactional
	@Modifying
	@Query(value = "delete from baby_record where baby_record_id =?1" , nativeQuery = true)
	void deleteByBabyrecordid(int babyrecordid);
}
