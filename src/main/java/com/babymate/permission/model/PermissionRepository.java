// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/

package com.babymate.permission.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PermissionRepository extends JpaRepository<PermissionVO, Integer> {
	
	@Query("SELECT rp.permissionVO FROM RolePermissionVO rp WHERE rp.roleVO.roleId = :roleId")
    List<PermissionVO> findByRoleId(@Param("roleId") Integer roleId);

}