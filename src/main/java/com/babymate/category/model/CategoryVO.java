package com.babymate.category.model;

import java.sql.Date;
import java.sql.Timestamp;
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
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotEmpty;

@Entity
@Table(name = "CATEGORY")
public class CategoryVO implements java.io.Serializable{
	private static final long SerialVersionUID = 1L;
	
	private Integer categoryId;
	private String categoryName;
	private Timestamp updateTime;
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
	@NotEmpty(message="類別名稱請勿空白")
	public String getCategoryName() {
		return categoryName;
	}
	
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
	@Column(name = "UPDATE_TIME",
			insertable = false,  // 關鍵：插入時不要帶
			updatable = false,   // 如果你希望完全交給 DB 控制，更新也不要帶
			nullable = false)    // 確保 DB 欄位有 DEFAULT，否則可移除 nullable=false
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	public Timestamp getUpdateTime() {
		return updateTime;
	}
	
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
	
	@Transient // Hibernate/JPA: 這個屬性不會映射到資料庫欄位，只在程式中使用
	public String getUpdateTimeStr() { // 將 Java 物件的 Timestamp 欄位 (UpdateTime) 格式化成字串，方便前端顯示。
	    if (updateTime != null) {
	        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(updateTime);
	    }
	    return null;
	}

	@OneToMany(cascade=CascadeType.ALL,
			   fetch=FetchType.EAGER,
			   mappedBy="categoryVO")
	@OrderBy("productId asc")
	public Set<ProductVO> getProducts() {
		return products;
	}

	public void setProducts(Set<ProductVO> products) {
		this.products = products;
	}
	
}
