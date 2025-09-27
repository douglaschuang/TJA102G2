package com.babymate.member.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.babymate.member.model.MemberVO;
import com.babymate.staff.model.StaffVO;
import com.babymate.util.EncodingUtil;
import com.babymate.util.SimpleCaptchaGenerator;
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

	public void deleteMember(Integer memberId) {
		if (repository.existsById(memberId))
			repository.deleteByMemberId(memberId);
//		    repository.deleteById(memberVO);
	}

	public MemberVO getOneMember(Integer memberId) {
		Optional<MemberVO> optional = repository.findById(memberId);
//		return optional.get();
		return optional.orElse(null);  // public T orElse(T other) : 如果值存在就回傳其值，否則回傳other的值
	}
	
	public MemberVO getOneMember(String account) {
		Optional<MemberVO> optional = Optional.ofNullable(repository.findByAccount(account));
//		return optional.get();
		return optional.orElse(null);  // public T orElse(T other) : 如果值存在就回傳其值，否則回傳other的值
	}

	public List<MemberVO> getAll() {
		return repository.findAll();
	}

	public MemberVO initMember(String account, String authcode) {
		MemberVO initMemVO = new MemberVO();
		
		initMemVO.setAccount(account);
		initMemVO.setPassword(EncodingUtil.hashMD5(authcode)); // default password (same as authcode)
		initMemVO.setEmailVerified((byte) 0); // 未通過
		initMemVO.setRegisterDate(LocalDateTime.now()); // 註冊時間
		initMemVO.setAccountStatus((byte) 0); // 未啟用
		initMemVO.setEmailAuthToken(authcode); // 產生六位數驗證碼
		initMemVO.setUpdateDate(LocalDateTime.now());
		
		repository.save(initMemVO); // 新增會員
		
		return initMemVO;
		
	}
	
	public MemberVO login(String account, String hashedPassword) {
        return repository.findByAccountAndPassword(account, hashedPassword);
	}
	
	public MemberVO login(String account) {
        return repository.findByAccount(account);
	}
	
//	public List<Staff> getAll(Map<String, String[]> map) {
//		return HibernateUtil_CompositeQuery_Emp3.getAllC(map,sessionFactory.openSession());
//	}

}