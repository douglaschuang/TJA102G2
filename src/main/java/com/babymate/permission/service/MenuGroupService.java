package com.babymate.permission.service;

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

	/** 注入 PermissionRepository，用來查詢角色對應的權限資料 */
	@Autowired
	private PermissionRepository permissionRepository;

	/**
	 * 根據角色 ID 查詢該角色所擁有的功能選單，並依群組分類。
	 *
	 * @param roleId 角色 ID
	 * @return 依群組整理後的選單列表，每個元素為 {@link MenuGroupDTO}
	 */
	public List<MenuGroupDTO> getMenuByRole(Integer roleId) {
		// 查詢指定角色的所有權限記錄
		List<PermissionVO> permissions = permissionRepository.findByRoleId(roleId);

		// 使用 LinkedHashMap 以維持插入順序（避免群組順序被打亂）
		Map<String, MenuGroupDTO> groupMap = new LinkedHashMap<>();
		
		// 逐筆處理權限資料
		for (PermissionVO p : permissions) {

			// 若 type == 0，代表這個權限不屬於選單項目（可能是 API 權限），略過
			if (p.getType() == 0) 
				continue;

			// 從 PermissionVO 取得所屬群組名稱
			String groupName = p.getMenuGroupVO().getGroupName();
			// 若群組尚未建立，初始化一個新的 MenuGroupDTO
			groupMap.putIfAbsent(groupName, new MenuGroupDTO());
			// 取得該群組的 DTO
			MenuGroupDTO dto = groupMap.get(groupName);
			
			// 設定群組名稱與圖示（Icon）
			dto.setGroupName(groupName);
			dto.setIcon(p.getMenuGroupVO().getIcon());
			
			// 確保 permissions list 已初始化
			if (dto.getPermissions() == null)
				dto.setPermissions(new ArrayList<>());
			
			// 將該權限加入該群組底下
			dto.getPermissions().add(p);
		}
		// 將分群後的結果轉為 List 返回
		return new ArrayList<>(groupMap.values());
	}

}
