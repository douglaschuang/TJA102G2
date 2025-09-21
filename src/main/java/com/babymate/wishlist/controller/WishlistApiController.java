package com.babymate.wishlist.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.babymate.member.model.MemberVO;
import com.babymate.product.model.ProductService;
import com.babymate.wishlist.model.WishlistProductService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistApiController {

  private final WishlistProductService wishlistSvc;
  private final ProductService productSvc; // ★ 加入

  public WishlistApiController(WishlistProductService wishlistSvc, ProductService productSvc) {
    this.wishlistSvc = wishlistSvc;
    this.productSvc = productSvc; // ★ 加入
  }

  @PostMapping("/toggle")
  public ResponseEntity<?> toggle(@RequestParam("productId") Integer productId, HttpSession session){
    MemberVO login = (MemberVO) session.getAttribute("member");
    if (login == null) {
      return ResponseEntity.status(401).body(Map.of(
        "ok", false,
        "message", "請先登入",
        "redirect", "/shop/login"
      ));
    }
    boolean added = wishlistSvc.toggle(login.getMemberId(), productId);
    return ResponseEntity.ok(Map.of("ok", true, "favorited", added));
  }

  @GetMapping("/status")
  public ResponseEntity<?> status(@RequestParam("productId") Integer productId, HttpSession session){
    MemberVO login = (MemberVO) session.getAttribute("member");
    boolean favored = (login != null) && wishlistSvc.isFavorite(login.getMemberId(), productId);
    return ResponseEntity.ok(Map.of("favorited", favored));
  }

  @GetMapping("/ping")
  public Map<String,Object> ping(){ return Map.of("ok", true); }

  @GetMapping("/mini")
  public ResponseEntity<?> mini(HttpSession session){
    MemberVO login = (MemberVO) session.getAttribute("member");
    if (login == null) {
      return ResponseEntity.status(401).body(Map.of(
        "ok", false,
        "message", "請先登入",
        "redirect", "/shop/login"
      ));
    }

    var rows = wishlistSvc.listByMember(login.getMemberId());

    // ★ 明確型別 + Map.<String,Object>of 避免推導失敗
    List<Map<String, Object>> items = rows.stream()
        .map(r -> r.getProductId())
        .map(pid -> productSvc.getOneProduct(pid))
        .filter(p -> p != null)
        .map(p -> Map.<String, Object>of(
            "productId", p.getProductId(),
            "name",      p.getProductName(),
            "price",     p.getPrice(), // BigDecimal 也沒問題
            "imageUrl",  "/product/DBGifReader?productId=" + p.getProductId(),
            "linkUrl",   "/shop/product-basic?id=" + p.getProductId()
        ))
        .collect(Collectors.toList()); // 若非 JDK16+ 請用 Collectors

    return ResponseEntity.ok(Map.of(
        "ok", true,
        "items", items
    ));
  }
}
