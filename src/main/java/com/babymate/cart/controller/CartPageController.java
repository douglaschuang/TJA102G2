package com.babymate.cart.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

@Controller
@RequestMapping("/cart")
public class CartPageController {

    @Autowired
    private CartService cartService;
    
    @Autowired
    private ProductService productSvc;

    private String getCartKey(HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute("member");
        if (member != null) {
            return "cart:member:" + member.getMemberId();
        } else {
            return "cart:session:" + session.getId();
        }
    }
    
    @GetMapping("/viewCart")
    public String viewCart(HttpSession session, Model model) {
        String cartKey = getCartKey(session);
        int totalQty = 0;
        System.out.println(session.getId());
        System.out.println(cartKey);
        // 取得 Redis 購物車
        List<CartRedisVO> cartItems = cartService.getCart(cartKey);

        // 取 ProductVO 資料
        List<CartItemDisplayVO> displayItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartRedisVO item : cartItems) {
            ProductVO product = productSvc.getOneProduct(item.getProductId());
//            double totalPrice = product.getPrice() * item.getQuantity();
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

        model.addAttribute("cartItems", displayItems);
        model.addAttribute("totalQty", totalQty);
        model.addAttribute("total", subtotal); // 可加運費或其他費用再計算

        return "frontend/shop-cart"; // 對應 cart.html
    }
}
