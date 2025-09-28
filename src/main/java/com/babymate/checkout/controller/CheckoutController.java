package com.babymate.checkout.controller;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostMapping("/create-session")
    @ResponseBody
    public Map<String, Object> createCheckoutSession() throws Exception {
        Stripe.apiKey = stripeApiKey;

        System.out.println("create-session");
        // 建立 Checkout Session
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("http://localhost:8080/checkout/success")
                        .setCancelUrl("http://localhost:8080/checkout/cancel")
                        .setCustomerEmail("testuser@example.com")
                        .addAllPaymentMethodType(
                                Arrays.asList(SessionCreateParams.PaymentMethodType.CARD)
                        )  // 付款方式僅允許信用卡
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("usd")
                                                        .setUnitAmount(2000L) // 單位: 分 (2000 = $20.00)
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("測試商品 A")
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();

        Session session = Session.create(params);
//        System.out.println("session created");
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("id", session.getId());
        responseData.put("url", session.getUrl()); // Stripe 會給完整的結帳網址
//        System.out.println("responseData"+responseData);
        return responseData;
    }

    @GetMapping("/success")
    @ResponseBody
    public String successPage() {
        return "付款成功！";
    }

    @GetMapping("/cancel")
    @ResponseBody
    public String cancelPage() {
        return "付款取消！";
    }
}