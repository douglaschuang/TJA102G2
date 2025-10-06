package com.babymate.orderDetail.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.babymate.orderDetail.model.OrderDetailService;
import com.babymate.orderDetail.model.OrderDetailVO;
import com.babymate.orders.model.OrdersService;
import com.babymate.orders.model.OrdersVO;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/orderdetail")
public class OrderDetailController {
	
	@Autowired
	OrderDetailService orderDetailSvc;
	
	@Autowired
	OrdersService ordersSvc;

	public OrderDetailController() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	// 去除BindingResult中某個欄位的FieldError紀錄
			public BindingResult removeFieldError(OrderDetailVO orderDetailVO, BindingResult result,
					String removedFieldname) {
				List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
						.filter(fieldname -> !fieldname.getField().equals(removedFieldname)).collect(Collectors.toList());
				result = new BeanPropertyBindingResult(orderDetailVO, "orderDetailVO");
				for (FieldError fieldError : errorsListToKeep) {
					result.addError(fieldError);
				}
				return result;
			}
			
//			@GetMapping("list")
//			public String listAllOrders(@RequestParam(value = "orderDetailId", required = false) Integer orderDetailId, Model model) {
//			    
//			    List<OrderDetailVO> list;
//			    
//			    if (orderDetailId != null) {
//			    	OrderDetailVO orderDetail = orderDetailSvc.getOneOrderDetailId(orderDetailId);
//			        model.addAttribute("orderDetail", orderDetail);
//
//			        list = orderDetailSvc.findByorderDetailId(orderDetailId);
//			    } else {
//			        list = orderDetailSvc.getAll();
//			    }
//
//			    return "admin/orders/list";
//			}
			
//			@GetMapping("list")
//			public String listAllOrderDetail(HttpServletRequest req, Model model) {
//				Map<String, String[]> map = req.getParameterMap();
//				List<OrderDetailVO> list = orderDetailSvc.getAll();
//				model.addAttribute("orderDetailList", list); // for listAllEmp.html 第85行用
//				return "admin/orderDetail/list";
//			}
			
			@GetMapping("list")
			public String listOrderDetailByOrderId(@RequestParam(value = "orderId", required = false) Integer orderId, Model model) {
			    List<OrderDetailVO> list;
			    if (orderId != null) {
			        list = orderDetailSvc.findByOrderId(orderId);
			    } else {
			        list = orderDetailSvc.getAll();
			    }
			    model.addAttribute("orderDetailList", list);
				model.addAttribute("pageTitle", "訂單明細");
			    
			    return "admin/orderdetail/list";
			}
			
			@GetMapping("/detail")
			public String showOrderDetail(@RequestParam("orderId") Integer orderId, Model model) {
			    OrdersVO orders = ordersSvc.getOneOrder(orderId);

			    if (orders == null) {
			        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此訂單");
			    }

			    model.addAttribute("orders", orders);
			    model.addAttribute("orderDetailList", orderDetailSvc.findByOrderId(orderId));
				model.addAttribute("pageTitle", "訂單明細");
				
			    return "admin/orderdetail/list";  
			}
			
}
