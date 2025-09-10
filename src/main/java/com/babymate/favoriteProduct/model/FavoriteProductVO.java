package com.babymate.favoriteProduct.model;

import java.sql.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.babymate.member.model.MemberVO;
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
@Table(name = "FAVORITE_PRODUCT")
public class FavoriteProductVO implements java.io.Serializable {
	private Integer favoriteProductId;
	private MemberVO memberVO;
	private ProductVO productVO;
	private Date updateTime;
	
	@Id
	@Column(name = "FAVORITE_PRODUCT_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer getFavoriteProductId() {
		return favoriteProductId;
	}
	
	public void setFavoriteProductId(Integer favoriteProductId) {
		this.favoriteProductId = favoriteProductId;
	}
	
	
	@ManyToOne
	@JoinColumn(name = "PRODUCT_ID")
	public ProductVO getProductVO() {
		return productVO;
	}

	public void setProductVO(ProductVO productVO) {
		this.productVO = productVO;
	}

	@ManyToOne
	@JoinColumn(name = "MEMBER_ID")
	public MemberVO getMemberVO() {
		return memberVO;
	}
	
	public void setMemberVO(MemberVO memberVO) {
		this.memberVO = memberVO;
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
