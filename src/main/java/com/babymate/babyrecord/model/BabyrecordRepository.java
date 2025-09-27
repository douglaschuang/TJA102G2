package com.babymate.babyrecord.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface BabyrecordRepository extends JpaRepository<BabyrecordVO, Integer>{

	@Transactional
	@Modifying
	@Query(value = "delete from baby_record where baby_record_id =?1" , nativeQuery = true)
	void deleteByBabyrecordid(int babyrecordid);
	
	@Query("SELECT br.babyhandbook.babyhandbookid, COUNT(br) FROM BabyrecordVO br GROUP BY br.babyhandbook.babyhandbookid")
    List<Object[]> countRecordsGroupByBabyhandbookId();

    //取得圖片(即使已刪除)
    @Query(value = "SELECT baby_record_files FROM baby_record WHERE baby_record_id = :id", nativeQuery = true)
    byte[] findPhotoBytesById(@Param("id") Integer id);

	List<BabyrecordVO> findByBabyhandbook_Babyhandbookid(Integer babyhandbookid);

}
