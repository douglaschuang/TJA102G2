package com.babymate.category.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.babymate.category.model.CategoryCountDTO;
import com.babymate.category.model.CategoryService;

@Controller
@RequestMapping("/frontend")
public class CategoryFrontEndController {
	
	@Autowired
	private CategoryService categorySvc;
	
	@GetMapping("/shop/left")
	public String categories(Model model) { 
		
		List<CategoryCountDTO> categories = categorySvc.listWithCount();
		
		// 全部數量 = 各分類加總
		long allCount = categories.stream()
						.mapToLong(CategoryCountDTO::count)
						.sum();
		
		model.addAttribute("categories", categories);
		model.addAttribute("allCount", allCount);
		return "frontend/shop-left-sidebar";
		
	}
}