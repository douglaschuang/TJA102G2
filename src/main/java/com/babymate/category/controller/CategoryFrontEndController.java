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
	
	/**
	 * 前台商城左側分類列表
	 * URL: /frontend/shop/left
	 * 
	 * 功能：
	 *  1️⃣ 取得每個分類及其商品數量（只計上架商品）
	 *  2️⃣ 計算全部上架商品總數（sum）
	 *  3️⃣ 傳給前台 shop-left-sidebar.html 顯示
	 */
	@GetMapping("/shop/left")
	public String categories(Model model) { 
		
		/** 1.查出每個分類及上架商品數量 */
		List<CategoryCountDTO> categories = categorySvc.listWithCount();
		
		/** 2.計算全部上架商品總數 */
		long allCount = categories.stream()
						.mapToLong(CategoryCountDTO::count)
						.sum();
		
		/** 3.放入 Model 給前端 shop-left-sidebar 顯示 */
		model.addAttribute("categories", categories);
		model.addAttribute("allCount", allCount);
		return "frontend/shop-left-sidebar";
	}
}