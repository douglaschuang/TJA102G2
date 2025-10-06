package com.babymate.orders.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrdersRepository extends JpaRepository<OrdersVO, Integer>,
JpaSpecificationExecutor<OrdersVO> {

  List<OrdersVO> findByOrderId(Integer orderId);

  // 今日訂單數
  long countByOrderTimeBetween(LocalDateTime start, LocalDateTime end);

  // 最新訂單 TOP5
  List<OrdersVO> findTop5ByOrderByOrderTimeDesc();
  /**
   * 月營收（只算已付款 status=1）
   */
  @Query(value = """
    SELECT DATE_FORMAT(ORDER_TIME, '%Y-%m-01') AS month_key,
           SUM(AMOUNT) AS total
    FROM orders
    WHERE STATUS = 1
      AND ORDER_TIME >= :from
      AND ORDER_TIME <  :to
    GROUP BY month_key
    ORDER BY month_key
  """, nativeQuery = true)
  List<Object[]> sumPaidAmountByMonth(@Param("from") LocalDateTime from,
                                      @Param("to")   LocalDateTime to);

  /**
   * 近 N 天熱銷 TOP5（數量合計）
   * 依你的實體：明細表 @Table(name="ORDER_DETAIL")、商品表 @Table(name="PRODUCT")
   * 欄位：ORDER_DETAIL(ORDER_ID, PRODUCT_ID, QUANTITY)、PRODUCT(PRODUCT_ID, PRODUCT_NAME)
   * 用 LEFT JOIN PRODUCT，避免商品資料缺失讓統計漏掉
   */
  @Query(value = """
    SELECT od.PRODUCT_ID,
           COALESCE(p.PRODUCT_NAME, CONCAT('商品#', od.PRODUCT_ID)) AS product_name,
           SUM(od.QUANTITY) AS qty
    FROM ORDER_DETAIL od
    JOIN orders o
      ON o.ORDER_ID = od.ORDER_ID
     AND o.STATUS   = 1
    LEFT JOIN PRODUCT p
      ON p.PRODUCT_ID = od.PRODUCT_ID
    WHERE o.ORDER_TIME >= :since
    GROUP BY od.PRODUCT_ID, product_name
    ORDER BY qty DESC
    LIMIT 5
  """, nativeQuery = true)
  List<Object[]> topSellingProductsSince(@Param("since") LocalDateTime since);
  // 依各自會員訂單查詢(按訂單編號排序)
  List<OrdersVO> findByMemberVO_MemberIdOrderByOrderTimeDesc(Integer memberId);

  Optional<OrdersVO> findByOrderNo(String orderNo);
}
