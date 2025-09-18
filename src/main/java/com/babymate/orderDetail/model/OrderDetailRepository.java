package com.babymate.orderDetail.model;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.babymate.orders.model.OrdersVO;

public interface OrderDetailRepository extends JpaRepository<OrderDetailVO, Integer>{

	  List<OrderDetailVO> findByOrdersVO_OrderId(Integer orderId);

}
