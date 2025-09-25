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

    private final ProductService productSvc;
    private final WishlistProductService wishlistSvc;

    public ProductFrontEndController(ProductService productSvc,
                                     WishlistProductService wishlistSvc) {
        this.productSvc = productSvc;
        this.wishlistSvc = wishlistSvc;
    }

    @GetMapping("/shop/product-basic")
    public String showOneProduct(@RequestParam("id") Integer productId,
                                 ModelMap model,
                                 HttpSession session) {
        ProductVO oneProduct = productSvc.getOneProduct(productId);
        if (oneProduct == null) {
            // 找不到商品：你可改成丟 404 或導回列表
            return "redirect:/shop/left";
        }

        boolean favorited = false;
        MemberVO login = (MemberVO) session.getAttribute("member");
        if (login != null) {
            favorited = wishlistSvc.isFavorite(login.getMemberId(), productId);
        }

        model.addAttribute("product", oneProduct);
        model.addAttribute("favorited", favorited);
        return "frontend/shop-product-basic";
    }
}
