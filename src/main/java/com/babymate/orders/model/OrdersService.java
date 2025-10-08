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
	
	//	抓取MamberId，要用在確認該會員才可檢視自己訂單資料 
	public List<OrdersVO> getOrdersByMemberIdDesc(Integer memberId){
		return repository.findByMemberVO_MemberIdOrderByOrderTimeDesc(memberId);
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

	/**
     * 建立「未付款」訂單（進入結帳頁時或按下去綠界前建立）
     * 注意：
     * - payTime 不要填；等待綠界回傳成功再寫入
     * - status = 1（依你 my-orders.html 的對應：1=未付款、2=已付款、0=已取消）
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

        // 用 orderTime 轉成字串當作 orderNo
        String orderNo = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        order.setOrderNo(orderNo);
        
        order.setStatus(1); // 1=未付款（對應 my-orders.html）
        order.setAmount(total);
        order.setRecipient(recipient == null ? "" : recipient);
        order.setAddress(address == null ? "" : address);
        order.setPhone(phone == null ? "" : phone);
        order.setEmail(email == null ? "" : email);
        order.setRemark(remark == null ? "" : remark);
        order.setUpdateTime(now);

        OrdersVO saved = repository.save(order);

        // 明細
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
     * 只更新訂單的配送/聯絡資訊：
     * - recipient / address / phone 必填（後端再保險檢查）
     * - email 可為空（若空就存空字串或沿用原值，依需求）
     * - 不修改 status、payTime、amount
     */
    @Transactional
    public void updateOrderContact(String orderNo,
                                   String recipient,
                                   String address,
                                   String phone,
                                   String email) {
        OrdersVO order = repository.findByOrderNo(orderNo)
            .orElseThrow(() -> new IllegalArgumentException("找不到訂單 orderNo=" + orderNo));

        // 基本保險（前端/DTO已驗過，這裡再簡單守一次）
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
    
    // 查詢單筆訂單（依訂單編號）
    public OrdersVO findByOrderNo(String orderNo) {
        return repository.findByOrderNo(orderNo)
        		.orElseThrow(() -> new RuntimeException("找不到訂單 orderNo=" + orderNo));
    }
}
