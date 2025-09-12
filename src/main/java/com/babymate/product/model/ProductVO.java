package com.babymate.product.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

import com.babymate.category.model.CategoryVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "PRODUCT")
public class ProductVO implements java.io.Serializable{
	private Integer productId;
	private String productNo;
	private String productName;
	private CategoryVO categoryVO;
	private BigDecimal price;
	private Integer status;
	private Timestamp statusUpdateTime;
	private byte[]  productIcon;
	private String featureDesc;
	private String specDesc;
	private String note;
	private String remark;
	private Timestamp updateTime;
//	private Set<FavoriteProductVO> favoriteProducts = new HashSet<FavoriteProductVO>();
//	private Set<OrderDetailVO> orderDetails = new HashSet<OrderDetailVO>();
	
	public ProductVO() {
	}
	
	@Id
	@Column(name = "PRODUCT_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer getProductId() {
		return productId;
	}
	
	public void setProductId(Integer productId) {
		this.productId = productId;
	}
	
	@Column(name = "PRODUCT_NO")
	@NotEmpty(message="商品編號請勿空白")
	public String getProductNo() {
		return productNo;
	}
	
	public void setProductNo(String productNo) {
		this.productNo = productNo;
	}
	
	@Column(name = "PRODUCT_NAME")
	@NotEmpty(message="商品名稱請勿空白")
	public String getProductName() {
		return productName;
	}
	
	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	@ManyToOne
	@JoinColumn(name = "CATEGORY_ID")
	public CategoryVO getCategoryVO() {
		return categoryVO;
	}
	
	public void setCategoryVO(CategoryVO categoryVO) {
		this.categoryVO = categoryVO;
	}
	
	@Column(name = "PRICE")
	@NotNull(message="金額不能空白")
	@Digits(integer = 8, fraction = 2, message = "價格格式錯誤：最多 8 位整數、2 位小數")
	@DecimalMin(value = "0.00", inclusive = true, message = "價格需 ≥ 0")
	@NumberFormat(pattern = "#,##0.##") // （可選）只影響顯示/格式化，不是驗證
	public BigDecimal getPrice() {
		return price;
	}
	
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	
	@Column(name = "STATUS")
	@NotNull(message="商品狀態不能空白，只能0：下架、1：上架")
	public Integer getStatus() {
		return status;
	}
	
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	@Transient
	public String getStatusStr() {
		if(status == null) 
			return "";
		return status == 1 ? "上架" : "下架";
	}
	
	@CreationTimestamp
	@Column(name = "STATUS_UPDATE_TIME", updatable = false)
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	public Timestamp getStatusUpdateTime() {
		return statusUpdateTime;
	}
	
	public void setStatusUpdateTime(Timestamp statusUpdateTime) {
		this.statusUpdateTime = statusUpdateTime;
	}
	
	@Transient // Hibernate/JPA: 這個屬性不會映射到資料庫欄位，只在程式中使用
	public String getStatusUpdateTimeStr() { // 將 Java 物件的 Timestamp 欄位 (statusUpdateTime) 格式化成字串，方便前端顯示。
	    if (statusUpdateTime != null) {
	        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(statusUpdateTime);
	    }
	    return null;
	}
	
	@Column(name = "PRODUCT_ICON")
	public byte[] getProductIcon() {
		return productIcon;
	}
	
	public void setProductIcon(byte[] productIcon) {
		this.productIcon = productIcon;
	}
	
	@Column(name = "FEATURE_DESC")
	public String getFeatureDesc() {
		return featureDesc;
	}
	
	public void setFeatureDesc(String featureDesc) {
		this.featureDesc = featureDesc;
	}
	
	@Column(name = "SPEC_DESC")
	public String getSpecDesc() {
		return specDesc;
	}
	
	public void setSpecDesc(String specDesc) {
		this.specDesc = specDesc;
	}
	
	@Column(name = "NOTE")
	public String getNote() {
		return note;
	}
	
	public void setNote(String note) {
		this.note = note;
	}
	
	@Column(name = "REMARK")
	public String getRemark() {
		return remark;
	}
	
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	@Column(name = "UPDATE_TIME",
			insertable = false,  // 關鍵：插入時不要帶
	        updatable = false,   // 如果你希望完全交給 DB 控制，更新也不要帶
	        nullable = false)
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
//	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, mappedBy="productVO")
//	@OrderBy("favoriteProductId asc")
//	public Set<FavoriteProductVO> getFavoriteProducts() {
//		return favoriteProducts;
//	}
//
//	public void setFavoriteProducts(Set<FavoriteProductVO> favoriteProducts) {
//		this.favoriteProducts = favoriteProducts;
//	}
//
//	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, mappedBy="productVO")
//	@OrderBy("orderDetailId asc")
//	public Set<OrderDetailVO> getOrderDetails() {
//		return orderDetails;
//	}
//
//	public void setOrderDetails(Set<OrderDetailVO> orderDetails) {
//		this.orderDetails = orderDetails;
//	}

	
}