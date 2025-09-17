package com.babymate.product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.babymate.product.model.ProductService;
import com.babymate.product.model.ProductVO;

@Controller
public class ProductFrontEndController {

	@Autowired
	private ProductService productSvc;
	
	@GetMapping("/shop/product-basic")
	public String showOneProduct(@RequestParam("id") Integer productId, ModelMap model) {
		ProductVO oneProduct = productSvc.getOneProduct(productId);
		model.addAttribute("product", oneProduct);
		return "frontend/shop-product-basic";
	}
}
