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

	/**
     * 根據角色 ID 查詢該角色所擁有的所有權限。
     *
     * <p>此方法會透過 {@link PermissionRepository#findByRoleId(Integer)}  
     * 從資料庫取得 {@link PermissionVO} 清單。</p>
     *
     * @param roleId 角色 ID
     * @return 權限清單，每個元素為 {@link PermissionVO}
     */
	public List<PermissionVO> getPermissionsByRoleId(Integer roleId) {
		return permissionRepository.findByRoleId(roleId);
	}
}
