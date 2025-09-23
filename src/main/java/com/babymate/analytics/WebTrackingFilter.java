package com.babymate.analytics;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.*;

@Component
public class WebTrackingFilter implements Filter {

  private static final String COOKIE_NAME = "bm_sid";
  private final TrackingService tracking;

  public WebTrackingFilter(TrackingService tracking) { this.tracking = tracking; }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest  r = (HttpServletRequest) req;
    HttpServletResponse w = (HttpServletResponse) res;

    // 排除靜態資源與健康檢查等路由（依你的專案調整）
    String uri = r.getRequestURI();
    if (uri.startsWith("/assets/") || uri.startsWith("/adminlte/") || uri.startsWith("/css/")
        || uri.startsWith("/js/") || uri.startsWith("/images/") || uri.startsWith("/favicon")
        || uri.startsWith("/actuator")) {
      chain.doFilter(req, res);
      return;
    }

    // 取得或建立 session_id（cookie）
    String sid = null;
    Cookie[] cookies = r.getCookies();
    if (cookies != null) {
      for (Cookie c : cookies) { if (COOKIE_NAME.equals(c.getName())) { sid = c.getValue(); break; } }
    }
    if (sid == null || sid.isBlank()) {
      sid = java.util.UUID.randomUUID().toString().replace("-", "");
      Cookie c = new Cookie(COOKIE_NAME, sid);
      c.setPath("/");
      c.setHttpOnly(true);
      c.setMaxAge(60*60*24*365);
      w.addCookie(c);
    }

    chain.doFilter(req, res);

    // 記錄在 chain 之後，避免阻塞主流程
    LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Taipei"));
    String ip = r.getRemoteAddr();
    String ua = r.getHeader("User-Agent");
    tracking.record(sid, ip, ua, uri, now);
  }
}
