package com.babymate.product.model;

import java.io.Serializable;
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

/**
 * 商品資料實體 (ProductVO)
 * 
 * 對應資料表：PRODUCT
 * 功能：
 *  - 後台管理頁面顯示商品資訊
 *  - 前台商品詳情與分類顯示
 *  - 自動驗證欄位是否正確（@NotNull、@DecimalMin...）
 */
@Entity
@Table(name = "PRODUCT")
public class ProductVO implements Serializable{
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
	
	public ProductVO() {
	}
	
	/** 主鍵設定 */
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
	
	/** 關聯到CategoryVO */
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
	
	/** 用於後台商品管理顯示狀態文字(上架 / 下架) */
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
	
	/** 將狀態更新時間格式化成字串(不存DB) */
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
	
	/** 將更新時間格式化成字串(不存DB) */
	@Transient // Hibernate/JPA: 這個屬性不會映射到資料庫欄位，只在程式中使用
	public String getUpdateTimeStr() { // 將 Java 物件的 Timestamp 欄位 (UpdateTime) 格式化成字串，方便前端顯示。
	    if (updateTime != null) {
	        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(updateTime);
	    }
	    return null;
	}
}