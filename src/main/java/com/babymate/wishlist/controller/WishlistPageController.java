package com.babymate.wishlist.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.babymate.wishlist.model.WishlistProductService;
import com.babymate.product.model.ProductService;
import com.babymate.product.model.ProductVO;
import com.babymate.member.model.MemberVO;

import jakarta.servlet.http.HttpSession;

@Controller
public class WishlistPageController {

  private final WishlistProductService wishlistSvc;
  private final ProductService productSvc;

  public WishlistPageController(WishlistProductService wishlistSvc, ProductService productSvc) {
    this.wishlistSvc = wishlistSvc;
    this.productSvc = productSvc;
  }

  @GetMapping("/shop/wishlist")
  public String wishlist(Model model, HttpSession session) {
    MemberVO login = (MemberVO) session.getAttribute("member");
    if (login == null) return "redirect:/shop/login";

    var rows = wishlistSvc.listByMember(login.getMemberId());
    List<ProductVO> products = rows.stream()
        .map(r -> productSvc.getOneProduct(r.getProductId()))
        .filter(p -> p != null)
        .toList();

    Set<Integer> favIds = products.stream().map(ProductVO::getProductId).collect(Collectors.toSet());
    model.addAttribute("wishlistProducts", products);
    model.addAttribute("favIds", favIds);
    return "frontend/shop-wishlist";
  }
}
