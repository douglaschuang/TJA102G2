package com.babymate.babyhandbook.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;

public interface BabyhandbookRepository extends JpaRepository<BabyhandbookVO, Integer>{

	@Transactional
	@Modifying
	@Query(value = "delete from baby_handbook where baby_handbook_id =?1" , nativeQuery = true)
	void deleteByBabyhandbookid(int babyhandbookid);
}
