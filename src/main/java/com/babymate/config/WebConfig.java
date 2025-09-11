package com.babymate.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.babymate.staff.filter.StaffLoginFilter;

@Configuration
public class WebConfig {
	
    @Bean
    public FilterRegistrationBean<StaffLoginFilter> staffLoginFilter() {
    	
        FilterRegistrationBean<StaffLoginFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new StaffLoginFilter());

        // 攔截 /admin/** 的所有請求（排除 login 自行處理）
        registrationBean.addUrlPatterns("/admin/*");

        return registrationBean;
    }
}
