package com.babymate.category.model;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.babymate.product.model.ProductVO;

@Service("categoryService")
public class CategoryService {

	@Autowired
	CategoryRepository repository;
	
	public void addCategory(CategoryVO categoryVO) {
		repository.save(categoryVO);
	}
	
	public void updateCategory(CategoryVO categoryVO) {
		repository.save(categoryVO);
	}
	
	public void deleteCategory(Integer category_id) {
		if(repository.existsById(category_id))
		   repository.deleteById(category_id);
	}
	
	public CategoryVO getOneCategory(Integer category_id) {
		Optional<CategoryVO> optional = repository.findById(category_id);
//		return optional.get();
		return optional.orElse(null);
	}
	
	public List<CategoryVO> getAll(){
		return repository.findAll();
	}
	
	public Set<ProductVO> getEmpsByCategory_id(Integer category_id){
		return getOneCategory(category_id).getProducts();
	}
}
