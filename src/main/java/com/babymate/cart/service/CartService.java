package com.babymate.cart.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.babymate.cart.model.CartRedisVO;
import com.babymate.cart.model.CartRepository;
import com.babymate.cart.model.CartVO;
import com.babymate.member.model.MemberRepository;
import com.babymate.member.model.MemberVO;

/**
 * 購物車服務層（Service）
 * <p>
 * 負責處理購物車的新增、修改、刪除、查詢等業務邏輯， 並透過 {@link CartRepository} 與資料庫進行互動。
 * </p>
 */
@Service("cartService")
public class CartService {

	private final RedisTemplate<String, Object> redisTemplate;
	private static final String CART_KEY_MEMBER_PREFIX = "cart:member:";
	private static final String CART_KEY_SESSION_PREFIX = "cart:session:";

	// TODO: 預留：未來購物車同步資料庫時使用
//	@Autowired
//	CartRepository repository;

	// TODO: 預留：未來購物車同步資料庫時使用
//	public void addCart(CartVO cartVO) {
//		repository.save(cartVO);
//	}

	// TODO: 預留：未來購物車同步資料庫時使用
//	public void updateCart(CartVO cartVO) {
//		repository.save(cartVO);
//	}

	// TODO: 預留：未來購物車同步資料庫時使用
//	public void deleteCart(Integer cartId) {
//		if (repository.existsById(cartId))
//			repository.deleteByCartId(cartId);
//	}

	// TODO: 預留：未來購物車同步資料庫時使用
//	public CartVO getOneCart(Integer cartId) {
//		Optional<CartVO> optional = repository.findById(cartId);
//		return optional.orElse(null); // public T orElse(T other) : 如果值存在就回傳其值，否則回傳other的值
//	}

	// TODO: 預留：未來購物車同步資料庫時使用
//	public List<CartVO> getAll() {
//		return repository.findAll();
//	}

	private String getSessionCartKey(String sessionId) {
		return CART_KEY_SESSION_PREFIX + sessionId;
	}

	private String getMemberCartKey(Integer memberId) {
		return CART_KEY_MEMBER_PREFIX + memberId;
	}

	/**
	 * 建構子 (Constructor)
	 *
	 * <p>用於注入 Spring 管理的 {@link RedisTemplate} 物件，
	 * 讓此 Service 可以透過 redisTemplate 與 Redis 資料庫互動。</p>
	 *
	 * @param redisTemplate Spring Boot 自動配置的 RedisTemplate，用來執行各種 Redis 操作
	 */
	public CartService(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 根據cartKey查詢購物車內容。
	 *
	 * @param cartKey 購物車key值。
	 * @return 回傳List<CartRedisVO> 購物車清單。
	 */
	public List<CartRedisVO> getCart(String cartKey) {
		// 從 Redis 的 Hash 結構中取出整個購物車（商品 ID → CartRedisVO）
		Map<Object, Object> entries = redisTemplate.opsForHash().entries(cartKey);
		
		// 建立要回傳的購物車清單
		List<CartRedisVO> cartItems = new ArrayList<>();
		
		// 遍歷 Hash 每一筆商品項目，轉型成 CartRedisVO，並加入清單
		entries.forEach((k, v) -> cartItems.add((CartRedisVO) v));
		
		// 回傳購物車清單
		return cartItems;
	}

	/**
	 * 購物車新增或更新商品內容。
	 *
	 * @param cartKey 購物車key值。
	 * @param productId 商品ID。
	 * @param quantity 商品數量
	 * @return 回傳更新完成的 CartRedisVO。
	 */
	public CartRedisVO addOrUpdateItem(String cartKey, Integer productId, Integer quantity) {
		
		// 依據cartKey, productId 取得購物車內的商品item
		CartRedisVO item = (CartRedisVO) redisTemplate.opsForHash().get(cartKey, productId.toString());

		// 若沒有該商品(item == null)
		if (item == null) {
			// 於購物車新增該商品及數量
			item = new CartRedisVO(productId, quantity);
		} else {
			// 商品已存在, 更新數量及時間
			item.setQuantity(item.getQuantity() + quantity);
			item.setUpdateTime(new Date());
		}

		// 將商品item回寫購物車內(Redis)
		redisTemplate.opsForHash().put(cartKey, productId.toString(), item);
		return item;
	}

	/**
	 * 根據cartKey, productId 修改商品數量。
	 *
	 * @param cartKey 購物車key值。
	 * @param productId 商品ID。
	 * @param quantity 商品數量
	 * @return 回傳List<CartRedisVO> 購物車清單。
	 */
	public CartRedisVO updateQuantity(String cartKey, Integer productId, Integer quantity) {
		
		// 依據cartKey, productId 取得購物車內的商品item
		CartRedisVO item = (CartRedisVO) redisTemplate.opsForHash().get(cartKey, productId.toString());

		// 商品存在, 更新數量
		if (item != null) {
			item.setQuantity(quantity);
			item.setUpdateTime(new Date());
			redisTemplate.opsForHash().put(cartKey, productId.toString(), item);
		}
		
		return item;
	}

	/**
	 * 根據cartKey, productId 移除該商品。
	 *
	 * @param cartKey 購物車key值。
	 * @param productId 商品ID。
	 */
	public void removeItem(String cartKey, Integer productId) {
		redisTemplate.opsForHash().delete(cartKey, productId.toString());
	}

	/**
	 * 根據cartKey清空購物車。
	 *
	 * @param cartKey 購物車key值。
	 */
	public void clearCart(String cartKey) {
		redisTemplate.delete(cartKey);
	}

	/**
	 * 登入時合併購物車
	 * 
	 * @param sessionId 目前的SessionId, 用來尋找未登入狀態時的購物車內容。
	 * @param memberId 登入後的會員Id, 取得該會員的購物車內容。
	 */
	public void loginMergeCart(String sessionId, Integer memberId) {
		String sessionCartKey = getSessionCartKey(sessionId);
		String memberCartKey = getMemberCartKey(memberId);

		// 取出 session 購物車 (未登入)
		Map<Object, Object> sessionEntries = redisTemplate.opsForHash().entries(sessionCartKey);
		// 取會員購物車
		Map<Object, Object> memberEntries = redisTemplate.opsForHash().entries(memberCartKey);

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