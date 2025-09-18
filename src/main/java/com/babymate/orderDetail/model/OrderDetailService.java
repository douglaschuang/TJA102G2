package com.babymate.orderDetail.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.babymate.orders.model.OrdersVO;

import jakarta.persistence.EntityNotFoundException;

@Service("OrderDetailService")
public class OrderDetailService {
	
	@Autowired
	OrderDetailRepository repository;
	
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
		return repository.findAll();
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
		    return repository.findByOrdersVO_OrderId(orderId);
		}
	 
}
