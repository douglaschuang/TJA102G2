package com.babymate.cart.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

public class CartRedisVO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Integer productId;
    private Integer quantity;
    private Date updateTime;

    public CartRedisVO() {}

    public CartRedisVO(Integer productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
        this.updateTime = new Date();
    }

    // getter/setter
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}