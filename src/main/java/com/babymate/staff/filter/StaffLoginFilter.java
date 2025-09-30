package com.babymate.staff.filter;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babymate.permission.model.PermissionVO;
import com.babymate.staff.model.StaffVO;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class StaffLoginFilter implements Filter {
	
	private static final Logger logger = LoggerFactory.getLogger(StaffLoginFilter.class);

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        String uri = request.getRequestURI();

        response.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
        response.setHeader("Pragma","no-cache");
        response.setDateHeader("Expires", 0);

//        System.out.println("Filter triggered, URI = " + request.getRequestURI());
        logger.info("Filter triggered, URI = {}", request.getRequestURI());
        
        // 放行不需要登入的頁面
        if (uri.endsWith("/admin/login") || uri.endsWith("/logout") || uri.endsWith("/loginCheck") || uri.endsWith("/permission") 
        		|| uri.contains("/mhb/photo") || uri.contains("/css") || uri.contains("/js")) {
//        	System.out.println("放行不需要登入的頁面");
        	logger.info("放行不需要登入的頁面.");
            chain.doFilter(req, res);
            return;
        }

//        if (session == null )
//        	System.out.println("StaffLoginFilter: session is null");
        
        // 檢查是否登入
        if (session != null && session.getAttribute("staff") != null) {
//        	System.out.println("success");
        	logger.info("Get session or staff success."); 
        	
        	// 取得登入者資料
            StaffVO staff = (StaffVO) session.getAttribute("staff");
            logger.info("Get Staff = {}", staff);
            @SuppressWarnings("unchecked")
            List<PermissionVO> permissions = (List<PermissionVO>) session.getAttribute("permissions");
            logger.info("Staff permissions: {}", permissions.toString());       	
        	
            chain.doFilter(req, res); // 已登入，放行
        } else {
//        	System.out.println("failed");
        	logger.info("Get session or staff failed.");     
            response.sendRedirect(request.getContextPath() + "/admin/login");
        }
		
	}
	
}
