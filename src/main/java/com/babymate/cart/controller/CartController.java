package com.babymate.cart.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.babymate.cart.model.CartItemDisplayVO;
import com.babymate.cart.model.CartRedisVO;
import com.babymate.cart.service.CartService;
import com.babymate.category.model.CategoryService;
import com.babymate.member.model.MemberVO;
import com.babymate.product.model.ProductService;
import com.babymate.product.model.ProductVO;

import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/cart")
public class CartController {
	
	@Autowired
	private ProductService productSvc;
	
	@Autowired
	private CategoryService categorySvc;

    private final CartService cartService;
    
    private static final String CART_KEY_MEMBER_PREFIX = "cart:member:";
    private static final String CART_KEY_SESSION_PREFIX = "cart:session:";

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    
    private String getCartKey(HttpSession session) {
    	MemberVO member = (MemberVO) session.getAttribute("member");
   	
        if (member != null) {
            return CART_KEY_MEMBER_PREFIX + member.getMemberId();
        } else {
            return CART_KEY_SESSION_PREFIX + session.getId();
        }
    }

    @GetMapping("/get/{memberId}")
    public List<CartRedisVO> getCart(@PathVariable Integer memberId, HttpSession session) {
    	String cartkey = getCartKey(session);
    	return cartService.getCart(cartkey);
    }

    @PostMapping("/add")
    public CartRedisVO addItem(@RequestParam Integer memberId,
                               @RequestParam Integer productId,
                               @RequestParam Integer quantity,
                               HttpSession session) {
    	String cartkey = getCartKey(session);
        return cartService.addOrUpdateItem(cartkey, productId, quantity);
    }
    
    @PostMapping("/addToCart")
    public ResponseEntity<String> addToCart(
    		@RequestParam Integer productId,
            @RequestParam Integer quantity,
            HttpSession session) {

    	String cartkey = getCartKey(session);
        
        cartService.addOrUpdateItem(cartkey, productId, quantity);
        return ResponseEntity.ok("加入購物車成功");
    }

    @PutMapping("/update")
    public CartRedisVO updateQuantity(@RequestParam Integer memberId,
                                      @RequestParam Integer productId,
                                      @RequestParam Integer quantity,
                                      HttpSession session) {
    	String cartkey = getCartKey(session);
        return cartService.updateQuantity(cartkey, productId, quantity);
    }

    @DeleteMapping("/del/{memberId}/{productId}")
    public void removeItem(@PathVariable Integer memberId,
                           @PathVariable Integer productId,
                           HttpSession session) {
    	String cartkey = getCartKey(session);
        cartService.removeItem(cartkey, productId);
    }

    @DeleteMapping("/clear/{memberId}")
    public void clearCart(@PathVariable Integer memberId, HttpSession session) {
    	String cartkey = getCartKey(session);
        cartService.clearCart(cartkey);
    }
    
}