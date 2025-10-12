package com.babymate.category.model;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.babymate.product.model.ProductVO;

@Service("categoryService")
public class CategoryService {

	// Spring 自動注入資料庫介面
	@Autowired
	CategoryRepository repository;
	
	/** 新增商品分類 */
	public void addCategory(CategoryVO categoryVO) {
		repository.save(categoryVO);
	}
	
	/** 修改商品分類 */
	public void updateCategory(CategoryVO categoryVO) {
		repository.save(categoryVO);
	}
	
	/** 刪除分類(此處無做刪除功能，僅做預備) */
	public void deleteCategory(Integer category_id) {
		if(repository.existsById(category_id))
		   repository.deleteById(category_id);
	}
	
	/** 查詢單一商品分類 */
	public CategoryVO getOneCategory(Integer category_id) {
		Optional<CategoryVO> optional = repository.findById(category_id);
		return optional.orElse(null);
	}
	
	/** 查詢所有商品類別 */
	public List<CategoryVO> getAll(){
		return repository.findAll();
	}
	
	/** 根據 CategoryId 取該分類底下的 Product */
	public Set<ProductVO> getEmpsByCategory_id(Integer category_id){
		return getOneCategory(category_id).getProducts();
	}
	
	/** 取得商品分類列表(含商品數統計) */
	public List<CategoryCountDTO> listWithCount(){
		return repository.listWithCount();
	}
}
