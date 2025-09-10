package com.babymate.mhb.model;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MhbRepository extends JpaRepository<MhbVO, Integer> {

    /* ====== 條件查詢（用 Entity 欄位名） ======*/
    @Query("""
           SELECT m
           FROM MhbVO m
           WHERE (:id IS NULL OR m.motherHandbookId = :id)
             AND (:name IS NULL OR m.motherName LIKE CONCAT('%', :name, '%'))
             AND (:birthday IS NULL OR m.motherBirthday = :birthday)
           ORDER BY m.motherHandbookId
           """)
    List<MhbVO> findByOthers(
            @Param("id") Integer id,
            @Param("name") String name,
            @Param("birthday") Date birthday
            // 若用 LocalDate：java.time.LocalDate birthday
    );

    /* ====== 列表（配合 @Where 只會抓未刪） ====== */
    List<MhbVO> findByDeletedFalseOrderByMotherHandbookIdAsc();

    /* ====== 垃圾桶清單*/
    @Query(value = "SELECT * FROM mother_handbook WHERE deleted = 1 ORDER BY mother_handbook_id", nativeQuery = true)
    List<MhbVO> findAllDeletedNative();

    /* ====== 軟刪除／復原 ======
       - 軟刪除：直接把 deleted=1
       - 復原：把 deleted=0（用 native 可繞過 @Where）
    */
    @Transactional
    @Modifying
    @Query("UPDATE MhbVO m SET m.deleted = true WHERE m.motherHandbookId = :id AND m.deleted = false")
    int softDelete(@Param("id") Integer id);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE mother_handbook SET deleted = 0, deleted_at = NULL WHERE mother_handbook_id = :id",
           nativeQuery = true)
    int restoreById(@Param("id") Integer id);

    /* ====== 取圖（忽略 @Where，用於垃圾桶縮圖） ====== */
    @Query(value = "SELECT upfiles FROM mother_handbook WHERE mother_handbook_id = :id", nativeQuery = true)
    byte[] findPhotoBytesByIdNative(@Param("id") Integer id);
}
