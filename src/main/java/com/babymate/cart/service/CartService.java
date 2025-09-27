package com.babymate.cart.service;

//import java.time.LocalDateTime;
import java.util.ArrayList;
//import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
//import java.util.stream.Collectors;

import org.hibernate.Session;
//import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.babymate.cart.model.CartRedisVO;
import com.babymate.cart.model.CartRepository;
import com.babymate.cart.model.CartVO;

//import hibernate.util.CompositeQuery.HibernateUtil_CompositeQuery_Emp3;


@Service("cartService")
public class CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CART_KEY_MEMBER_PREFIX = "cart:member:";
    private static final String CART_KEY_SESSION_PREFIX = "cart:session:";
	
	@Autowired
	CartRepository repository;
	
//	@Autowired
//    private SessionFactory sessionFactory;

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

	private String getSessionCartKey(String sessionId) {
        return CART_KEY_SESSION_PREFIX + sessionId;
    }

    private String getMemberCartKey(Integer memberId) {
        return CART_KEY_MEMBER_PREFIX + memberId;
    }
	
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

    /**
     * 登入時合併購物車
     */
    public void loginMergeCart(String sessionId, Integer memberId) {
        String sessionCartKey = getSessionCartKey(sessionId);
        String memberCartKey = getMemberCartKey(memberId);

        // 取出 session 購物車
     // 取未登入購物車
        Map<Object, Object> sessionEntries = redisTemplate.opsForHash().entries(sessionCartKey);
        // 取會員購物車
        Map<Object, Object> memberEntries  = redisTemplate.opsForHash().entries(memberCartKey);

        // 建立合併結果
        Map<Integer, CartRedisVO> mergedMap = new HashMap<>();

        // 會員原本的先放進去
        for (Object obj : memberEntries.values()) {
            CartRedisVO vo = (CartRedisVO) obj;
            mergedMap.put(vo.getProductId(), vo);
        }

        // 把未登入的合併進來（數量相加）
        for (Object obj : sessionEntries.values()) {
            CartRedisVO vo = (CartRedisVO) obj;
            mergedMap.merge(vo.getProductId(), vo, (oldVo, newVo) -> {
                oldVo.setQuantity(oldVo.getQuantity() + newVo.getQuantity());
                oldVo.setUpdateTime(new Date());
                return oldVo;
            });
        }

        // 清掉舊會員購物車
        redisTemplate.delete(memberCartKey);

        // 寫回 Redis Hash
        for (CartRedisVO vo : mergedMap.values()) {
            redisTemplate.opsForHash().put(memberCartKey, String.valueOf(vo.getProductId()), vo);
        }

        // 刪除未登入購物車
        redisTemplate.delete(sessionCartKey);
    }
    
}