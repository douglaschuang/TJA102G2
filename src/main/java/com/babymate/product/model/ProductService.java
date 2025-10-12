package com.babymate.product.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("productService")
public class ProductService {

	@Autowired // 自動注入
	ProductRepository repository;

	/**
	 * 新增商品（或有主鍵則視為更新）
	 * - Spring Data JPA 的 save()：主鍵存在→UPDATE；不存在→INSERT
	 */
	public void addProduct(ProductVO productVO) {
		repository.save(productVO);
	}
	
	/**
	 * 更新商品（或無主鍵則視為新增）
	 * - Spring Data JPA 的 save()：主鍵存在→UPDATE；不存在→INSERT
	 */
	public void updateProduct(ProductVO productVO) {
		repository.save(productVO);
	}

	/**
	 * 刪除商品
	 * - 先 existsById() 確認存在再刪（避免拋 Exception）
	 * - 用自訂 SQL（deleteByProductId）刪除；也可直接用 deleteById()
	 */
	public void deleteProduct(Integer productId) {
		if(repository.existsById(productId))         // 先確認資料庫有無這筆資料
		   repository.deleteByProductId(productId); // 呼叫ProductRepository自訂刪除方法(SQL寫)
//		   repository.deleteById(productId);         // Spring Data JPA內建刪除方法(用主鍵刪除)
	}

	/**
	 * 以主鍵查一筆
	 * - Optional 避免 NullPointerException，若無回傳 null
	 */
	public ProductVO getOneProduct(Integer productId) {
		Optional<ProductVO> optional = repository.findById(productId);
//		return optional.get(); // 用Optional避免直接NullPointerException
		return optional.orElse(null);
	}
	
	/** 查詢全部(後台列表用) */
	public List<ProductVO> getAll() {
		return repository.findAll();
	}
	
	/** 查詢商品狀態 (1:上架, 0:下架) */
	public List<ProductVO> findByStatus(Integer status){
		return repository.findByStatus(status);
	}

	/** 上架商品最新時間排列UpdateTimeDesc(前台商城用) */
	public List<ProductVO> getAllByNewProduct(){
		return repository.findByStatusOrderByUpdateTimeDesc(1);
	}
	
	/** 上架商品依價格由高到低PriceDesc(前台商城用) */
	public List<ProductVO> getAllByPriceDesc(){
		return repository.findByStatusOrderByPriceDesc(1);
	}
	
	/** 上架商品依價格由低到高PriceAsc(前台商城用) */
	public List<ProductVO> getAllByPriceAsc(){
		return repository.findByStatusOrderByPriceAsc(1);
	}
	
	/** 前台篩選及排列 */
	public List<ProductVO> findByCondition(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String sort) {
	    switch (sort) {
	        case "priceAsc":
	            return repository.findByConditionOrderByPriceAsc(categoryId, minPrice, maxPrice);
	        case "priceDesc":
	            return repository.findByConditionOrderByPriceDesc(categoryId, minPrice, maxPrice);
	        case "newProduct":
	        default:
	            return repository.findByConditionOrderByUpdateTimeDesc(categoryId, minPrice, maxPrice);
	    }
	}
}
