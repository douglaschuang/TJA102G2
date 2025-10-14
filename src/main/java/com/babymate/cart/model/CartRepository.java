package com.babymate.cart.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CartRepository extends JpaRepository<CartVO, Integer> {

	@Transactional
	@Modifying
	@Query(value = "delete from cart where cart_id =?1", nativeQuery = true)
	void deleteByCartId(int cartId);
}