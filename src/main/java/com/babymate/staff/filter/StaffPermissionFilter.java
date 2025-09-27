package com.babymate.staff.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.core.annotation.Order;

import com.babymate.permission.model.PermissionVO;
import com.babymate.staff.model.StaffVO;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class StaffPermissionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        String uri = request.getRequestURI();
//        System.out.println("PermissionFilter triggered, URI = " + uri);

        // login 頁面、靜態資源直接放行
        if (uri.endsWith("/admin/login") || uri.endsWith("/loginCheck") || uri.endsWith("/permission")|| uri.endsWith("/logout")
        		|| uri.contains("/mhb/photo") || uri.contains("/css") || uri.contains("/js")) {
            chain.doFilter(req, res);
            return;
        }
        
        // 如果還沒登入，直接丟回登入頁
        if (session == null || session.getAttribute("staff") == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }

        // 取得登入者資料
        StaffVO staff = (StaffVO) session.getAttribute("staff");
        @SuppressWarnings("unchecked")
        List<PermissionVO> permissions = (List<PermissionVO>) session.getAttribute("permissions");

        // 權限比對
        boolean allowed = false;

        if (permissions != null) {
            for (PermissionVO p : permissions) {
//            	System.out.println("uri="+uri + ", request.getContextPath()="+request.getContextPath()+", p.getUrl()="+p.getUrl());
//            	System.out.println(uri.startsWith(request.getContextPath() + p.getUrl()));
                if (uri.startsWith(request.getContextPath() + p.getUrl())) { 
                	
                    allowed = true;
                    break;
                }
            }
        }

        if (allowed) {
//        	 System.out.println("permission allowed");
            chain.doFilter(req, res); // 有權限
        } else {
            System.out.println("權限不足：" + staff.getNickname() + " -> " + uri);
//            response.sendError(HttpServletResponse.SC_FORBIDDEN, "您沒有權限訪問此功能");
            response.sendRedirect(request.getContextPath() + "/error/403");
        }
    }
}
