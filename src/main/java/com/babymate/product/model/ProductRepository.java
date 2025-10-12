package com.babymate.product.model;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * 介面繼承 JpaRepository<ProductVO, Integer> → 擁有基本 CRUD
 * 自訂刪除：@Transactional + @Modifying + @Query(nativeQuery=true)
 * 前台條件查詢：使用 JPQL（可讀性高、可支援關聯 LEFT JOIN p.categoryVO c）
 * Java 文字方塊（Text Block） 字串：""" ... """，需要 JDK 15+
 */
public interface ProductRepository extends JpaRepository<ProductVO, Integer>{

	
	/**
	 * 自訂刪除（原生 SQL）
	 * - @Transactional：方法在交易中執行，失敗會 rollback
	 * - @Modifying：標示為更新/刪除型操作
	 * - nativeQuery = true：使用原生 SQL（資料表/欄位名需與 DB 一致）
	 */
	@Transactional
	@Modifying
	@Query(value = "delete from product where product_id =?1", nativeQuery = true)  // nativeQuery = true 代表原生SQL查詢
	void deleteByProductId(int productId);

	/** 依狀態查（上/下架） */
	List<ProductVO> findByStatus(Integer status); // 1:上架, 0:下架
	
	/** 上架且依更新時間新到舊 */
	List<ProductVO> findByStatusOrderByUpdateTimeDesc(Integer status); 
	
	/** 上架且依價格由高到低 */
	List<ProductVO> findByStatusOrderByPriceDesc(Integer status);
	
	/** 上架且依價格由低到高 */
	List<ProductVO> findByStatusOrderByPriceAsc(Integer status);

	/**
	 * 前台條件查詢：排序＝最新
	 * - 注意 JPQL（Java Persistence Query Language） 用的是 Entity 與欄位，而非資料表/欄名
	 * - 支援「分類可為空」與「價格上下限可為空」
	 * - 關聯：LEFT JOIN p.categoryVO c（需在 ProductVO 有對應欄位/關聯命名）
	 */
    @Query("""
        SELECT p FROM ProductVO p
        LEFT JOIN p.categoryVO c
        WHERE p.status = 1
          AND (:categoryId IS NULL OR c.categoryId = :categoryId)
          AND (:minPrice IS NULL OR p.price >= :minPrice)
          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        ORDER BY p.updateTime DESC
    """)
    List<ProductVO> findByConditionOrderByUpdateTimeDesc(
        @Param("categoryId") Integer categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice
    );
	
    /** 前台條件查詢：排序＝價格由低到高 */
    @Query("""
        SELECT p FROM ProductVO p
        LEFT JOIN p.categoryVO c
        WHERE p.status = 1
          AND (:categoryId IS NULL OR c.categoryId = :categoryId)
          AND (:minPrice IS NULL OR p.price >= :minPrice)
          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        ORDER BY p.price ASC
    """)
    List<ProductVO> findByConditionOrderByPriceAsc(
        @Param("categoryId") Integer categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice
    );

    /** 前台條件查詢：排序＝價格由高到低 */
    @Query("""
        SELECT p FROM ProductVO p
        LEFT JOIN p.categoryVO c
        WHERE p.status = 1
          AND (:categoryId IS NULL OR c.categoryId = :categoryId)
          AND (:minPrice IS NULL OR p.price >= :minPrice)
          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        ORDER BY p.price DESC
    """)
    List<ProductVO> findByConditionOrderByPriceDesc(
        @Param("categoryId") Integer categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice
    );
}
