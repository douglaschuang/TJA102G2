package com.babymate.cart.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.babymate.cart.model.CartRedisVO;
import com.babymate.cart.service.CartService;
import com.babymate.member.model.MemberVO;
import com.babymate.product.model.ProductService;
import com.babymate.product.model.ProductVO;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/cart")
public class CartController {
	
	@Autowired
	private ProductService productSvc;
	
    private final CartService cartService;
    
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);
    
    private static final String CART_KEY_MEMBER_PREFIX = "cart:member:";
    private static final String CART_KEY_SESSION_PREFIX = "cart:session:";

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    
    /**
     * 根據當前 session 取得購物車的唯一識別 key。
     *
     * <p>此方法會檢查當前 session 是否與已登入的會員關聯，若是已登入會員，
     * 則返回以會員 ID 作為識別的購物車 key；若是訪客，則返回基於 session ID 的購物車 key。</p>
     *
     * @param session 當前 HTTP session，包含當前用戶的登入狀態或訪客狀態。
     * @return 以會員 ID 或 session ID 為基礎生成的購物車唯一識別 key。
     */
    private String getCartKey(HttpSession session) {
    	// 從 session 中取得當前登入的會員資訊
    	MemberVO member = (MemberVO) session.getAttribute("member");
   	
        if (member != null) {
        	// 若是已登入會員，使用會員 ID 作為購物車的唯一識別 key
        	logger.info("getCart - cartkey={}",CART_KEY_MEMBER_PREFIX + member.getMemberId());
            return CART_KEY_MEMBER_PREFIX + member.getMemberId();
            
        } else {
        	// 若未登入，則使用 session ID 作為購物車的唯一識別 key
        	logger.info("getCart - cartkey={}",CART_KEY_MEMBER_PREFIX + session.getId());
            return CART_KEY_SESSION_PREFIX + session.getId();
        }
    }

    /**
     * 根據當前 session 獲取用戶的購物車商品列表。
     *
     * <p>此方法會根據當前 session 生成唯一的購物車識別 key，然後透過該 key 從 Redis 中獲取對應的購物車商品列表。</p>
     *
     * @param memberId 會員 ID，用於路由映射，實際上在這個方法中並未使用，但可以作為識別符或未來擴展的參數。
     * @param session 當前的 HTTP session，用來獲取或生成購物車的唯一識別 key。
     * @return 返回購物車內的所有商品詳細資料，格式為 `List<CartRedisVO>`。
     */
    @GetMapping("/get/{memberId}")
    public List<CartRedisVO> getCart(@PathVariable Integer memberId, HttpSession session) {
    	String cartkey = getCartKey(session);
    	
    	// 根據 cartkey 從 Redis 中獲取對應的購物車商品列表
    	return cartService.getCart(cartkey);
    }
    
    /**
     * 取得購物車內所有商品的明細資料。
     *
     * <p>根據目前的 session，從 Redis 取得購物車內容，並整合每個商品的詳細資訊，
     * 包含商品名稱、價格、數量、總金額等，回傳 JSON 格式的結果。</p>
     *
     * @param session 當前使用者的 HTTP session
     * @return 包含商品清單、總數量與總金額的 ResponseEntity（JSON 格式）
     */
    @GetMapping("/getCartDetail")
    public ResponseEntity<?> getCartList(HttpSession session) {
        String cartKey = getCartKey(session);
        List<CartRedisVO> cartItems = cartService.getCart(cartKey);

        int totalQty = 0;
        BigDecimal total = BigDecimal.ZERO;

        List<Map<String, Object>> items = new ArrayList<>();
        
        // List all items in cart
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
    
    /**
     * 新增或更新購物車中的商品項目。
     *
     * <p>根據傳入的商品編號與數量，將該商品新增至購物車，
     * 若購物車中已存在該商品，則會更新其數量。</p>
     *
     * @param memberId 會員編號（目前未直接使用，作為後續擴充用）
     * @param productId 商品編號
     * @param quantity 數量
     * @param session 當前使用者的 HTTP session，用於判斷購物車 key（會員或訪客）
     * @return 更新後的購物車項目資料（CartRedisVO）
     */
    @PostMapping("/add")
    public CartRedisVO addItem(@RequestParam Integer memberId,
                               @RequestParam Integer productId,
                               @RequestParam Integer quantity,
                               HttpSession session) {
    	String cartkey = getCartKey(session);
        return cartService.addOrUpdateItem(cartkey, productId, quantity);
    }
    
    /**
     * 新增商品到購物車。
     *
     * <p>根據傳入的商品編號與數量，將該商品新增至購物車，
     * 若購物車中已存在該商品，則會更新其數量。</p>
     *
     * @param productId 商品編號
     * @param quantity 數量
     * @param session 當前使用者的 HTTP session，用於判斷購物車 key（會員或訪客）
     * @return ResponseEntity<String> 加入購物車成功
     */
    @PostMapping("/addToCart")
    public ResponseEntity<String> addToCart(
    		@RequestParam Integer productId,
            @RequestParam Integer quantity,
            HttpSession session) {
    	
    	// 驗證數量有效性
        if (quantity == null || quantity <= 0) {
            return ResponseEntity.badRequest().body("數量必須大於 0");
        }

    	String cartkey = getCartKey(session);
        
    	// 新增商品或更新購物車中的商品
        cartService.addOrUpdateItem(cartkey, productId, quantity);
        return ResponseEntity.ok("加入購物車成功");
    }

    /**
     * 將指定商品加入購物車或更新數量。
     *
     * <p>根據傳入的商品編號與數量，將該商品加入購物車。如果商品數量無效（<= 0），
     * 會回傳錯誤訊息。若商品已存在於購物車中，則更新該商品的數量。</p>
     *
     * @param productId 商品編號
     * @param quantity 需要新增的數量（必須大於 0）
     * @param session 目前使用者的 HTTP session，用於識別購物車 key（針對不同用戶）
     * @return 回應結果，包含成功或錯誤訊息
     */
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

    /**
     * 從購物車中刪除指定商品。
     *
     * <p>根據商品 ID 刪除購物車中的商品，並重新計算購物車的總數量與總金額。
     * 刪除後會回傳更新後的購物車資訊（包括總數量和總金額）。</p>
     *
     * @param productId 商品 ID，表示要從購物車中刪除的商品。
     * @param session 當前使用者的 HTTP session，用於識別購物車 key（針對不同用戶）
     * @return 更新後的購物車資訊，包括總數量（totalQty）和總金額（total）。
     */
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

     // 遍歷購物車中的每一項商品，計算總數量與總金額
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

    /**
     * 清空指定會員的購物車。
     *
     * <p>此方法會根據會員的 ID 清空對應的購物車。它會根據 session 中的購物車 key，
     * 呼叫服務層的方法來清空購物車中的所有商品。</p>
     *
     * @param memberId 會員的 ID，用於識別要清空購物車的會員。
     * @param session 當前用戶的 HTTP session，用於取得購物車的唯一識別 key。
     */
    @DeleteMapping("/clear/{memberId}")
    public void clearCart(@PathVariable Integer memberId, HttpSession session) {
    	// 取得購物車唯一值
    	String cartkey = getCartKey(session);
    	
    	// 清空購物車
        cartService.clearCart(cartkey);
    }
    
}