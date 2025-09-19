package com.babymate.orders.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.babymate.babyrecord.model.BabyrecordVO;

public interface OrdersRepository extends JpaRepository<OrdersVO, Integer> {

  List<OrdersVO> findByOrderId(Integer orderId);
	
  // 今日訂單數
  long countByOrderTimeBetween(LocalDateTime start, LocalDateTime end);
  
  // 最新訂單 TOP5
  List<OrdersVO> findTop5ByOrderByOrderTimeDesc();

  // 月營收（只算已付款 status=1）
  @Query(value = """
    SELECT DATE_FORMAT(order_time, '%Y-%m-01') AS month_key, SUM(amount) AS total
    FROM orders
    WHERE status = 1 AND order_time >= :from AND order_time < :to
    GROUP BY month_key ORDER BY month_key
  """, nativeQuery = true)
  List<Object[]> sumPaidAmountByMonth(@Param("from") LocalDateTime from,
                                      @Param("to")   LocalDateTime to);

  // 最近 N 天熱銷 TOP5（quantity 合計）
  @Query(value = """
    SELECT p.product_id, p.product_name, SUM(od.quantity) AS qty
    FROM order_detail od
    JOIN orders o   ON o.order_id = od.order_id AND o.status = 1
    JOIN product p  ON p.product_id = od.product_id
    WHERE o.order_time >= :since
    GROUP BY p.product_id, p.product_name
    ORDER BY qty DESC
    LIMIT 5
  """, nativeQuery = true)
  List<Object[]> topSellingProductsSince(@Param("since") LocalDateTime since);

}
