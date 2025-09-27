package com.babymate.permission.model;

import java.util.List;

public class MenuGroupDTO  implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	 private String groupName;
	 private String icon;
	 private List<PermissionVO> permissions;
	 
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public List<PermissionVO> getPermissions() {
		return permissions;
	}
	public void setPermissions(List<PermissionVO> permissions) {
		this.permissions = permissions;
	}
}
