package com.babymate.orderDetail.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.babymate.member.model.MemberVO;
import com.babymate.orderDetail.model.OrderDetailService;
import com.babymate.orderDetail.model.OrderDetailVO;
import com.babymate.orders.model.OrdersService;
import com.babymate.orders.model.OrdersVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping
public class OrderDetailFrontEndController {
	
	@Autowired
	private OrderDetailService orderDetailSvc;
	
	@Autowired
	private OrdersService ordersSvc;

	// 前台訂單明細頁
		@GetMapping("/my-order-detail/{orderId}")
		public String myOrderDetail(@PathVariable Integer orderId, HttpSession session, Model model) {
			MemberVO member = (MemberVO) session.getAttribute("member");
			if (member == null) {
				return "redirect:/shop/login";
			}
			
			// 1. 查主檔
	        OrdersVO order = ordersSvc.getOneOrder(orderId);
	        if (order == null) {
	            // 找不到就回列表或 404
	            return "redirect:/my-orders";
	        }
	        // 2. 安全防呆：避免看別人的訂單（可選）
	        if (!order.getMemberVO().getMemberId().equals(member.getMemberId())) {
	            return "redirect:/my-orders";
	        }
			// 3. 查詢訂單明細
			List<OrderDetailVO> details = orderDetailSvc.findByOrderId(orderId);
			
			// 4. 傳給前端 Thymeleaf
			model.addAttribute("order", order);
			model.addAttribute("details", details);
			
			return "frontend/my-order-detail";
		}
}
