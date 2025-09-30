package com.babymate.permission.model;

import com.babymate.role.model.RoleVO;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 後台角色權限實體類別
 */
@Entity
@Table(name = "role_permission")
public class RolePermissionVO implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer rolePermissionId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "role_id")
	private RoleVO roleVO;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "permission_id")
	private PermissionVO permissionVO;

	public Integer getRolePermissionId() {
		return rolePermissionId;
	}

	public void setRolePermissionId(Integer rolePermissionId) {
		this.rolePermissionId = rolePermissionId;
	}

	public RoleVO getRoleVO() {
		return roleVO;
	}

	public void setRoleVO(RoleVO roleVO) {
		this.roleVO = roleVO;
	}

	public PermissionVO getPermissionVO() {
		return permissionVO;
	}

	public void setPermissionVO(PermissionVO permissionVO) {
		this.permissionVO = permissionVO;
	}
}
