package com.babymate.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.babymate.staff.filter.StaffLoginFilter;
import com.babymate.staff.filter.StaffPermissionFilter;

/**
 * WebConfig
 *
 * <p>此類別為網站的全域過濾器設定（Filter Configuration）。</p>
 *
 * <p>主要用途：
 * <ul>
 *   <li>註冊自訂的後台登入與權限驗證過濾器。</li>
 *   <li>設定過濾器攔截的 URL 路徑。</li>
 *   <li>設定過濾器執行順序。</li>
 * </ul>
 * </p>
 *
 * <p>過濾器順序說明：
 * <ol>
 *   <li><b>StaffLoginFilter</b>：驗證使用者是否已登入。</li>
 *   <li><b>StaffPermissionFilter</b>：檢查登入使用者是否具有相應權限。</li>
 * </ol>
 * </p>
 *
 * @see com.babymate.staff.filter.StaffLoginFilter
 * @see com.babymate.staff.filter.StaffPermissionFilter
 */
@Configuration
public class WebConfig {
	
	/**
     * 註冊後台登入驗證過濾器。
     * <p>此過濾器會檢查使用者是否已登入系統，若尚未登入，將導向登入頁面。</p>
     *
     * @return FilterRegistrationBean 物件，封裝 StaffLoginFilter 與其設定
     */
    @Bean
    public FilterRegistrationBean<StaffLoginFilter> staffLoginFilter() {
    	
        FilterRegistrationBean<StaffLoginFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new StaffLoginFilter());

        // 攔截 /admin/** 的所有請求（排除 login 自行處理）
        registrationBean.addUrlPatterns("/admin/*", "/pregnancy/*", "/mhb/*", "/staff/*");
        registrationBean.setOrder(1); // 先執行

        return registrationBean;
    }
    
    /**
     * 註冊後台權限驗證過濾器。
     * <p>此過濾器在確認登入後，檢查該使用者是否具有訪問該資源的權限。</p>
     *
     * @return FilterRegistrationBean 物件，封裝 StaffPermissionFilter 與其設定
     */
    @Bean
    public FilterRegistrationBean<StaffPermissionFilter> staffPermissionFilter() {
    	
        FilterRegistrationBean<StaffPermissionFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new StaffPermissionFilter());

        // 攔截 /admin/** 的所有請求（排除 login 自行處理）
        registrationBean.addUrlPatterns("/admin/*", "/pregnancy/*", "/mhb/*", "/staff/*");
        registrationBean.setOrder(2); // 於登入驗證完成後執行

        return registrationBean;
    }
}
