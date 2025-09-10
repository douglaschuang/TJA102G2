// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/

package com.babymate.cart.model;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CartRepository extends JpaRepository<CartVO, Integer> {

	@Transactional
	@Modifying
	@Query(value = "delete from cart where cart_id =?1", nativeQuery = true)
	void deleteByCartId(int cartId);

	//● (自訂)條件查詢
//	@Query(value = "from CartVO where cartId=?1 and memberId=?2 order by cartId")
//	List<CartVO> findByOthers(int cartId, int memberId);
}