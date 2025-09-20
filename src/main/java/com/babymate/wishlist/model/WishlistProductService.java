package com.babymate.wishlist.model;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishlistProductService {

  private final WishlistProductRepository repo;
  public WishlistProductService(WishlistProductRepository repo) { this.repo = repo; }

  /** 切換收藏狀態；回傳 true=加入、false=取消 */
  @Transactional
  public boolean toggle(Integer memberId, Integer productId){
    var exist = repo.findByMemberIdAndProductId(memberId, productId);
    if (exist.isPresent()) {
      repo.deleteByMemberIdAndProductId(memberId, productId);
      return false;
    } else {
      var w = new WishlistProductVO();
      w.setMemberId(memberId);
      w.setProductId(productId);
      repo.save(w);
      return true;
    }
  }

  public boolean isFavorite(Integer memberId, Integer productId){
    return repo.existsByMemberIdAndProductId(memberId, productId);
  }

  public List<WishlistProductVO> listByMember(Integer memberId){
    return repo.findByMemberId(memberId);
  }
}
