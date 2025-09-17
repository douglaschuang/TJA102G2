package com.babymate.product.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.babymate.category.model.CategoryService;
import com.babymate.category.model.CategoryVO;
import com.babymate.product.model.ProductService;
import com.babymate.product.model.ProductVO;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/product")
public class ProductController {
    
	@Autowired
	ProductService productSvc;
	
	@Autowired
	CategoryService categorySvc;
	
    //===========addProduct.html=================
	@GetMapping("addProduct")
	public String addProduct(ModelMap model) {
		ProductVO productVO = new ProductVO();
		model.addAttribute("productVO", productVO);
		return "admin/product/addProduct";
	}
	
	/*
	 * This method will be called on addProduct.html form submission, handling POST request It also validates the user input
	 */
	@PostMapping("insert")
	public String insert(@Valid ProductVO productVO, BindingResult result, ModelMap model,
			@RequestParam("productIcon") MultipartFile[] parts) throws IOException{

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 去除BindingResult中upFiles欄位的FieldError紀錄 --> 見第172行
		result = removeFieldError(productVO, result, "productIcon");

		if (parts[0].isEmpty()) { // 使用者未選擇要上傳的圖片時
			model.addAttribute("errorMessage", "商品圖片: 請上傳商品圖片");
		} else {
			for (MultipartFile multipartFile : parts) {
				byte[] buf = multipartFile.getBytes();
				productVO.setProductIcon(buf);
			}
		}
		if (result.hasErrors() || parts[0].isEmpty()) {
			return "admin/product/addProduct";
		}
		/*************************** 2.開始新增資料 *****************************************/
		// EmpService empSvc = new EmpService();
		productSvc.addProduct(productVO);
		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/
		List<ProductVO> list = productSvc.getAll();
		model.addAttribute("productListData", list); // for listAllEmp.html 第85行用
		model.addAttribute("success", "- (新增成功)");
		return "redirect:/admin/product/listAllProduct"; // 新增成功後重導至IndexController_inSpringBoot.java的第58行@GetMapping("/emp/listAllEmp")
	}
	
	/*
	 * This method will be called on listAllProduct.html form submission, handling POST request
	 */
	@PostMapping("getOne_For_Update")
	public String getOne_For_Update(@RequestParam("product_id") String productId, ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始查詢資料 *****************************************/
		// EmpService empSvc = new EmpService();
		ProductVO productVO = productSvc.getOneProduct(Integer.valueOf(productId));

		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("productVO", productVO);
		return "admin/product/update_product_input"; // 查詢完成後轉交update_product_input.html
	}

	// 上架清單
	@GetMapping("listAllProduct")
	public String listAllProduct(ModelMap model) {
		model.addAttribute("productListData", productSvc.findByStatus(1));
		return "admin/product/listAllProduct";
	}
	
	// 下架清單(下架區)
	@GetMapping("productRemoveArea")
	public String productRemoveArea(ModelMap model) {
		model.addAttribute("productListData", productSvc.findByStatus(0));
		return "admin/product/productRemoveArea";
	}
	
	
	/*
	 * This method will be called on update_product_input.html form submission, handling POST request It also validates the user input
	 */
	@PostMapping("update")
	public String update(@Valid ProductVO productVO, BindingResult result, ModelMap model,
			@RequestParam("productIcon") MultipartFile[] parts) throws IOException {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 去除BindingResult中upFiles欄位的FieldError紀錄 --> 見第172行
		result = removeFieldError(productVO, result, "productIcon");

		if (parts[0].isEmpty()) { // 使用者未選擇要上傳的新圖片時
			// EmpService empSvc = new EmpService();
			byte[] productIcon = productSvc.getOneProduct(productVO.getProductId()).getProductIcon();
			productVO.setProductIcon(productIcon);
		} else {
			for (MultipartFile multipartFile : parts) {
				byte[] productIcon = multipartFile.getBytes();
				productVO.setProductIcon(productIcon);
			}
		}
		if (result.hasErrors()) {
			return "admin/product/update_product_input";
		}
		/*************************** 2.開始修改資料 *****************************************/
		// ProductService ProductSvc = new ProductService();
		productSvc.updateProduct(productVO);

		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/
		model.addAttribute("success", "- (修改成功)");
		productVO = productSvc.getOneProduct(Integer.valueOf(productVO.getProductId()));
		model.addAttribute("productVO", productVO);
		// 區分status上架及下架該去的位置
		if(Integer.valueOf(0).equals(productVO.getStatus())) {
			return "redirect:/admin/product/productRemoveArea"; // status=0
		}else {
			return "redirect:/admin/product/listAllProduct"; // status=1
		}
		
	}

	/*
	 * This method will be called on listAllProduct.html form submission, handling POST request
	 */
//	@PostMapping("delete")
//	public String delete(@RequestParam("productId") String productId, ModelMap model) {
//		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
//		/*************************** 2.開始刪除資料 *****************************************/
//		// EmpService empSvc = new EmpService();
//		productSvc.deleteProduct(Integer.valueOf(productId));
//		/*************************** 3.刪除完成,準備轉交(Send the Success view) **************/
//		List<ProductVO> list = productSvc.getAll();
//		model.addAttribute("productListData", list); // for listAllEmp.html 第85行用
//		model.addAttribute("success", "- (刪除成功)");
//		return "admin/product/listAllProduct"; // 刪除完成後轉交listAllEmp.html
//	}

	/*
	 * 第一種作法 Method used to populate the List Data in view. 如 : 
	 * <form:select path="deptno" id="deptno" items="${deptListData}" itemValue="deptno" itemLabel="dname" />
	 */
	@ModelAttribute("categoryListData")
	protected List<CategoryVO> referenceListData() {
		// DeptService deptSvc = new DeptService();
		List<CategoryVO> list = categorySvc.getAll();
		return list;
	}

	/*
	 * 【 第二種作法 】 Method used to populate the Map Data in view. 如 : 
	 * <form:select path="deptno" id="deptno" items="${depMapData}" />
	 */
//	@ModelAttribute("categoryMapData") //
//	protected Map<Integer, String> referenceMapData() {
//		Map<Integer, String> map = new LinkedHashMap<Integer, String>();
//		map.put(1, "奶粉類");
//		map.put(2, "母嬰用品類");
//		map.put(3, "桌椅類");
//		return map;
//	}

	// 去除BindingResult中某個欄位的FieldError紀錄
	public BindingResult removeFieldError(ProductVO productVO, BindingResult result, String removedFielcategoryName) {
		List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
				.filter(fielcategoryName -> !fielcategoryName.getField().equals(removedFielcategoryName))
				.collect(Collectors.toList());
		result = new BeanPropertyBindingResult(productVO, "productVO");
		for (FieldError fieldError : errorsListToKeep) {
			result.addError(fieldError);
		}
		return result;
	}
}
