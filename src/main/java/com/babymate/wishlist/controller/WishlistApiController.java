package com.babymate.wishlist.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.babymate.wishlist.model.WishlistProductService;
import com.babymate.member.model.MemberVO;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistApiController {

  private final WishlistProductService wishlistSvc;
  public WishlistApiController(WishlistProductService wishlistSvc) { this.wishlistSvc = wishlistSvc; }

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

}
