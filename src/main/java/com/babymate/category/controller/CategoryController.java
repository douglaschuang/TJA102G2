package com.babymate.category.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.babymate.category.model.CategoryService;
import com.babymate.category.model.CategoryVO;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/category")
public class CategoryController {
	
	@Autowired
	private CategoryService categorySvc;
	
	//===========addCategory.html=================
	@GetMapping("/addCategory")
	public String addCategory(Model model) { 
		CategoryVO categoryVO = new CategoryVO();
		model.addAttribute("categoryVO", categoryVO);
		// 商品管理標題＿類別新增
		model.addAttribute("pageTitle", "商品管理｜新增類別");
		return "admin/category/addCategory";
	}
	
	@PostMapping("insert")
	public String insert(@Valid @ModelAttribute CategoryVO categoryVO, BindingResult result, ModelMap model) {
		
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		if(result.hasErrors()) {
			return "admin/category/addCategory";
		}
		/*************************** 2.開始新增資料 *****************************************/
		categorySvc.addCategory(categoryVO);
		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/
		List<CategoryVO> list = categorySvc.getAll();
		model.addAttribute("categoryListData", list);
		model.addAttribute("success", "-(新增成功)");
		return "redirect:/admin/category/listAllCategory";
	}
	
	//===========listAllCategory.html=================
	@GetMapping("listAllCategory")
	public String listAllCategory(ModelMap model) {
		List<CategoryVO> list = categorySvc.getAll();
		model.addAttribute("categoryListData", list);
		// 商品管理標題＿類別清單
		model.addAttribute("pageTitle", "商品管理｜類別清單");
		return "admin/category/listAllCategory";
	}
	
	
	//===========getOne_For_update=================
	@PostMapping("getOne_For_Update")
	public String getOne_For_Update(@RequestParam("category_id") String categoryId, ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始查詢資料 *****************************************/
		CategoryVO categoryVO = categorySvc.getOneCategory(Integer.valueOf(categoryId));
		
		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("categoryVO", categoryVO);
		// 商品管理標題＿類別修改
		model.addAttribute("pageTitle", "商品管理｜修改類別");
		return "admin/category/updateCategory";
	}
	
	//===========updateCategory.html=================
	@PostMapping("update")
	public String update(@Valid CategoryVO categoryVO, BindingResult result, ModelMap model) throws IOException{
		
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始修改資料 *****************************************/
		categorySvc.updateCategory(categoryVO);
		
		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/
		model.addAttribute("success", "-(修改成功)");
		categoryVO = categorySvc.getOneCategory(Integer.valueOf(categoryVO.getCategoryId()));
		model.addAttribute("categoryVO", categoryVO);
		return "redirect:/admin/category/listAllCategory";
	}
}
