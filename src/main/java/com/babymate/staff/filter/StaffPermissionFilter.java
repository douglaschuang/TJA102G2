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

public class StaffPermissionFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(StaffPermissionFilter.class);

	/**
	 * 權限驗證過濾器（Permission Filter）。
	 *
	 * <p>此 Filter 用於檢查使用者是否具有存取後台資源的權限。</p>
	 *
	 * <p>功能包含：</p>
	 * <ul>
	 *   <li>放行登入、登出與靜態資源等不需驗證的 URI。</li>
	 *   <li>若未登入則導向登入頁。</li>
	 *   <li>從 session 中取得登入使用者的權限清單，並檢查目前 URI 是否在許可的 URL 範圍中。</li>
	 *   <li>若有權限則放行，否則導向 403 無權限頁面。</li>
	 * </ul>
	 *
	 * @param req ServletRequest 物件，實際為 HttpServletRequest。
	 * @param res ServletResponse 物件，實際為 HttpServletResponse。
	 * @param chain FilterChain 物件，控制是否繼續後續處理流程。
	 * @throws IOException 資源處理過程中可能發生的輸出入錯誤。
	 * @throws ServletException Servlet 執行過程中可能發生的錯誤。
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		HttpSession session = request.getSession(false); // 不自動建立 session

		String uri = request.getRequestURI();
		logger.info("PermissionFilter triggered, URI = {}", uri);

		// 放行登入頁、登出、靜態資源、權限初始化等無需權限驗證的 URI
		if (uri.endsWith("/admin/login") || uri.endsWith("/loginCheck") || uri.endsWith("/permission")
				|| uri.endsWith("/logout") || uri.contains("/mhb/photo") || uri.contains("/css")
				|| uri.contains("/js")) {
			chain.doFilter(req, res);
			return;
		}

		// 未登入（無 session 或無 staff 屬性），導向登入頁
		if (session == null || session.getAttribute("staff") == null) {
			response.sendRedirect(request.getContextPath() + "/admin/login");
			return;
		}

		// 取得登入者資訊與權限清單
		StaffVO staff = (StaffVO) session.getAttribute("staff");
		
		@SuppressWarnings("unchecked")
		List<PermissionVO> permissions = (List<PermissionVO>) session.getAttribute("permissions");

		// 權限驗證標記
		boolean allowed = false;

		if (permissions != null) {
			for (PermissionVO p : permissions) {
				logger.info("uri = {}, contextPath = {}, permission = {}", uri, request.getContextPath(), p.getUrl());
				
				// 檢查目前請求 URI 是否以權限 URL 開頭（模擬類似「路徑包含」的權限比對）
				if (uri.startsWith(request.getContextPath() + p.getUrl())) {

					allowed = true;
					break;
				}
			}
		}

		if (allowed) {
			logger.info("Permission allowed");
			chain.doFilter(req, res); // 有權限，放行
		} else {
			// 無權限，導向 403 錯誤頁
			logger.info("StaffPermissionFilter - 權限不足: staff nickname: {} , uri: {} , redirect to 403.",
					staff.getNickname(), uri);
			response.sendRedirect(request.getContextPath() + "/error/403");
		}
	}
}
