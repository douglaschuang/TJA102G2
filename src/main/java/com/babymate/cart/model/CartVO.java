package com.babymate.cart.model;

import java.time.LocalDateTime;

import com.babymate.member.model.MemberVO;
import com.babymate.product.model.ProductVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cart")
public class CartVO {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cart_id")
	private Integer cartId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private MemberVO member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private ProductVO product;

	@Column(name = "quantity", nullable = false)
	private Integer quantity;

	@Column(name = "update_time")
	private LocalDateTime updateTime;

	// Getters and Setters
	public Integer getCartId() {
		return cartId;
	}

	public void setCartId(Integer cartId) {
		this.cartId = cartId;
	}

	public MemberVO getMember() {
		return member;
	}

	public void setMember(MemberVO member) {
		this.member = member;
	}

	public ProductVO getProduct() {
		return product;
	}

	public void setProduct(ProductVO product) {
		this.product = product;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public LocalDateTime getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(LocalDateTime updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public String toString() {
		return "CartVO [cartId=" + cartId + ", member=" + member.getMemberId() + ", product=" + product.getProductId() + ", quantity=" + quantity
				+ ", updateTime=" + updateTime + "]";
	}
	
	
}
