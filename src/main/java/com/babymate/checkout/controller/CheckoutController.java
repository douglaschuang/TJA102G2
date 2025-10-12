package com.babymate.checkout.controller;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 使用 stripe 國外信用卡，與目前綠界ECPay無關 
 */
@Controller
@RequestMapping("/checkout")
public class CheckoutController {

	/** 讀取 application.properties 內設定的 Stripe API 金鑰 */
    /** stripe.api.key=sk_test_xxxxxxxxxxxxxxx */
    @Value("${stripe.api.key}")
    private String stripeApiKey;

    /**
     * 【Stripe 建立結帳會話 (Checkout Session)】
     * - 路徑：POST /checkout/create-session
     * - 用於與 Stripe 伺服器溝通，生成一個可用的線上付款頁面網址 (session.url)
     * - 回傳 JSON 給前端，前端再導向 Stripe 提供的結帳畫面
     */
    @PostMapping("/create-session")
    @ResponseBody
    public Map<String, Object> createCheckoutSession() throws Exception {
    	/** 1.設定 Stripe 的 API 金鑰，讓 SDK 知道要呼叫哪個商家帳號 */
        Stripe.apiKey = stripeApiKey;

        System.out.println("create-session");
        /** 2.建立 Checkout Session 參數設定物件 */
        SessionCreateParams params =
                SessionCreateParams.builder()
                		/** 模式設定：PAYMENT 表示單次付款（其他可選：subscription 訂閱） */
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        /** Stripe 成功付款後導回的網址 */
                        .setSuccessUrl("http://localhost:8080/checkout/success")
                        /** Stripe 使用者取消付款時導回的網址 */
                        .setCancelUrl("http://localhost:8080/checkout/cancel")
                        /** 可選：預設顧客 Email（實務上應帶會員 Email） */
                        .setCustomerEmail("testuser@example.com")
                        /** 3.設定可使用的付款方式（這裡僅限信用卡） */
                        .addAllPaymentMethodType(
                                Arrays.asList(SessionCreateParams.PaymentMethodType.CARD)
                        )  // 付款方式僅允許信用卡
                        
                        /** 4.建立一筆商品項目（LineItem） */
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L) // 數量：1
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("usd")   // 幣別：美元（USD）
                                                        .setUnitAmount(2000L) // 價格單位: 分 (2000 = $20.00)
                                                        .setProductData(       // 商品名稱
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("測試商品 A")
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();
        
        /** 5.呼叫 Stripe API 建立 Session（真正發送 HTTPS 請求） */
        Session session = Session.create(params);
//        System.out.println("session created");
        
        /** 6.回傳給前端的資料（通常包含 session.id 與 redirect url） */
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("id", session.getId());   // Stripe Session 的唯一 ID
        responseData.put("url", session.getUrl()); // Stripe 會給完整的結帳網址
//        System.out.println("responseData"+responseData);
        return responseData;                       // 前端可以取得這個 URL，然後使用 JavaScript 導向 Stripe 結帳畫面
    }

    /**
     * 【Stripe 付款成功頁】
     * - Stripe 成功付款後，會依上面 setSuccessUrl() 導回此路徑
     * - 實務上這裡可顯示成功畫面或更新訂單狀態
     */
    @GetMapping("/success")
    @ResponseBody
    public String successPage() {
        return "付款成功！";
    }

    /**
     * 【Stripe 付款取消頁】
     * - Stripe 結帳時使用者取消付款，會導回此路徑
     * - 可提示「付款已取消」並提供返回商城按鈕
     */
    @GetMapping("/cancel")
    @ResponseBody
    public String cancelPage() {
        return "付款取消！";
    }
}