// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/

package com.babymate.member.model;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MemberRepository extends JpaRepository<MemberVO, Integer> {

	@Transactional
	@Modifying
	@Query(value = "delete from member where member_id =?1", nativeQuery = true)
	void deleteByMemberId(int memberId);

	//● (自訂)條件查詢
	@Query(value = "from MemberVO where memberId=?1 and account like ?2 and registerDate=?3 order by memberId")
	List<MemberVO> findByOthers(int memberId , String account , LocalDateTime registerDate);
	
	// 根據account去找會員
	@Query(value = "from MemberVO where account=?1")
	MemberVO findByAccount(String account);
	
	// 根據account和password去找會員 // JPQL，查整個 Entity
	@Query("FROM MemberVO where account=?1 and password=?2")
	MemberVO findByAccountAndPassword(String account, String hashPassword);	

	// 後台首頁：今日新會員
    long countByRegisterDateBetween(LocalDateTime start, LocalDateTime end);

    // 後台首頁：最新會員 TOP5
    List<MemberVO> findTop5ByOrderByRegisterDateDesc();
}