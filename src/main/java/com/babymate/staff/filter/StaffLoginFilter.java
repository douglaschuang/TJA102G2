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

	/**
	 * 管理後台請求的登入驗證過濾器。
	 *
	 * <p>此過濾器會攔截所有後台請求，並根據使用者是否登入決定是否放行。</p>
	 * 
	 * <ul>
	 *   <li>設定防止快取的 HTTP 標頭。</li>
	 *   <li>允許特定 URI（如登入、登出、靜態資源）直接通過，不檢查登入。</li>
	 *   <li>檢查 session 中是否有登入的員工（staff）資訊，有則放行，否則導向登入頁面。</li>
	 * </ul>
	 *
	 * @param req ServletRequest 請求物件，會轉型為 HttpServletRequest。
	 * @param res ServletResponse 回應物件，會轉型為 HttpServletResponse。
	 * @param chain 過濾器鏈，決定是否繼續放行請求。
	 * @throws IOException 輸入輸出錯誤時拋出。
	 * @throws ServletException Servlet 執行錯誤時拋出。
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		HttpSession session = request.getSession(false); // 不自動建立新 session

		String uri = request.getRequestURI();

		// 防止瀏覽器快取頁面（避免使用者登出後還能按返回鍵看到之前畫面）
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0); 

		logger.info("Filter triggered, URI = {}", request.getRequestURI());

		// 放行不需要登入驗證的路徑
		if (uri.endsWith("/admin/login") || uri.endsWith("/logout") || uri.endsWith("/loginCheck")
				|| uri.endsWith("/permission") || uri.contains("/mhb/photo") || uri.contains("/css")
				|| uri.contains("/js")) {
			logger.info("放行不需要登入驗證的路徑, uri={}",uri);
			chain.doFilter(req, res);
			return;
		}

		// 檢查是否已登入（session 中是否有 "staff" 屬性）
		if (session != null && session.getAttribute("staff") != null) {
			logger.info("Get session or staff success.");

			// 顯示登入者資料
			StaffVO staff = (StaffVO) session.getAttribute("staff");
			logger.info("Get Staff = {}", staff);

			@SuppressWarnings("unchecked")
			List<PermissionVO> permissions = (List<PermissionVO>) session.getAttribute("permissions");
			if (permissions != null) {
				logger.info("Staff permissions: {}", permissions.toString());
			} else {
				logger.info("no permissions got.");
			}

			chain.doFilter(req, res); // 驗證成功，放行請求
			
		} else {
			logger.info("Get session or staff failed.");
			// 導向登入頁面
			response.sendRedirect(request.getContextPath() + "/admin/login");
		}

	}

}
