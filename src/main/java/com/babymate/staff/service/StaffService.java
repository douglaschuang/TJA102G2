package com.babymate.staff.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.babymate.staff.model.StaffVO;
import com.babymate.util.EncodingUtil;
import com.babymate.staff.model.StaffRepository;

//import hibernate.util.CompositeQuery.HibernateUtil_CompositeQuery_Emp3;


@Service("staffService")
public class StaffService {

	@Autowired
	StaffRepository repository;
	
	@Autowired
    private SessionFactory sessionFactory;

	public void addStaff(StaffVO staffVO) {
		staffVO.setUpdateDate(LocalDateTime.now());
		repository.save(staffVO);
	}

	public void updateStaff(StaffVO staffVO) {
		staffVO.setUpdateDate(LocalDateTime.now());
		repository.save(staffVO);
	}

	public void deleteStaff(Integer staffId) {
		if (repository.existsById(staffId))
			repository.deleteByStaffId(staffId);
//		    repository.deleteById(staffId);
	}

	public StaffVO getOneStaff(Integer staffId) {
		Optional<StaffVO> optional = repository.findById(staffId);
//		return optional.get();
		return optional.orElse(null);  // public T orElse(T other) : 如果值存在就回傳其值，否則回傳other的值
	}

	public List<StaffVO> getAll() {
		return repository.findAll();
	}
	
	public StaffVO login(String account, String hashedPassword) {
        return repository.findByAccountAndPassword(account, hashedPassword);
	}
	
	public StaffVO getOneStaffByAccount(String account) {
		return repository.findByAccount(account);
	}

//	public List<Staff> getAll(Map<String, String[]> map) {
//		return HibernateUtil_CompositeQuery_Emp3.getAllC(map,sessionFactory.openSession());
//	}

}