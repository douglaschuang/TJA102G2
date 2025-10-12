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

	/** 自動注入訂單明細的服務層，負責查詢 order_detail 資料表 */
	@Autowired
	private OrderDetailService orderDetailSvc;

	/** 自動注入訂單主檔的服務層，負責查詢 orders 資料表 */
	@Autowired
	private OrdersService ordersSvc;

	/**
	 * 【前台會員訂單明細頁】 路徑範例： /my-order-detail/{orderId} 
	 * → 例如： /my-order-detail/1001
	 * 
	 * 功能： 
	 * 	 會員登入後，可查看單筆訂單的詳細內容（含每個商品明細）。
	 */
	@GetMapping("/my-order-detail/{orderId}")
	public String myOrderDetail(@PathVariable Integer orderId, HttpSession session, Model model) {
		
		/** 1.檢查是否登入會員 */
		/**   若未登入，導回登入頁（/shop/login） */
		MemberVO member = (MemberVO) session.getAttribute("member");
		if (member == null) {
			return "redirect:/shop/login";
		}

		/** 2.依 orderId 查詢訂單主檔 */
		OrdersVO order = ordersSvc.getOneOrder(orderId);
		if (order == null) {
			/** 找不到訂單 → 導回「我的訂單列表」頁 */
			return "redirect:/my-orders";
		}
		/** 3.安全防呆：避免會員竄改網址看到別人的訂單 */
		/**   檢查該訂單的會員 ID 是否等於當前登入會員 ID */
		if (!order.getMemberVO().getMemberId().equals(member.getMemberId())) {
			/** 若不同，表示不是本人的訂單 → 導回列表  */
			return "redirect:/my-orders";
		}
		/** 4.查詢該筆訂單的明細資料（多筆商品） */
		List<OrderDetailVO> details = orderDetailSvc.findByOrderId(orderId);

		/** 5.將資料放入 model 給前端 Thymeleaf 頁面使用 */
		model.addAttribute("order", order);      // 訂單主檔資訊（編號、金額、收件人等）
		model.addAttribute("details", details);  // 明細清單（商品名稱、數量、單價、小計）

		/** 6.返回前端畫面：my-order-detail.html */
		return "frontend/my-order-detail";
	}
}
