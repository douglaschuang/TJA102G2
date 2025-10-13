package com.babymate.member.model;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * 會員資料庫存取介面，繼承 JpaRepository 提供基本 CRUD 功能。 並包含自訂的查詢方法，用於依條件查詢會員資料。
 */
public interface MemberRepository extends JpaRepository<MemberVO, Integer> {

	/**
	 * 根據會員編號刪除會員資料。
	 * 使用原生 SQL 查詢並標註為修改操作（刪除）。
	 * 
	 * @param memberId 要刪除的會員 ID
	 */
	@Transactional
	@Modifying
	@Query(value = "delete from member where member_id =?1", nativeQuery = true)
	void deleteByMemberId(int memberId);

	/**
	 * 依多條件查詢會員資料（範例用自訂查詢）。 使用 JPQL 語法查詢 {@link MemberVO}。
	 *
	 * @param memberId 會員 ID
	 * @param account 帳號（支援 LIKE 查詢）
	 * @param registerDate 註冊日期
	 * @return 符合條件的會員清單
	 */
	@Query(value = "from MemberVO where memberId=?1 and account like ?2 and registerDate=?3 order by memberId")
	List<MemberVO> findByOthers(int memberId, String account, LocalDateTime registerDate);

	/**
	 * 根據帳號查詢會員資料。
     * 用於註冊或登入時確認帳號是否存在。
	 *
	 * @param account 帳號（E-Mail）
	 * @return 對應的會員物件；若無則回傳 {@code null}
	 */
	@Query(value = "from MemberVO where account=?1")
	MemberVO findByAccount(String account);

	/**
	 * 根據帳號與密碼查詢會員（登入驗證）。
	 * 密碼應先經過 MD5 或其他雜湊演算法加密後再比對。
	 *
	 * @param account 帳號
	 * @param hashPassword 加密後的密碼
	 * @return 若帳號與密碼皆正確，回傳會員資料；否則回傳 {@code null}
	 */
	@Query("FROM MemberVO where account=?1 and password=?2")
	MemberVO findByAccountAndPassword(String account, String hashPassword);

	/**
     * 計算特定時間區間內的註冊會員數量。
     * 用於後台儀表板統計「今日新會員」等資訊。
     *
     * @param start 起始時間
     * @param end 結束時間
     * @return 註冊會員筆數
     */
	long countByRegisterDateBetween(LocalDateTime start, LocalDateTime end);

	/**
     * 查詢最新註冊的前 5 位會員。
     * 用於後台首頁顯示「最新會員 TOP5」。
     *
     * @return 依註冊日期倒序排列的前五位會員清單
     */
	List<MemberVO> findTop5ByOrderByRegisterDateDesc();
}