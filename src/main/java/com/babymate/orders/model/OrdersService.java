package com.babymate.orders.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.babymate.babyrecord.model.BabyrecordVO;

import jakarta.persistence.EntityNotFoundException;

@Service("OrdersService")
public class OrdersService {

	@Autowired
	OrdersRepository repository;

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
	public List<OrdersVO> getOrdersByMemberId(Integer memberId){
		return repository.findByMemberVO_MemberId(memberId);
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

	 
}
