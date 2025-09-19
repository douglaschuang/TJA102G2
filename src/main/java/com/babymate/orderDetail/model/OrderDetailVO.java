package com.babymate.orderDetail.model;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.babymate.orders.model.OrdersVO;
import com.babymate.product.model.ProductVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "ORDER_DETAIL")
public class OrderDetailVO implements java.io.Serializable{
	
	@Id
	@Column(name = "ORDER_DETAIL_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer orderDetailId;
	
	@ManyToOne
	@JoinColumn(name = "ORDER_ID")
	@NotNull
	private OrdersVO ordersVO;
	
	@ManyToOne
	@JoinColumn(name = "PRODUCT_ID")
	@NotNull
	private ProductVO productVO;
	
	@Column(name = "QUANTITY")
	@NotNull
	private Integer quantity;
	
	@Column(name = "PRICE")
	@NotNull
	private Double price;
	
	@Column(name = "UPDATE_TIME")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@NotNull
	private LocalDateTime updateTime;
	

	public Integer getOrderDetailId() {
		return orderDetailId;
	}
	
	public void setOrderDetailId(Integer orderDetailId) {
		this.orderDetailId = orderDetailId;
	}
	
	public OrdersVO getOrdersVO() {
		return ordersVO;
	}
	
	public void setOrdersVO(OrdersVO ordersVO) {
		this.ordersVO = ordersVO;
	}
	
	public ProductVO getProductVO() {
		return productVO;
	}
	
	public void setProductVO(ProductVO productVO) {
		this.productVO = productVO;
	}
	
	public Integer getQuantity() {
		return quantity;
	}
	
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	public Double getPrice() {
		return price;
	}
	
	public void setPrice(Double price) {
		this.price = price;
	}

	public LocalDateTime getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(LocalDateTime updateTime) {
		this.updateTime = updateTime;
	}

	
}
