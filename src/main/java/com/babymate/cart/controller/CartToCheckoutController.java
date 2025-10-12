package com.babymate.cart.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.babymate.cart.model.CartItemDisplayVO;
import com.babymate.cart.model.CartRedisVO;
import com.babymate.cart.service.CartService;
import com.babymate.member.model.MemberVO;
import com.babymate.orders.model.OrdersService;
import com.babymate.orders.model.OrdersVO;
import com.babymate.product.model.ProductService;
import com.babymate.product.model.ProductVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/shop")
public class CartToCheckoutController {
	
	/** 讀取購物車項目 (Redis) */
	@Autowired
	private CartService cartSvc;
	
	/** 查詢商品資訊（價格、名稱、圖片） */
	@Autowired
	private ProductService productSvc;
	
	/** 建立暫存訂單（未付款） */
	@Autowired
	private OrdersService ordersSvc;
	
	/**
	 * 根據是否登入決定使用「會員購物車 key」或「匿名 session 購物車 key」
	 * - 會員： cart:member:{memberId}
	 * - 訪客： cart:session:{sessionId}
	 */
	private String getCartKey(HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute("member");
        /** 使用 if else 判斷會員或訪客 */
//        if (member != null) {
//            return "cart:member:" + member.getMemberId();
//        } else {
//            return "cart:session:" + session.getId();
//        }
        
        /** 判斷會員或訪客(簡化版) */
        return (member != null) ? "cart:member:" + member.getMemberId() 
        						: "cart:session:" + session.getId();
    }
	
	/**
	 * 結帳頁（顯示購物車明細，並建立一筆「未付款訂單」）
	 * URL：/shop/checkout
	 *
	 * 流程：
	 *  1.檢查是否登入（未登入→導向登入頁）
	 *  2.讀取購物車（Redis）。若為空→導回購物車頁
	 *  3.將購物車項目轉成前端顯示用物件（名稱/單價/數量/小計/圖片）
	 *  4.小計金額、總數量、ECPay ItemName（"A x2#B x1"）
	 *  5.建立一筆「未付款訂單」，拿到 orderNo，放入 session
	 *  6.將明細與金額放入 model，回傳 checkout 頁面
	 */
	@GetMapping("/checkout")
	public String checkout(HttpSession session, Model model) {
		/** 1.未登入的話，要先導向登入介面 */
		MemberVO member = (MemberVO) session.getAttribute("member");
		if (member == null) {
			return "redirect:/shop/login";
		}
		
		/** 2.取得 Redis 購物車 */
		String cartKey = getCartKey(session);
		List<CartRedisVO> cartItems = cartSvc.getCart(cartKey);
		
		/** 3.如果沒東西就回購物車 */
		if(cartItems.isEmpty()) {
			return "redirect:/shop/cart";
		}
		
		/** 4.轉成前端顯示用 VO + 計算總金額 / 數量 + itemName 給綠界 */
        List<CartItemDisplayVO> displayItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalQty = 0;
        		
        for (CartRedisVO item : cartItems) {
            ProductVO product = productSvc.getOneProduct(item.getProductId());
            BigDecimal itemTotalPrice = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotalPrice);

            CartItemDisplayVO displayVO = new CartItemDisplayVO();
            displayVO.setProductId(product.getProductId());
            displayVO.setProductName(product.getProductName());
            displayVO.setPrice(product.getPrice());
            displayVO.setQuantity(item.getQuantity());
            displayVO.setTotalPrice(itemTotalPrice);
            displayVO.setImageUrl(product.getProductIcon());
            displayItems.add(displayVO);
            totalQty += item.getQuantity();
        }

        /** 5.綠界 ItemName 格式：商品A x2#商品B x1 */
        String itemName = displayItems.stream()
							          .map(i -> i.getProductName() + " x" + i.getQuantity())
							          .collect(Collectors.joining("#"));
        
        /** 6.這裡「先建一筆未付款訂單」並拿到 orderNo */
        /**   收件資訊目前沒有就先留空字串，之後你也可以在 checkout form 帶回來更新 */
        String orderNo = "EC" + System.currentTimeMillis();
        OrdersVO order = ordersSvc.createPendingOrder(
                member,
                subtotal,
                displayItems,
                "", "", "", "", // recipient/address/phone/email 先空
                "");            // remark 先空

        /**   把這筆 orderNo 放 session，等送到 /ecpay/checkout 一起帶出去 */
        session.setAttribute("currentOrderNo", order.getOrderNo());

        /** 7.給前端顯示 */
        model.addAttribute("cartItems", displayItems);
        model.addAttribute("totalQty", totalQty);
        model.addAttribute("total", subtotal);
        model.addAttribute("itemName", itemName);
        model.addAttribute("orderNo", order.getOrderNo()); // 給 ecpaySubmit 的 hidden 使用

        return "frontend/shop-checkout";
	}
}
