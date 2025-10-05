package com.babymate.cart.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.ResponseBody;
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
    
    @GetMapping("/getCartDetail")
    public ResponseEntity<?> getCartList(HttpSession session) {
        String cartKey = getCartKey(session);
        List<CartRedisVO> cartItems = cartService.getCart(cartKey);

        int totalQty = 0;
        BigDecimal total = BigDecimal.ZERO;

        List<Map<String, Object>> items = new ArrayList<>();
        
        for (CartRedisVO item : cartItems) {
            ProductVO product = productSvc.getOneProduct(item.getProductId());
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalQty += item.getQuantity();
            total = total.add(lineTotal);

            Map<String, Object> row = new HashMap<>();
            row.put("productId", product.getProductId());
            row.put("name", product.getProductName());
            row.put("imageUrl", "/product/DBGifReader?productId=" + product.getProductId()); 
            row.put("price", product.getPrice());
            row.put("quantity", item.getQuantity());
            items.add(row);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalQty", totalQty);
        result.put("total", total);
        result.put("items", items);

        return ResponseEntity.ok(result);
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
    	
        if (quantity == null || quantity <= 0) {
            return ResponseEntity.badRequest().body("數量必須大於 0");
        }

    	String cartkey = getCartKey(session);
        
        cartService.addOrUpdateItem(cartkey, productId, quantity);
        return ResponseEntity.ok("加入購物車成功");
    }

//    @PutMapping("/update")
//    public CartRedisVO updateQuantity(@RequestParam Integer memberId,
//                                      @RequestParam Integer productId,
//                                      @RequestParam Integer quantity,
//                                      HttpSession session) {
//    	String cartkey = getCartKey(session);
//        return cartService.updateQuantity(cartkey, productId, quantity);
//    }
    
    @PutMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateItem(@RequestParam Integer productId,
    		@RequestParam Integer quantity,
            HttpSession session) {
		String cartKey = getCartKey(session);
		
		if (quantity <= 0) {
		// 數量 <= 0 → 刪除該商品
		cartService.removeItem(cartKey, productId);
		} else {
		// 更新數量
		cartService.updateQuantity(cartKey, productId, quantity);
		}
		
		// 重新計算購物車資訊
		List<CartRedisVO> cartItems = cartService.getCart(cartKey);
		
		int totalQty = 0;
		BigDecimal total = BigDecimal.ZERO;
		BigDecimal itemTotal = BigDecimal.ZERO;
		
		for (CartRedisVO item : cartItems) {
			ProductVO product = productSvc.getOneProduct(item.getProductId());
			BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
			totalQty += item.getQuantity();
			total = total.add(lineTotal);
		
			if (item.getProductId().equals(productId)) {
				itemTotal = lineTotal;
			}
		}
		
		Map<String, Object> result = new HashMap<>();
		result.put("totalQty", totalQty);
		result.put("total", total);
		result.put("itemTotal", itemTotal);
		
		return ResponseEntity.ok(result);
	}

//    @DeleteMapping("/del/{productId}")
//    public void removeItem(@PathVariable Integer productId,
//                           HttpSession session) {
//    	String cartkey = getCartKey(session);
//        cartService.removeItem(cartkey, productId);
//    }
    
    @DeleteMapping("/del/{productId}")
    @ResponseBody
    public ResponseEntity<?> removeItem(@PathVariable Integer productId,
                                        HttpSession session) {
        String cartkey = getCartKey(session);

        // 刪除商品
        cartService.removeItem(cartkey, productId);

        // 重新計算購物車資訊
        List<CartRedisVO> cartItems = cartService.getCart(cartkey);

        int totalQty = 0;
        BigDecimal total = BigDecimal.ZERO;

        for (CartRedisVO item : cartItems) {
            ProductVO product = productSvc.getOneProduct(item.getProductId());
            totalQty += item.getQuantity();
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalQty", totalQty);
        result.put("total", total);

        return ResponseEntity.ok(result);
    }

    // 在綠界結帳跳回商城頁面時session已經丟失，getCartKey(session)會拿不到對應的會員或session購物車
//    @DeleteMapping("/clear/{memberId}")
//    public void clearCart(@PathVariable Integer memberId, HttpSession session) {
//    	String cartkey = getCartKey(session);
//        cartService.clearCart(cartkey);
//    }
    
    @DeleteMapping("/clear/{memberId}")
    public void clearCart(@PathVariable Integer memberId) {
        String cartKey = "cart:member:" + memberId;
        cartService.clearCart(cartKey);
    }
}