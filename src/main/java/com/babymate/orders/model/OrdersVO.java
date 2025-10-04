package com.babymate.orders.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import com.babymate.member.model.MemberVO;
import com.babymate.orderDetail.model.OrderDetailVO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "orders")
public class OrdersVO implements java.io.Serializable{
	
	@Id
	@Column(name = "ORDER_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer orderId;
	
	@ManyToOne
	@JoinColumn(name = "MEMBER_ID")
	@NotNull
	private MemberVO memberVO;
	
	@Column(name = "ORDER_NO")
	@NotNull
	private String orderNo;
	
	@Column(name = "ORDER_TIME")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@NotNull
	private LocalDateTime orderTime;
	
	@Column(name = "STATUS")
	@NotNull
	private Integer status;
	
	@Column(name = "PAY_TIME")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")

	private LocalDateTime payTime;

	@Column(name = "AMOUNT", precision = 18, scale = 2)
	@NotNull
	private BigDecimal amount;
	
	@Column(name = "RECIPIENT")
	@NotNull
	private String recipient;
	
	@Column(name = "ADDRESS")
	@NotNull
	private String address;
	
	@Column(name = "PHONE")
	@NotNull
	private String phone;
	
	@Column(name = "EMAIL")
	@NotNull
	private String email;
	
	@Column(name = "REMARK")
	private String remark;
	
	@Column(name = "UPDATE_TIME")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@NotNull
	private LocalDateTime updateTime;
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, mappedBy="ordersVO")
	@OrderBy("order_detail_id asc")
	private Set<OrderDetailVO> orderDetails = new HashSet<OrderDetailVO>();
	
	
	public Integer getOrderId() {
		return orderId;
	}
	
	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}
	
	public MemberVO getMemberVO() {
		return memberVO;
	}
	
	public void setMemberVO(MemberVO memberVO) {
		this.memberVO = memberVO;
	}
	
	public String getOrderNo() {
		return orderNo;
	}
	
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	
	public LocalDateTime getOrderTime() {
		return orderTime;
	}
	
	public void setOrderTime(LocalDateTime orderTime) {
		this.orderTime = orderTime;
	}

	public Integer getStatus() {
		return status;
	}
	
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public LocalDateTime getPayTime() {
		return payTime;
	}
	
	public void setPayTime(LocalDateTime payTime) {
		this.payTime = payTime;
	}
	
	public BigDecimal getAmount() {
		return amount;
	}
	
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	public String getRecipient() {
		return recipient;
	}
	
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getPhone() {
		return phone;
	}
	
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getRemark() {
		return remark;
	}
	
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public LocalDateTime getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(LocalDateTime updateTime) {
		this.updateTime = updateTime;
	}

	public Set<OrderDetailVO> getOrderDetails() {
		return orderDetails;
	}
	
	public void setOrderDetails(Set<OrderDetailVO> orderDetails) {
		this.orderDetails = orderDetails;
	}
	
	
}
