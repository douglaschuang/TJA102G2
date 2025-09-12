package com.babymate.product.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("productService")
public class ProductService {

	@Autowired // 自動注入
	ProductRepository repository;

	@Autowired // 自動注入，Hibernate物件，開啟session與資料庫互動
	private SessionFactory sessionFactory;
	// 如果productVO有主鍵，執行update;如果productVO沒有主鍵，執行insert
	public void addProduct(ProductVO productVO) {
		repository.save(productVO);
	}
	// 如果productVO有主鍵，執行update;如果productVO沒有主鍵，執行insert
	public void updateProduct(ProductVO productVO) {
		repository.save(productVO);
	}

	public void deleteProduct(Integer productId) {
		if(repository.existsById(productId))         // 先確認資料庫有無這筆資料
		   repository.deleteByProductId(productId); // 呼叫ProductRepository自訂刪除方法(SQL寫)
//		   repository.deleteById(productId);         // Spring Data JPA內建刪除方法(用主鍵刪除)
	}

	public ProductVO getOneProduct(Integer productId) {
		Optional<ProductVO> optional = repository.findById(productId);
//		return optional.get(); // 用Optional避免直接NullPointerException
		return optional.orElse(null); // public T orElse(T other) : 如果值存在就回傳其值，否則回傳other的值
	}
	// 查詢所有商品
	public List<ProductVO> getAll() {
		return repository.findAll();
	}
	
	// 查詢商品狀態 1:上架, 0:下架
	public List<ProductVO> findByStatus(int status){
		return repository.findByStatus(status);
	}
	// 商品編號從B0001開始
//	public String generateProductNo(int id) {
//		return "B" + String.format("%04d", id);
//	}

}
