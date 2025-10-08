package com.babymate.orders.controller;

import com.babymate.orders.model.OrdersVO;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderSpecs {

	public static Specification<OrdersVO> orderNoLike(String orderNo) {
		return (root, query, cb) -> orderNo == null || orderNo.isEmpty() ? null
				: cb.like(root.get("orderNo"), "%" + orderNo + "%");
	}

	public static Specification<OrdersVO> orderTimeBetween(LocalDateTime from, LocalDateTime to) {
		return (root, query, cb) -> {
			if (from != null && to != null) {
				return cb.between(root.get("orderTime"), from, to);
			} else if (from != null) {
				return cb.greaterThanOrEqualTo(root.get("orderTime"), from);
			} else if (to != null) {
				return cb.lessThanOrEqualTo(root.get("orderTime"), to);
			} else {
				return null;
			}
		};
	}

	public static Specification<OrdersVO> amountBetween(BigDecimal min, BigDecimal max) {
		return (root, query, cb) -> {
			if (min != null && max != null) {
				return cb.between(root.get("amount"), min, max);
			} else if (min != null) {
				return cb.greaterThanOrEqualTo(root.get("amount"), min);
			} else if (max != null) {
				return cb.lessThanOrEqualTo(root.get("amount"), max);
			} else {
				return null;
			}
		};
	}

	public static Specification<OrdersVO> memberIdEqual(Integer memberId) {
		return (root, query, cb) -> memberId == null ? null : cb.equal(root.get("memberVO").get("memberId"), memberId);
	}

	// 金額 >= minAmount
	public static Specification<OrdersVO> amountGreaterThanOrEqual(BigDecimal minAmount) {
		return (root, query, cb) -> minAmount == null ? null : cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
	}

	// 金額 <= maxAmount
	public static Specification<OrdersVO> amountLessThanOrEqual(BigDecimal maxAmount) {
		return (root, query, cb) -> maxAmount == null ? null : cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
	}

	// 訂單時間 >= startTime
	public static Specification<OrdersVO> orderTimeAfter(LocalDateTime startTime) {
		return (root, query, cb) -> startTime == null ? null
				: cb.greaterThanOrEqualTo(root.get("orderTime"), startTime);
	}

	// 訂單時間 <= endTime
	public static Specification<OrdersVO> orderTimeBefore(LocalDateTime endTime) {
		return (root, query, cb) -> endTime == null ? null : cb.lessThanOrEqualTo(root.get("orderTime"), endTime);
	}

}
