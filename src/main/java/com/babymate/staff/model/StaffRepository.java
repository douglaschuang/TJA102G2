package com.babymate.staff.model;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * 員工資料庫存取介面，繼承 JpaRepository 提供基本 CRUD 功能。
 * 並包含自訂的查詢方法，用於依條件查詢與刪除員工資料。
 */
public interface StaffRepository extends JpaRepository<StaffVO, Integer> {

	/**
     * 根據員工編號刪除該員工資料。
     * 使用原生 SQL 查詢並標註為修改操作（修改、刪除）。
     *
     * @param staffId 要刪除的員工編號
     */
	@Transactional
	@Modifying
	@Query(value = "delete from staff where staff_id = ?1", nativeQuery = true)
	void deleteByStaffId(int staffId);

	/**
     * 根據員工編號與帳號名稱（模糊查詢）取得員工列表，並依員工編號排序。
     *
     * @param staffId 指定員工編號（精確比對）
     * @param account 帳號名稱條件，使用 LIKE 進行模糊查詢（可包含 % 通配符）
     * @return 符合條件的員工列表
     */
	@Query(value = "from StaffVO where staffId = ?1 and account like concat('%', ?2, '%') order by staffId")
	List<StaffVO> findByOthers(int staffId, String account);

	/**
     * 根據帳號與密碼（已加密的 Hash 值）查詢符合的員工資料，用於登入驗證。
     *
     * @param account 員工帳號
     * @param hashPassword 加密後的密碼字串（例如 MD5 或其他）
     * @return 符合帳號與密碼的員工資料，找不到回傳 null
     */
	@Query(value = "from StaffVO where account = ?1 and password = ?2")
	StaffVO findByAccountAndPassword(String account, String hashPassword);

	/**
     * 根據帳號查詢員工資料，用於檢查帳號是否已存在，避免重複註冊。
     *
     * @param account 欲查詢的員工帳號
     * @return 找到的員工資料，若無則回傳 null
     */
	@Query(value = "from StaffVO where account = ?1")
	StaffVO findByAccount(String account);
}