package com.babymate.product.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.babymate.member.model.MemberVO;
import com.babymate.product.model.ProductService;
import com.babymate.product.model.ProductVO;
import com.babymate.wishlist.model.WishlistProductService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProductFrontEndController {

	/** 建構式注入：告訴 Spring 在建立 Controller 時要注入這兩個 Service，final 不可被更改 */
    private final ProductService productSvc;
    private final WishlistProductService wishlistSvc;

    /** 有參數建構子，初始化成員變數 */
    public ProductFrontEndController(ProductService productSvc,
                                     WishlistProductService wishlistSvc) {
        this.productSvc = productSvc;
        this.wishlistSvc = wishlistSvc;
    }

    /**
     * 前台「商品詳情」頁面控制器
     * 網址對應：/shop/product-basic?id=商品ID
     *
     * 功能說明：
     *  1️⃣ 從 productSvc 依商品 ID 查出一筆商品資料。
     *  2️⃣ 若查無資料 → 導回商品列表頁 (/shop/left)。
     *  3️⃣ 若會員已登入，檢查該商品是否在願望清單中。
     *  4️⃣ 把商品資訊與收藏狀態放入 model，回傳樣板「frontend/shop-product-basic.html」。
     */
    @GetMapping("/shop/product-basic")
    public String showOneProduct(@RequestParam("id") Integer productId,
                                 ModelMap model,
                                 HttpSession session) {
    	
    	// 1.查詢商品資料
        ProductVO oneProduct = productSvc.getOneProduct(productId);
        if (oneProduct == null) {
            // 找不到商品：你可改成丟 404 或導回列表
            return "redirect:/shop/left";
        }

        // 2.判斷已登入會員是否已收藏該商品
        boolean favorited = false;
        MemberVO login = (MemberVO) session.getAttribute("member");
        if (login != null) {
            favorited = wishlistSvc.isFavorite(login.getMemberId(), productId);
        }

        // 3.將資料放入model，供前台商城 Thymeleaf 顯示
        model.addAttribute("product", oneProduct);
        model.addAttribute("favorited", favorited);
        return "frontend/shop-product-basic";
    }
}
