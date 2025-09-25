package com.babymate.permission.service;

import java.security.Permission;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.babymate.permission.model.MenuGroupDTO;
import com.babymate.permission.model.PermissionRepository;
import com.babymate.permission.model.PermissionVO;

@Service
public class MenuGroupService {

	@Autowired
    private PermissionRepository permissionRepository;

    public List<MenuGroupDTO> getMenuByRole(Integer roleId) {
        List<PermissionVO> permissions = permissionRepository.findByRoleId(roleId);

        // 分群組
        Map<String, MenuGroupDTO> groupMap = new LinkedHashMap<>();
        for (PermissionVO p : permissions) {
        	
//        	System.out.println("permission"+p.toString());
        	
        	if (p.getType() == 0) // permission不顯示在menu
        	  continue;
        	
//        	System.out.println(p.getType());
            String groupName = p.getMenuGroupVO().getGroupName();
            groupMap.putIfAbsent(groupName, new MenuGroupDTO());
            MenuGroupDTO dto = groupMap.get(groupName);
            dto.setGroupName(groupName);
            dto.setIcon(p.getMenuGroupVO().getIcon());
            if (dto.getPermissions() == null) dto.setPermissions(new ArrayList<>());
            dto.getPermissions().add(p);
        }
        return new ArrayList<>(groupMap.values());
    }
	
}
