package com.babymate.role.model;

import org.springframework.data.jpa.repository.JpaRepository;
import com.babymate.role.model.RoleVO;

public interface RoleRepository extends JpaRepository<RoleVO, Integer> {

}
