package com.babymate.category.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<CategoryVO, Integer>{
	
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
