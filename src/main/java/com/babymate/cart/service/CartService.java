package com.babymate.cart.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.babymate.cart.model.CartRedisVO;
import com.babymate.cart.model.CartRepository;
import com.babymate.cart.model.CartVO;
import com.babymate.staff.model.StaffVO;
import com.babymate.staff.model.StaffRepository;

//import hibernate.util.CompositeQuery.HibernateUtil_CompositeQuery_Emp3;


@Service("cartService")
public class CartService {

	@Autowired
	CartRepository repository;
	
	@Autowired
    private SessionFactory sessionFactory;

	public void addCart(CartVO cartVO) {
		repository.save(cartVO);
	}

	public void updateCart(CartVO cartVO) {
		repository.save(cartVO);
	}

	public void deleteCart(Integer cartId) {
		if (repository.existsById(cartId))
			repository.deleteByCartId(cartId);
//		    repository.deleteById(staffId);
	}

	public CartVO getOneStaff(Integer cartId) {
		Optional<CartVO> optional = repository.findById(cartId);
//		return optional.get();
		return optional.orElse(null);  // public T orElse(T other) : 如果值存在就回傳其值，否則回傳other的值
	}

	public List<CartVO> getAll() {
		return repository.findAll();
	}

//	public List<CartVO> getAll(Map<String, String[]> map) {
//		return HibernateUtil_CompositeQuery_Emp3.getAllC(map,sessionFactory.openSession());
//	}
	
    private final RedisTemplate<String, Object> redisTemplate;
//    private static final String CART_KEY_MEMBER_PREFIX = "cart:member:";
//    private static final String CART_KEY_SESSION_PREFIX = "cart:session:";

    public CartService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 取得購物車
    public List<CartRedisVO> getCart(String cartKey) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(cartKey);
        List<CartRedisVO> cartItems = new ArrayList<>();
        entries.forEach((k, v) -> cartItems.add((CartRedisVO) v));
        return cartItems;
    }

    // 新增或更新購物車商品
//    public CartRedisVO addOrUpdateItem(Integer memberId, Integer productId, Integer quantity) {
//        String key = getCartKey(memberId);
//        CartRedisVO item = (CartRedisVO) redisTemplate.opsForHash().get(key, productId.toString());
//
//        if (item == null) {
//            item = new CartRedisVO(productId, quantity);
//        } else {
//            item.setQuantity(item.getQuantity() + quantity);
//            item.setUpdateTime(new Date());
//        }
//
//        redisTemplate.opsForHash().put(key, productId.toString(), item);
//        return item;
//    }
    
    public CartRedisVO addOrUpdateItem(String cartKey, Integer productId, Integer quantity) {
        CartRedisVO item = (CartRedisVO) redisTemplate.opsForHash()
                .get(cartKey, productId.toString());

        if (item == null) {
            item = new CartRedisVO(productId, quantity);
        } else {
            item.setQuantity(item.getQuantity() + quantity);
            item.setUpdateTime(new Date());
        }

        redisTemplate.opsForHash().put(cartKey, productId.toString(), item);
        return item;
    }

    // 修改商品數量
    public CartRedisVO updateQuantity(String cartKey, Integer productId, Integer quantity) {
//        String key = getCartKey(memberId);
        CartRedisVO item = (CartRedisVO) redisTemplate.opsForHash().get(cartKey, productId.toString());

        if (item != null) {
            item.setQuantity(quantity);
            item.setUpdateTime(new Date());
            redisTemplate.opsForHash().put(cartKey, productId.toString(), item);
        }
        return item;
    }

    // 移除商品
    public void removeItem(String cartKey, Integer productId) {
        redisTemplate.opsForHash().delete(cartKey, productId.toString());
    }

    // 清空購物車
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

}