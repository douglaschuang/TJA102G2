package com.babymate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcViewConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 首頁
        registry.addViewController("/").setViewName("frontend/index");

        // 一般頁
        registry.addViewController("/faq").setViewName("frontend/faq");
        registry.addViewController("/my-account").setViewName("frontend/my-account");
        registry.addViewController("/my-orders").setViewName("frontend/my-orders");
        registry.addViewController("/element/google-maps").setViewName("frontend/element-google-maps");

        // Forum
        registry.addViewController("/forum")
                .setViewName("frontend/forum");
        registry.addViewController("/blog/standard-left")
                .setViewName("frontend/blog-standard-left-sidebar");

        // Shop
        registry.addViewController("/shop/cart").setViewName("frontend/shop-cart");
        registry.addViewController("/shop/checkout").setViewName("frontend/shop-checkout");
        registry.addViewController("/shop/login").setViewName("frontend/shop-customer-login");
        registry.addViewController("/shop/left").setViewName("frontend/shop-left-sidebar");
        registry.addViewController("/shop/product-basic").setViewName("frontend/shop-product-basic");
        registry.addViewController("/shop/wishlist").setViewName("frontend/shop-wishlist");
        
    }
}
