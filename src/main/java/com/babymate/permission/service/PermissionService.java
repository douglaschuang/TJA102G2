package com.babymate.permission.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.babymate.permission.model.PermissionRepository;
import com.babymate.permission.model.PermissionVO;

@Service
public class PermissionService {
	
	@Autowired
	private PermissionRepository permissionRepository;

	public List<PermissionVO> getPermissionsByRoleId(Integer roleId) {
		return permissionRepository.findByRoleId(roleId);
	}
}
