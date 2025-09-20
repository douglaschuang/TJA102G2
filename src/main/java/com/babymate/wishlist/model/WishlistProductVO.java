package com.babymate.wishlist.model;

import java.sql.Timestamp;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.*;

@Entity
@Table(
  name = "wishlist_product",
  uniqueConstraints = @UniqueConstraint(name = "uk_member_product", columnNames = {"member_id","product_id"})
)
public class WishlistProductVO implements java.io.Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="wishlist_id")
  private Integer wishlistId;

  @Column(name="member_id", nullable=false)
  private Integer memberId;

  @Column(name="product_id", nullable=false)
  private Integer productId;

  @Column(name="created_at", insertable=false, updatable=false)
  @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
  private Timestamp createdAt;

  public Integer getWishlistId() { return wishlistId; }
  public void setWishlistId(Integer wishlistId) { this.wishlistId = wishlistId; }

  public Integer getMemberId() { return memberId; }
  public void setMemberId(Integer memberId) { this.memberId = memberId; }

  public Integer getProductId() { return productId; }
  public void setProductId(Integer productId) { this.productId = productId; }

  public Timestamp getCreatedAt() { return createdAt; }
  public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
