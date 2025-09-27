package com.babymate.babyhandbook.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.babymate.mhb.model.MhbVO;

import jakarta.transaction.Transactional;

public interface BabyhandbookRepository extends JpaRepository<BabyhandbookVO, Integer>{
	
	//查詢未刪除資料
	@Query(value = "SELECT * FROM baby_handbook WHERE deleted = 0 ORDER BY baby_handbook_id", nativeQuery = true)
    List<BabyhandbookVO> findAllActive();
    
    //查詢已刪除資料(垃圾桶裡)
    @Query(value = "SELECT * FROM baby_handbook WHERE deleted = 1 ORDER BY baby_handbook_id", nativeQuery = true)
    List<BabyhandbookVO> findAllDeleted();
    
    //軟刪除
    @Transactional
	@Modifying
	@Query(value = "UPDATE baby_handbook SET deleted = 1 WHERE baby_handbook_id = :id AND deleted = 0", nativeQuery = true)
	int softDelete(@Param("id")Integer id);
	
    //還原資料
    @Transactional
	@Modifying
	@Query(value = "UPDATE baby_handbook SET deleted = 0 WHERE baby_handbook_id = :id", nativeQuery = true)
	int restoreById(@Param("id")Integer id);
    
    //取得圖片(即使已刪除)
    @Query(value = "SELECT baby_handbook_files FROM baby_handbook WHERE baby_handbook_id = :id", nativeQuery = true)
    byte[] findPhotoBytesById(@Param("id") Integer id);


    @Query(value = "SELECT * FROM baby_handbook WHERE member_id = :memberId AND deleted = 0 ORDER BY baby_handbook_id DESC", nativeQuery = true)
    List<BabyhandbookVO> findAllByMemberId(@Param("memberId") Integer memberId);
    
    
    @Query(value = "SELECT * FROM baby_handbook WHERE baby_handbook_id = :babyhandbookid AND member_id = :memberId AND deleted = 0", nativeQuery = true)
    Optional<BabyhandbookVO> findByIdAndMemberId(@Param("babyhandbookid") Integer babyhandbookid,
                                                 @Param("memberId") Integer memberId);
    
}
