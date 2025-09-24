package com.babymate.orders.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
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
@RequestMapping("/my-orders")
public class OrdersFrontEndController {
	
	@Autowired
	private OrderDetailService orderDetailSvc;
	
	@Autowired
	private OrdersService ordersSvc;
	
	@GetMapping
	public String myOrders(HttpSession session, ModelMap model) {
		MemberVO member = (MemberVO) session.getAttribute("member");
		if (member == null) {
			return "redirect:/shop/login";
		}
		
		List<OrdersVO> myOrders = ordersSvc.getOrdersByMemberId(member.getMemberId());
		model.addAttribute("orders", myOrders);
		return "frontend/my-orders";
	}

}
