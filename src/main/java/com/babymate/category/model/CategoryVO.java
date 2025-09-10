package com.babymate.category.model;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import com.babymate.product.model.ProductVO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "CATEGORY")
public class CategoryVO implements java.io.Serializable{
	private static final long SerialVersionUID = 1L;
	
	private Integer categoryId;
	private String categoryName;
	private Date updateTime;
	private Set<ProductVO> products = new HashSet<ProductVO>();
	
	public CategoryVO() {
	}
	
	@Id
	@Column(name = "CATEGORY_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer getCategoryId() {
		return categoryId;
	}
	
	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}
	
	@Column(name = "CATEGORY_NAME")
	public String getCategoryName() {
		return categoryName;
	}
	
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
	@Column(name = "UPDATE_TIME")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	public Date getUpdateTime() {
		return updateTime;
	}
	
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, mappedBy="categoryVO")
	@OrderBy("product_id asc")
	public Set<ProductVO> getProducts() {
		return this.products;
	}

	public void setProducts(Set<ProductVO> products) {
		this.products = products;
	}
	
}
