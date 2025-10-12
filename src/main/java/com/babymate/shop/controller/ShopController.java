package com.babymate.shop.controller;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.babymate.category.model.CategoryCountDTO;
import com.babymate.category.model.CategoryService;
import com.babymate.member.model.MemberVO;
import com.babymate.product.model.ProductService;
import com.babymate.wishlist.model.WishlistProductService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ShopController {

	/** 商品查詢服務（依條件篩選、排序） */
	@Autowired
	ProductService productSvc;
	
	/** 取得分類與商品數 */
	@Autowired
	CategoryService categorySvc;
	
	/** 願望清單（用來標記愛心） */
	@Autowired
    WishlistProductService wishlistSvc; // ★ 加這行

	public ShopController(ProductService productSvc) {
		super();
		this.productSvc = productSvc;
	}
	
	/**
	 * 前台商城左側列表頁（含分類篩選、價格、排序、我的收藏）
	 * URL 對應：/shop/left → /templates/frontend/shop-left-sidebar.html
	 *
	 * 參數：
	 * - sort：排序（預設 newProduct，也可 priceAsc、priceDesc）
	 * - categoryId：分類（可為 null 代表全部）
	 * - minPrice/maxPrice：價格篩選（可為 null）
	 *
	 * 流程：
	 *  1.依條件呼叫 productSvc.findByCondition(...) 取得商品清單
	 *  2.載入分類清單（含上架商品數量）與「全部商品總數」
	 *  3.將目前篩選條件（categoryId/minPrice/maxPrice/sort）回灌 model，方便頁面顯示
	 *  4.若已登入，載入會員收藏的商品 id 集合，讓前端可點亮愛心
	 */
	@GetMapping("/shop/left")
	public String shopLeftSidebar(
			@RequestParam(defaultValue = "newProduct") String sort,
			@RequestParam(required = false) Integer categoryId,
			@RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice,
			ModelMap model,
			HttpSession session // ★ 帶入 session
			){
		/** 1.商品清單(依分類與價格篩選＋排序) */
		var products = productSvc.findByCondition(categoryId, minPrice, maxPrice, sort);
	    model.addAttribute("hotProducts", products); // 頁面上實際就是商品列表
	    model.addAttribute("sort", sort);
		
		/** 2.分類清單(含每個分類的商品數量) */
		var categories = categorySvc.listWithCount();
		model.addAttribute("categories", categories);
		
		/** 3.全部上架商品總數（把各分類 count 加總） */
		long allCount = categories.stream().mapToLong(CategoryCountDTO::count).sum();
		model.addAttribute("allCount", allCount);
		
		/** 4.回灌目前選擇（頁面要帶回條件才能顯示「使用者目前的篩選狀態」） */
		model.addAttribute("selectedCategoryId", categoryId);
		model.addAttribute("minPrice", minPrice);
		model.addAttribute("maxPrice", maxPrice);
		
		/** 5.會員收藏的商品 id 集合（未登入 → 空集合；登入 → 把會員收藏的商品 id 給前端） */
        Set<Integer> favIds = java.util.Collections.emptySet();
        MemberVO login = (MemberVO) session.getAttribute("member"); // 你的 session key
        if (login != null) {
            favIds = wishlistSvc.listByMember(login.getMemberId())
                                .stream()
                                .map(r -> r.getProductId())
                                .collect(Collectors.toSet());
        }
        model.addAttribute("favIds", favIds); // ★ 放進 model
			
		return "frontend/shop-left-sidebar";
	}
}
