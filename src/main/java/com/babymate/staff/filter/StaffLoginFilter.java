package com.babymate.staff.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter
public class StaffLoginFilter implements Filter {

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

        System.out.println("Filter triggered, URI = " + request.getRequestURI());
        
        // 放行不需要登入的頁面
        if (uri.endsWith("/admin/login") || uri.contains("/css") || uri.contains("/js")) {
            chain.doFilter(req, res);
            return;
        }

//        System.out.println(session.getAttribute("staff"));
        
        // 檢查是否登入
        if (session != null && session.getAttribute("staff") != null) {
        	System.out.println("success");
            chain.doFilter(req, res); // 已登入，放行
        } else {
        	System.out.println("failed");
            response.sendRedirect(request.getContextPath() + "/admin/login");
        }
		
	}
	
}
