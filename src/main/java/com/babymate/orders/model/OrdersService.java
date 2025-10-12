package com.babymate.orders.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.babymate.cart.model.CartItemDisplayVO;
import com.babymate.member.model.MemberVO;
import com.babymate.orderDetail.model.OrderDetailService;

import jakarta.persistence.EntityNotFoundException;

@Service("OrdersService")
public class OrdersService {

	@Autowired
	private OrdersRepository repository;

	@Autowired
	private OrderDetailService orderDetailSvc;
	
	public void updateOrders(OrdersVO ordersVO) {
		repository.save(ordersVO);
	}

	public OrdersVO getOneOrder(Integer orderId) {
		if (orderId == null) {
			throw new IllegalArgumentException("orderid 不能為 null");
		}
		return repository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("找不到 orders，id: " + orderId));
	}

	public List<OrdersVO> getAll(){
		return repository.findAll();
	}

	public List<OrdersVO> findByorderId(Integer ordersId) {
		return null;
	}
	
//	 public Map<Integer, Integer> getCountMap() {
//	        List<Object[]> resultList = repository.countRecordsGroupByOrderId();
//
//	        Map<Integer, Integer> countMap = new HashMap<>();
//	        for (Object[] row : resultList) {
//	            Integer orderId = (Integer) row[0];
//	            Long count = (Long) row[1];
//	            countMap.put(orderId, count.intValue());
//	        }
//	        return countMap;
//	    }

	/** 依會員查訂單（新→舊）：給前台「我的訂單」頁面使用 */
	public List<OrdersVO> getOrdersByMemberIdDesc(Integer memberId){
		return repository.findByMemberVO_MemberIdOrderByOrderTimeDesc(memberId);
	}
	
	/**
	 * 【建立未付款訂單】（進入結帳頁或送綠界前建立）
	 * - 產生 orderNo（以時間 yyyyMMddHHmmssSSS）
	 * - 狀態 status=1（依 my-orders.html 的對應：1=未付款、2=已付款、0=已取消）
	 * - 金額 amount = 購物車小計
	 * - 立即寫入訂單明細（每個商品一筆 detail）
	 * - payTime 先不填，待綠界回傳成功再寫
	 */
	@Transactional
    public OrdersVO createPendingOrder(MemberVO member,
                                       BigDecimal total,
                                       List<CartItemDisplayVO> items,
                                       String recipient, String address,
                                       String phone, String email,
                                       String remark) {
        OrdersVO order = new OrdersVO();
        order.setMemberVO(member);
        
        LocalDateTime now = LocalDateTime.now();
        order.setOrderTime(now);

        /** 訂單編號（用 orderTime 轉成字串當作 orderNo，新增訂單時存入DB */
        String orderNo = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        order.setOrderNo(orderNo);
        
        order.setStatus(1);       // 1=未付款（對應 my-orders.html）
        order.setAmount(total);   // 小計金額
        order.setRecipient(recipient == null ? "" : recipient);
        order.setAddress(address == null ? "" : address);
        order.setPhone(phone == null ? "" : phone);
        order.setEmail(email == null ? "" : email);
        order.setRemark(remark == null ? "" : remark);
        order.setUpdateTime(now);

        OrdersVO saved = repository.save(order);

        /** 建立每一筆明細（商品、數量、單價） */
        items.forEach(i -> {
        	BigDecimal unitPrice = (i.getPrice() == null) ? BigDecimal.ZERO : i.getPrice();
            orderDetailSvc.addDetail(
            		saved.getOrderId(),
                    i.getProductId(), 
                    i.getQuantity(), 
                    unitPrice
            );
        });

        return saved;
    }

	/**
     * 【伺服器通知時標記已付款】ReturnURL (Server-to-Server)
     * - status=2（已付款）
     * - payTime=綠界通知時間（呼叫端傳入）
     * - amount=以綠界回傳金額為準
     */
    @Transactional
    public void markPaidByOrderNo(String orderNo, BigDecimal paidAmt, LocalDateTime payTime) {
        OrdersVO order = repository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("訂單不存在: " + orderNo));
        order.setStatus(2); // 2=已付款
        order.setPayTime(payTime);
        order.setAmount(paidAmt); // 以綠界回傳金額為準
        order.setUpdateTime(LocalDateTime.now());
        repository.save(order);
    }
    
    /** （選擇性）將訂單改為取消 */
    @Transactional
    public void markCancelIfExists(String orderNo, String reason) {
        repository.findByOrderNo(orderNo).ifPresent(o -> {
            o.setStatus(0); // 0 = 已取消（依你頁面對應）
            o.setRemark((o.getRemark() == null ? "" : o.getRemark()) +
                        (reason != null ? " | ECPay: " + reason : ""));
            o.setUpdateTime(LocalDateTime.now());
            repository.save(o);
        });
    }
    
    /**
     * 【前端回傳時標記已付款】OrderResultURL（保險再標一次）
     * - 若已是已付款就不覆寫 payTime（冪等）
     * - 以頁面金額為準或不覆寫視需求（你目前：若有 paidAmt 就覆寫）
     */
    @Transactional
    public void markPaidOnResult(String orderNo, BigDecimal paidAmt) {
        OrdersVO order = repository.findByOrderNo(orderNo)
            .orElseThrow(() -> new RuntimeException("訂單不存在: " + orderNo));

        // 已是已付款就不動 pay_time（避免被覆寫）
        if (order.getStatus() == 2) {
            // 可選：仍可更新 updateTime（例如別的欄位有變更）
            order.setUpdateTime(LocalDateTime.now());
            repository.save(order);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        order.setStatus(2);              // 2 = 已付款
        if (order.getPayTime() == null) {
            order.setPayTime(now);       // 第一次設定，之後不再改
        }
        // 若你希望以頁面總金額為準；或保留 DB 原金額就不要覆寫
        if (paidAmt != null) {
            order.setAmount(paidAmt);
        }
        order.setUpdateTime(now);        // 與 pay_time 同步當下時間
        repository.save(order);
    }

    /**
     * 【更新訂單聯絡/收件資訊】（送綠界前）
     * - ECPayController.checkout() 會先呼叫它，把前端表單收件人/電話/地址/Email 補齊
     * - 只動聯絡欄位與 updateTime，不動 status/payTime/amount
     */
    @Transactional
    public void updateOrderContact(String orderNo,
                                   String recipient,
                                   String address,
                                   String phone,
                                   String email) {
        OrdersVO order = repository.findByOrderNo(orderNo)
            .orElseThrow(() -> new IllegalArgumentException("找不到訂單 orderNo=" + orderNo));

        // 簡單校驗：確保三個欄位有值
        if (!StringUtils.hasText(recipient)) {
            throw new IllegalArgumentException("收件人不可空白");
        }
        if (!StringUtils.hasText(address)) {
            throw new IllegalArgumentException("地址不可空白");
        }
        if (!StringUtils.hasText(phone)) {
            throw new IllegalArgumentException("電話不可空白");
        }

        order.setRecipient(recipient.trim());
        order.setAddress(address.trim());
        order.setPhone(phone.trim());

        // email 可空，空的話你可以選擇存空字串或保留舊值
        if (email != null) {
            order.setEmail(email.trim());
        }

        // 只更新 updateTime，不碰 payTime/status/amount
        order.setUpdateTime(LocalDateTime.now());

        repository.save(order);
    }
    
    /** 依訂單編號查單筆：PDF 匯出與 Result/Notify 都會用到 */
    public OrdersVO findByOrderNo(String orderNo) {
        return repository.findByOrderNo(orderNo)
        		.orElseThrow(() -> new RuntimeException("找不到訂單 orderNo=" + orderNo));
    }
    
    /** 小工具：把 null 轉空字串 */
    private static String nv(String s){ 
    	return s == null ? "" : s; 
    }
}
