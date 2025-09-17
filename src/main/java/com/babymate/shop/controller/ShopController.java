package com.babymate.shop.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.babymate.category.model.CategoryCountDTO;
import com.babymate.category.model.CategoryService;
import com.babymate.product.model.ProductService;
import com.babymate.product.model.ProductVO;

@Controller
public class ShopController {

	@Autowired
	ProductService productSvc;
	
	@Autowired
	CategoryService categorySvc;

	public ShopController(ProductService productSvc) {
		super();
		this.productSvc = productSvc;
	}
	
	// 前台商城篩選商品列表頁，對應 /templates/frontend/shop-left-sidebar.html
	@GetMapping("/shop/left")
	public String shopLeftSidebar(
			@RequestParam(defaultValue = "newProduct") String sort,
			@RequestParam(required = false) Integer categoryId,
			@RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice,
			ModelMap model) {
		// 商品清單(依分類與價格篩選)
		var products = productSvc.findByCondition(categoryId, minPrice, maxPrice, sort);
	    model.addAttribute("hotProducts", products);
	    model.addAttribute("sort", sort);
		
		// 分類清單(含每個分類的商品數量)
		var categories = categorySvc.listWithCount();
		model.addAttribute("categories", categories);
		
		// 全部的數量(把個分類數量加總)
		long allCount = categories.stream().mapToLong(CategoryCountDTO::count).sum();
		model.addAttribute("allCount", allCount);
		
		// 把目前選擇回灌到頁面
		model.addAttribute("selectCategoryId", categoryId);
		model.addAttribute("minPrice", minPrice);
		model.addAttribute("maxPrice", maxPrice);
			
		return "frontend/shop-left-sidebar";
	}
}
