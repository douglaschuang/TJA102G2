package com.babymate.staff.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.babymate.permission.model.MenuGroupDTO;
import com.babymate.permission.model.PermissionVO;
import com.babymate.permission.service.MenuGroupService;
import com.babymate.permission.service.PermissionService;
import com.babymate.staff.model.StaffVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/staff/permission")
public class StaffPermissionController {

	private static final Logger logger = LoggerFactory.getLogger(StaffPermissionController.class);

	@Autowired
	private MenuGroupService menuService;

	@Autowired
	private PermissionService permissionService;

	@GetMapping("")
	public String dashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
		StaffVO staff = (StaffVO) session.getAttribute("staff");

		if (staff == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "請先登入");
			logger.info("dashboard - 未登入, 要求先登入");
			return "redirect:/admin/login";
		}

		// 取得權限URL清單 (filter過濾用)
		List<PermissionVO> permissions = permissionService.getPermissionsByRoleId(staff.getRoleVO().getRoleId());
		session.setAttribute("permissions", permissions);
		logger.info("dashboard - 取得staff {} 權限URL清單 (filter過濾用)", staff.getAccount());

		// 取得選單及權限URL列表 (頁面顯示用)
		List<MenuGroupDTO> menus = menuService.getMenuByRole(staff.getRoleVO().getRoleId());
		session.setAttribute("menus", menus);
		logger.info("dashboard - 取得staff {} 取得選單及權限URL列表 (頁面顯示用)", staff.getAccount());
		
		return "redirect:/admin/dashboard";

	}

}
