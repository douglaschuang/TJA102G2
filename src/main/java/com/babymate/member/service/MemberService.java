package com.babymate.member.service;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.babymate.member.model.MemberVO;
import com.babymate.member.model.MemberRepository;

//import hibernate.util.CompositeQuery.HibernateUtil_CompositeQuery_Emp3;


@Service("memberService")
public class MemberService {

	@Autowired
	MemberRepository repository;
	
	@Autowired
    private SessionFactory sessionFactory;

	public void addMember(MemberVO memberVO) {
		repository.save(memberVO);
	}

	public void updateMember(MemberVO memberVO) {
		repository.save(memberVO);
	}

	public void deleteMember(Integer memberVO) {
		if (repository.existsById(memberVO))
			repository.deleteByMemberId(memberVO);
//		    repository.deleteById(memberVO);
	}

	public MemberVO getOneMember(Integer memberVO) {
		Optional<MemberVO> optional = repository.findById(memberVO);
//		return optional.get();
		return optional.orElse(null);  // public T orElse(T other) : 如果值存在就回傳其值，否則回傳other的值
	}

	public List<MemberVO> getAll() {
		return repository.findAll();
	}

//	public List<Staff> getAll(Map<String, String[]> map) {
//		return HibernateUtil_CompositeQuery_Emp3.getAllC(map,sessionFactory.openSession());
//	}

}