package com.babymate.orders.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.babymate.babyhandbook.model.BabyhandbookVO;
import com.babymate.babyrecord.model.BabyrecordVO;
import com.babymate.orderDetail.model.OrderDetailService;
import com.babymate.orders.model.OrdersRepository;
import com.babymate.orders.model.OrdersService;
import com.babymate.orders.model.OrdersVO;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/orders")
public class OrdersController {

	@Autowired
	OrdersService ordersSvc;

	@Autowired
	OrderDetailService orderDetailSvc;

	@Autowired
	private OrdersRepository ordersRepository;

	public OrdersController(OrdersService ordersSvc, OrderDetailService orderDetailSvc) {
		super();
		this.ordersSvc = ordersSvc;
		this.orderDetailSvc = orderDetailSvc;
	}

	@PostMapping("getOne_For_Update")
	public String getOne_For_Update(@RequestParam("orderId") String orderId, ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始查詢資料 *****************************************/
		OrdersVO ordersVO = ordersSvc.getOneOrder(Integer.valueOf(orderId));

		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("ordersVO", ordersVO);
		model.addAttribute("pageTitle", "訂單明細｜修改");

		return "admin/orders/update_orders_input";
	}

	@PostMapping("update")
	public String update(@Valid @ModelAttribute("ordersVO") OrdersVO ordersVO, BindingResult result, ModelMap model)
			throws IOException {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/

		if (result.hasErrors()) {
			// 若有錯誤，回到編輯頁
			model.addAttribute("pageTitle", "訂單明細｜修改");

			return "admin/orders/update_orders_input";
		}

		ordersSvc.updateOrders(ordersVO);

		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/

		model.addAttribute("success", "修改成功");
		model.addAttribute("pageTitle", "訂單明細｜修改");

		return "redirect:/admin/orders/list";

	}

	/*
	 * Method used to populate the Map Data in view. 如 : <form:select path="deptno"
	 * id="deptno" items="${depMapData}" />
	 */
	@ModelAttribute("status") //
	protected Map<Integer, String> referenceMapData() {
		Map<Integer, String> map = new LinkedHashMap<Integer, String>();
		map.put(0, "已取消");
		map.put(1, "未付款");
		map.put(2, "已付款");
		map.put(3, "已出貨");
		map.put(4, "已收貨");
		map.put(5, "退款中");
		map.put(6, "已退款");
		return map;
	}

	// 去除BindingResult中某個欄位的FieldError紀錄
	public BindingResult removeFieldError(OrdersVO ordersVO, BindingResult result, String removedFieldname) {
		List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
				.filter(fieldname -> !fieldname.getField().equals(removedFieldname)).collect(Collectors.toList());
		result = new BeanPropertyBindingResult(ordersVO, "ordersVO");
		for (FieldError fieldError : errorsListToKeep) {
			result.addError(fieldError);
		}
		return result;
	}

	@GetMapping("list")
	public String listAllOrders(@RequestParam(value = "orderId", required = false) Integer orderId, Model model) {

		List<OrdersVO> list;

		if (orderId != null) {
			OrdersVO orders = ordersSvc.getOneOrder(orderId);
			model.addAttribute("orders", orders);

			list = ordersSvc.findByorderId(orderId);
		} else {
			list = ordersSvc.getAll();
		}

		// 確保 list 不為 null
		list = (list != null) ? list : Collections.emptyList();

		// 新增 listCountMap
		Map<Integer, Integer> listCountMap = new HashMap<>();
		for (OrdersVO order : list) {
			listCountMap.put(order.getOrderId(), order.getOrderDetails() != null ? order.getOrderDetails().size() : 0);
		}

		model.addAttribute("ordersList", list);
		model.addAttribute("orderId", orderId);
		model.addAttribute("listCountMap", listCountMap);
		model.addAttribute("pageTitle", "訂單管理｜列表");

		return "admin/orders/list";
	}

	@GetMapping("/detail")
	public String showOrderDetail(@RequestParam("orderId") Integer orderId, Model model) {
		OrdersVO orders = ordersSvc.getOneOrder(orderId);

		if (orders == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此訂單");
		}

		model.addAttribute("orders", orders);
		model.addAttribute("orderDetailList", orderDetailSvc.findByOrderId(orderId));

		return "admin/orderdetail/list"; // 對應到你目前的 Thymeleaf 頁面
	}

	@GetMapping("/search")
	public String searchOrders(@RequestParam(required = false) String orderNo,
			@RequestParam(required = false) BigDecimal minAmount, 
			@RequestParam(required = false) BigDecimal maxAmount,
			@RequestParam(required = false) Integer memberId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
			Model model,
            RedirectAttributes redirectAttributes) {

	    // 若無任何條件，給空結果避免全表查詢或錯誤
	    if (orderNo == null && minAmount == null && maxAmount == null && memberId == null && startTime == null && endTime == null) {
	        model.addAttribute("ordersList", Collections.emptyList());
	        model.addAttribute("listCountMap", new HashMap<>());
	        model.addAttribute("pageTitle", "訂單管理");
	        return "admin/orders/list";
	    }
	    
	    // 建立查詢條件
		Specification<OrdersVO> spec = Specification.where(null);
		spec = spec.and(OrderSpecs.orderNoLike(orderNo)).and(OrderSpecs.amountBetween(minAmount, maxAmount))
				.and(OrderSpecs.memberIdEqual(memberId)).and(OrderSpecs.orderTimeBetween(startTime, endTime));

	    // 執行查詢
		List<OrdersVO> ordersList = ordersRepository.findAll(spec);

		// 如果查無資料，設定提示變數
		if (ordersList.isEmpty()) {
	        redirectAttributes.addFlashAttribute("noResult", true);
	        return "redirect:/admin/orders/list";  // 導回查詢起始頁
	    }
		
		// 傳送查詢結果
		model.addAttribute("ordersList", ordersList);
		model.addAttribute("pageTitle", "訂單管理｜查詢結果");
		
		return "admin/orders/list"; // 顯示查詢結果頁

	}

}
