package com.babymate.wishlist.model;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistProductRepository extends JpaRepository<WishlistProductVO, Integer> {
  Optional<WishlistProductVO> findByMemberIdAndProductId(Integer memberId, Integer productId);
  List<WishlistProductVO> findByMemberId(Integer memberId);
  void deleteByMemberIdAndProductId(Integer memberId, Integer productId);
  boolean existsByMemberIdAndProductId(Integer memberId, Integer productId);
}
