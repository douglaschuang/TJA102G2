package com.babymate.staff.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.babymate.staff.model.StaffVO;
import com.babymate.staff.model.StaffRepository;

@Service("staffService")
public class StaffService {

	@Autowired
	StaffRepository repository;
	
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
	}

	public StaffVO getOneStaff(Integer staffId) {
		Optional<StaffVO> optional = repository.findById(staffId);
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
}