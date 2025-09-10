package com.babymate.orderDetail.model;

import java.sql.Date;

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

@Entity
@Table(name = "ORDER_DETAIL")
public class OrderDetailVO implements java.io.Serializable{
	private Integer orderDetailId;
	private OrdersVO ordersVO;
	private ProductVO productVO;
	private Integer quantity;
	private Double price;
	private Date updateTime;
	
	@Id
	@Column(name = "ORDER_DETAIL_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer getOrderDetailId() {
		return orderDetailId;
	}
	
	public void setOrderDetailId(Integer orderDetailId) {
		this.orderDetailId = orderDetailId;
	}
	
	@ManyToOne
	@JoinColumn(name = "ORDER_ID")
	public OrdersVO getOrdersVO() {
		return ordersVO;
	}
	
	public void setOrdersVO(OrdersVO ordersVO) {
		this.ordersVO = ordersVO;
	}
	
	@ManyToOne
	@JoinColumn(name = "PRODUCT_ID")
	public ProductVO getProductVO() {
		return productVO;
	}
	
	public void setProductVO(ProductVO productVO) {
		this.productVO = productVO;
	}
	
	@Column(name = "QUANTITY")
	public Integer getQuantity() {
		return quantity;
	}
	
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	@Column(name = "PRICE")
	public Double getPrice() {
		return price;
	}
	
	public void setPrice(Double price) {
		this.price = price;
	}
	
	@Column(name = "UPDATE_TIME")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	public Date getUpdateTime() {
		return updateTime;
	}
	
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	
}
