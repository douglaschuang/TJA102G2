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
	private ProductService productSvc;
	
	@Autowired
	private CategoryService categorySvc;
	
	/**
	 * 顯示「新增商品」頁面
	 * - 建立空的 ProductVO 讓表單綁定
	 * - pageTitle 給前端做標題顯示
	 * - 回傳給後台 addProduct.html
	 */
	@GetMapping("addProduct")
	public String addProduct(ModelMap model) {
		ProductVO productVO = new ProductVO();
		model.addAttribute("productVO", productVO);
		/** 商品管理標題＿新增商品資料 */
		model.addAttribute("pageTitle", "商品管理｜新增商品資料");
		return "admin/product/addProduct";
	}
	
	/**
	 * 接收「新增商品」表單提交
	 * - @Valid 觸發對 ProductVO 的欄位驗證
	 * - BindingResult 取得驗證結果
	 * - 圖片上傳：取 parts[0] 並存到 productIcon（byte[]）
	 * - 成功後導向 listAllProduct.html
	 */
	@PostMapping("insert")
	public String insert(@Valid ProductVO productVO, BindingResult result, ModelMap model,
			@RequestParam("productIcon") MultipartFile[] parts) throws IOException{

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 移除指定欄位(productIcon)的驗證錯誤，避免「未上傳圖片」直接擋住整個流程
		result = removeFieldError(productVO, result, "productIcon");

		// 檢查是否有上傳圖片（此處採用至少一張圖片的策略）
		if (parts[0].isEmpty()) { // 使用者未選擇要上傳的圖片時
			model.addAttribute("errorMessage", "商品圖片: 請上傳商品圖片");
		} else {
			// 多檔迭代：實際上只會以最後一次迭代的圖片覆蓋到 productIcon
			// 只留最後一張圖的 byte[]
			for (MultipartFile multipartFile : parts) {
				byte[] buf = multipartFile.getBytes();
				productVO.setProductIcon(buf);
			}
		}
		
		// 若驗證有錯或沒上傳圖，回到新增頁
		if (result.hasErrors() || parts[0].isEmpty()) {
			return "admin/product/addProduct";
		}
		/*************************** 2.開始新增資料 *****************************************/
		productSvc.addProduct(productVO);
		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/
		// 注意：下兩行放到 model 的資料，在 redirect 後不會保留（如需顯示成功訊息可改用 Flash Attribute）
		List<ProductVO> list = productSvc.getAll();
		model.addAttribute("productListData", list);
		model.addAttribute("success", "- (新增成功)");
		return "redirect:/admin/product/listAllProduct";
	}
	
	/**
	 * 從列表頁點「修改」→ 依 product_id 載入資料並前往編輯頁
	 * 查詢完成後，導向至 update_product_input.html
	 */
	@PostMapping("getOne_For_Update")
	public String getOne_For_Update(@RequestParam("product_id") String productId, ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始查詢資料 *****************************************/
		ProductVO productVO = productSvc.getOneProduct(Integer.valueOf(productId));

		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("productVO", productVO);
		// 商品管理標題＿修改商品資料
		model.addAttribute("pageTitle", "商品管理｜修改商品資料");
		return "admin/product/update_product_input"; // 查詢完成後轉交update_product_input.html
	}
	
	/**
	 * 從列表頁點「檢視」→ 依 product_id 載入資料並前往檢視頁
	 * 回傳樣板：admin/product/productview.html
	 */
	@PostMapping("getOne_For_Detail")
	public String getOne_For_Detail(
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		@RequestParam("product_id") String productId,
		ModelMap model) {
		/*************************** 2.開始查詢資料 *****************************************/
		ProductVO productVO = productSvc.getOneProduct(Integer.valueOf(productId));
		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("productVO", productVO);
		// 商品管理標題＿檢視商品資料
		model.addAttribute("pageTitle", "商品管理｜檢視商品資料");
		return "admin/product/productview"; // 查詢完成後轉交update_product_input.html
	}

	/**
	 * 顯示「上架區」列表頁（status=1）
	 */
	@GetMapping("listAllProduct")
	public String listAllProduct(ModelMap model) {
		// 上架清單(上架區) findByStatus(1)
		model.addAttribute("productListData", productSvc.findByStatus(1));
		// 商品管理標題＿上架區
		model.addAttribute("pageTitle", "商品管理｜上架區");
		return "admin/product/listAllProduct";
	}
	
	/**
	 * 顯示「下架區」列表頁（status=0）
	 */
	@GetMapping("productRemoveArea")
	public String productRemoveArea(ModelMap model) {
		// 下架清單(下架區) findByStatus(0)
		model.addAttribute("productListData", productSvc.findByStatus(0));
		// 商品管理標題＿下架區
		model.addAttribute("pageTitle", "商品管理｜下架區");
		return "admin/product/productRemoveArea";
	}
	
	
	/**
	 * 接收「商品修改」表單提交
	 * - 若未上傳新圖片：沿用舊圖（從 DB 讀舊圖）
	 * - 成功後：依商品狀態導回上架/下架列表
	 */
	@PostMapping("update")
	public String update(@Valid ProductVO productVO, BindingResult result, ModelMap model,
			@RequestParam("productIcon") MultipartFile[] parts) throws IOException {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 同新增：先把 productIcon 欄位的驗證錯誤移除
		result = removeFieldError(productVO, result, "productIcon");

		if (parts[0].isEmpty()) { // 未提供新圖 → 使用舊圖避免覆蓋成 null
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
		/*************************** 2.開始更新資料 *****************************************/
		productSvc.updateProduct(productVO);

		/*************************** 3.分流導頁：依照 status 決定導回上架或下架頁 **************/
		model.addAttribute("success", "- (修改成功)");
		productVO = productSvc.getOneProduct(Integer.valueOf(productVO.getProductId()));
		model.addAttribute("productVO", productVO);
		// 區分status上架及下架該去的位置
		if(Integer.valueOf(0).equals(productVO.getStatus())) {
			return "redirect:/admin/product/productRemoveArea"; // 下架
		}else {
			return "redirect:/admin/product/listAllProduct"; // 上架
		}
		
	}

	/**
	 * 供前端表單下拉選單使用：回傳「分類清單」
	 * 使用方式（Thymeleaf 舉例）：
	 * <select th:field="*{categoryVO.categoryId}" style="width: 600px; height:30px;">
	 *   <option th:each="categoryVO : ${categoryListData}"
	 *			 th:value="${categoryVO.categoryId}" th:text="${categoryVO.categoryId} + '-[' + ${categoryVO.categoryName} + ']'" >
	 *	 </option>
	 * </select>
	 */
	@ModelAttribute("categoryListData")
	protected List<CategoryVO> referenceListData() {
		List<CategoryVO> list = categorySvc.getAll();
		return list;
	}

	/**
	 * 自訂工具方法：從 BindingResult 移除特定欄位的 FieldError
	 * 典型用途：檔案上傳欄位在某些情境下暫時不檢查（例如修改表單允許不換圖）
	 */
	public BindingResult removeFieldError(ProductVO productVO, BindingResult result, String removedFielcategoryName) {
		// 1.取出目前所有欄位錯誤，過濾掉指定欄位（例如 "productIcon"）的錯誤
		List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
				.filter(fielcategoryName -> !fielcategoryName.getField().equals(removedFielcategoryName))
				.collect(Collectors.toList());
		// 2.建一個全新的 BindingResult（同樣綁定到 productVO）
		result = new BeanPropertyBindingResult(productVO, "productVO");
		// 3.把剛剛保留的「其他欄位」錯誤加回來（唯獨指定欄位的錯誤不加）
		for (FieldError fieldError : errorsListToKeep) {
			result.addError(fieldError);
		}
		return result;
	}
}
