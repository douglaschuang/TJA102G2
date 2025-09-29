package com.babymate.ecpay.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/ecpay")
public class ECPayController {

    // 測試環境金鑰
    @Value("${ecpay.merchant.id}")
    private String merchantId;

    @Value("${ecpay.hash.key}")
    private String hashKey;

    @Value("${ecpay.hash.iv}")
    private String hashIV;

    @Value("${ecpay.api.url}")
    private String apiUrl;

    @Value("${ecpay.return.url}")
    private String returnUrl;

    @Value("${ecpay.result.url}")
    private String resultUrl;

    @PostMapping("/checkout")
    public String checkout(@RequestParam("amount") String amount,
                           @RequestParam("itemName") String itemName,
                           Model model) {

        String tradeNo = "EC" + System.currentTimeMillis(); // 訂單編號
        String tradeDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());

        Map<String, String> params = new HashMap<>();
        params.put("MerchantID", merchantId);
        params.put("MerchantTradeNo", tradeNo);
        params.put("MerchantTradeDate", tradeDate);
        params.put("PaymentType", "aio");
        params.put("TotalAmount", amount);
        params.put("TradeDesc", "測試交易描述");
        params.put("ItemName", itemName);
        params.put("ReturnURL", returnUrl);  // Server端接收付款結果
        params.put("OrderResultURL", resultUrl); // 前端顯示付款結果
        params.put("ChoosePayment", "Credit"); // 一次付清
        params.put("EncryptType", "1");

        // 計算檢查碼
        String checkMacValue = ECPayCheckMacValue.generate(params, hashKey, hashIV);
        params.put("CheckMacValue", checkMacValue);

        model.addAttribute("params", params);
        model.addAttribute("action", apiUrl);

        return "/frontend/ecpaySubmit"; // 轉到自動送出的form
    }

    // 付款完成通知 (Server to Server)
    @PostMapping("/notify")
    @ResponseBody
    public String paymentNotify(@RequestParam Map<String, String> req) {
        System.out.println("ECPay Notify: " + req);
        return "1|OK"; // 綠界規定固定回覆
    }

    // 前端回傳 (付款完成頁)
    @PostMapping("/result")
    public String paymentResult(@RequestParam Map<String, String> req, Model model) {
        model.addAttribute("result", req);
        return "result";
    }
}
