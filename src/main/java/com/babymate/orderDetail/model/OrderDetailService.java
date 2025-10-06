package com.babymate.orderDetail.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.babymate.orders.model.OrdersRepository;
import com.babymate.orders.model.OrdersVO;
import com.babymate.product.model.ProductRepository;
import com.babymate.product.model.ProductVO;

@Service("OrderDetailService")
public class OrderDetailService {
	
	@Autowired
	private OrderDetailRepository orderDetailRepo;
	
	@Autowired
	private OrdersRepository ordersRepo;
	
	@Autowired
	private ProductRepository productRepo;
	
//	public void updateOrderDetail1(OrderDetailVO orderDetailVO) {
//		repository.save(orderDetailVO);
//	}
	
//	public OrderDetailVO getOneOrderDetail(Integer orderDetailId) {
//		if (orderDetailId == null) {
//			throw new IllegalArgumentException("orderDetailId 不能為 null");
//		}
//		return repository.findById(orderDetailId)
//				.orElseThrow(() -> new EntityNotFoundException("找不到 orderDetail，id: " + orderDetailId));
//	}

	public List<OrderDetailVO> getAll(){
		return orderDetailRepo.findAll();
	}
	
//	 public Map<Integer, Integer> getCountMap() {
//	        List<Object[]> resultList = repository.countRecordsGroupByOrderDetailId();
//
//	        Map<Integer, Integer> countMap = new HashMap<>();
//	        for (Object[] row : resultList) {
//	            Integer orderDetailId = (Integer) row[0];
//	            Long count = (Long) row[1];
//	            countMap.put(orderDetailId, count.intValue());
//	        }
//	        return countMap;
//	    }
//	 
	 public List<OrderDetailVO> findByOrderId(Integer orderId) {
		    return orderDetailRepo.findByOrdersVO_OrderId(orderId);
		}
	 
	 /**
	  * 新增一筆訂單明細
	  */
	 public void addDetail(Integer orderId, Integer productId, Integer qty, BigDecimal Price) {
		 OrdersVO order = ordersRepo.findById(orderId)
				 					.orElseThrow(() -> new IllegalArgumentException("找不到訂單: " + orderId));
	     ProductVO product = productRepo.findById(productId)
	    		 						.orElseThrow(() -> new IllegalArgumentException("找不到商品: " + productId));

	     OrderDetailVO d = new OrderDetailVO();
	     d.setOrdersVO(order);
	     d.setProductVO(product);
	     d.setQuantity(qty);
	     d.setPrice(Price);                     // 若你的欄位名稱不同，對應調整
	     d.setUpdateTime(LocalDateTime.now());

	     orderDetailRepo.save(d);
	    }
}
