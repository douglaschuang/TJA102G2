package com.babymate.category.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * 商品分類 Repository
 * 
 * 功能：
 *   - 提供基本 CRUD（由 JpaRepository 自動生成）
 *   - 自訂查詢 listWithCount()：統計每個分類底下上架商品數量
 */
public interface CategoryRepository extends JpaRepository<CategoryVO, Integer>{
	
	/**
	 * 查詢每個分類及其上架商品數量
	 *
	 * 說明：
	 *   - 使用 JPQL（不是原生 SQL）
	 *   - FROM CategoryVO c → 指操作 Category 實體，而非資料表
	 *   - LEFT JOIN c.products p WITH p.status = 1 → 關聯商品，並只取 status=1（上架）
	 *   - COUNT(p) → 統計該分類底下上架商品數量
	 *   - GROUP BY → 以分類 ID/名稱分組
	 *   - 回傳結果：CategoryCountDTO（自訂資料傳輸物件）
	 */
	@Query("""
			SELECT new com.babymate.category.model.CategoryCountDTO(
				c.categoryId,
				c.categoryName,
				COUNT(p)
			)
			FROM CategoryVO c
			LEFT JOIN c.products p
			WITH p.status = 1
			GROUP BY c.categoryId, c.categoryName
			ORDER BY c.categoryId
			""")
	List<CategoryCountDTO> listWithCount();

}
