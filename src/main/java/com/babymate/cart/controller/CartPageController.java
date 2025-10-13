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
import com.babymate.product.model.ProductService;
import com.babymate.product.model.ProductVO;

import jakarta.servlet.http.HttpSession;

/**
 * CartPageController
 *
 * <p>
 * 購物車頁面控制器，用於顯示會員或訪客的購物車內容。
 * </p>
 * 
 * <p>
 * 功能：
 * <ul>
 * <li>取得使用者的購物車（Redis 儲存）</li>
 * <li>整合商品資料，計算小計、總數量、產生 ECPay 用 itemName 字串</li>
 * <li>將資料回傳到前端模板 (frontend/shop-cart)</li>
 * </ul>
 * </p>
 * 
 * @see com.babymate.cart.service.CartService
 * @see com.babymate.product.model.ProductService
 */
@Controller
@RequestMapping("/cart")
public class CartPageController {

	@Autowired
	private CartService cartService;

	@Autowired
	private ProductService productSvc;

	/**
	 * 取得購物車 Key
	 *
	 * <p>
	 * 若會員已登入，使用會員 ID 作為 Key；否則使用 Session ID 作為 Key。
	 * </p>
	 *
	 * @param session HttpSession 物件
	 * @return 購物車在 Redis 的 Key
	 */
	private String getCartKey(HttpSession session) {
		MemberVO member = (MemberVO) session.getAttribute("member");
		if (member != null) {
			return "cart:member:" + member.getMemberId();
		} else {
			return "cart:session:" + session.getId();
		}
	}

	/**
	 * 顯示購物車頁面
	 *
	 * <p>
	 * 流程：
	 * <ol>
	 * <li>取得購物車 Key</li>
	 * <li>從 Redis 取得購物車項目</li>
	 * <li>查詢對應的商品資訊</li>
	 * <li>計算每個商品小計與購物車總額</li>
	 * <li>組合 ECPay itemName 字串</li>
	 * <li>將資料傳給前端模板渲染</li>
	 * </ol>
	 * </p>
	 *
	 * @param session HttpSession 物件
	 * @param model   Spring Model 物件，用於回傳前端資料
	 * @return 對應前端購物車頁面模板名稱
	 */
	@GetMapping("/viewCart")
	public String viewCart(HttpSession session, Model model) {
		String cartKey = getCartKey(session);
		int totalQty = 0;

		// 取得 Redis 購物車
		List<CartRedisVO> cartItems = cartService.getCart(cartKey);

		// 將 Redis 購物車資料轉換成前端顯示物件
		List<CartItemDisplayVO> displayItems = new ArrayList<>();
		BigDecimal subtotal = BigDecimal.ZERO;

		for (CartRedisVO item : cartItems) {
			ProductVO product = productSvc.getOneProduct(item.getProductId());

			// 計算每個商品的小計
			BigDecimal itemTotalPrice = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
			subtotal = subtotal.add(itemTotalPrice);

			// 組成前端顯示物件
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

		// 新增 itemName 字串（for ECPay）
		String itemName = displayItems.stream().map(item -> item.getProductName() + " x" + item.getQuantity())
				.collect(Collectors.joining("#"));
		model.addAttribute("itemName", itemName); // 傳給前端

		model.addAttribute("cartItems", displayItems);
		model.addAttribute("totalQty", totalQty);
		model.addAttribute("total", subtotal);

		return "frontend/shop-cart";
	}
}
