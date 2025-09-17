package com.babymate.product.model;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface ProductRepository extends JpaRepository<ProductVO, Integer>{

	@Transactional // Spring交易管理註解，交易失敗會自動rollback
	@Modifying     // 表示更新/刪除功能
	@Query(value = "delete from product where product_id =?1", nativeQuery = true)  // nativeQuery = true 代表原生SQL查詢
	void deleteByProductId(int productId);

	List<ProductVO> findByStatus(Integer status); // 1:上架, 0:下架
	
	List<ProductVO> findByStatusOrderByUpdateTimeDesc(Integer status); // 前台熱門商品用
	
	// :categoryId IS NULL -> 沒分類直接查全部
	@Query("""
			SELECT p FROM ProductVO p
			LEFT JOIN p.categoryVO c
			WHERE p.status = 1
			AND (:categoryId IS NULL OR c.categoryId = :categoryId)
			AND (:minPrice IS NULL OR p.price >= :minPrice)
			AND (:maxPrice IS NULL OR p.price <= :maxPrice)
			ORDER BY p.updateTime DESC
			""")
	List<ProductVO> productPriceSearch(
			@Param("categoryId") Integer categoryId,
			@Param("minPrice") BigDecimal minPrice,
			@Param("maxPrice") BigDecimal maxPrice
	);
}
