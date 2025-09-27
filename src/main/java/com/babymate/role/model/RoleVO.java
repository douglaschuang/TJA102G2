package com.babymate.role.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;

@Entity
@Table(name = "role")
public class RoleVO implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
	private Integer roleId;
	
	@Column(name = "name")
	@NotEmpty(message="角色名稱: 請勿空白")
	private String name;

	@Override
	public String toString() {
		return "RoleVO [roleId=" + roleId + ", name=" + name + "]";
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public String getName() {
	    return name;
	}

	public void setRoleName(String name) {
		this.name = name;
	}

}
