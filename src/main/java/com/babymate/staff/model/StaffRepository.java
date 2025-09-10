// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/

package com.babymate.staff.model;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface StaffRepository extends JpaRepository<StaffVO, Integer> {

	@Transactional
	@Modifying
	@Query(value = "delete from staff where staff_id =?1", nativeQuery = true)
	void deleteByStaffId(int staffId);

	//● (自訂)條件查詢
	@Query(value = "from StaffVO where staffId=?1 and account like?2 order by staffId")
	List<StaffVO> findByOthers(int staffId , String account);
	
	@Query(value = "from StaffVO where account=?1 and password=?2")
	StaffVO findByAccountAndPassword(String account, String hashPassword);
}