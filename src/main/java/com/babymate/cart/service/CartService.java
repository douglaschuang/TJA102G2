package com.babymate.cart.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}