package com.babymate.product.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.babymate.category.model.CategoryVO;


public interface ProductRepository extends JpaRepository<ProductVO, Integer>{

	@Transactional // Spring交易管理註解，交易失敗會自動rollback
	@Modifying     // 表示更新/刪除功能
	@Query(value = "delete from product where product_id =?1", nativeQuery = true)  // nativeQuery = true 代表原生SQL查詢
	void deleteByProductId(int productId);

	List<ProductVO> findByStatus(Integer status); // 1:上架, 0:下架
}
