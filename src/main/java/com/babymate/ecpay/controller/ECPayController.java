package com.babymate.ecpay.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.babymate.cart.service.CartService;
import com.babymate.checkout.model.ChectOutRequestDTO;
import com.babymate.member.model.MemberVO;
import com.babymate.orders.model.OrdersRepository;
import com.babymate.orders.model.OrdersService;
import com.babymate.orders.model.OrdersVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/ecpay")
public class ECPayController {

    /** ↓↓↓ 綠界金鑰 & API 設定（從 application.properties / yml 讀取） */
    @Value("${ecpay.merchant.id}")
    private String merchantId;

    @Value("${ecpay.hash.key}")
    private String hashKey;

    @Value("${ecpay.hash.iv}")
    private String hashIV;

    @Value("${ecpay.api.url}")
    private String apiUrl;

    @Value("${ecpay.return.url}")
    private String returnUrl;     // Server 端通知

    @Value("${ecpay.result.url}")
    private String resultUrl;     // 前端結果頁

    @Autowired
    private OrdersService ordersSvc;
    
    @Autowired
    private CartService cartSvc;
    
    @Autowired
    private OrdersRepository ordersRepo;
    
    /**
     * 將訂單送去綠界（產生自動送出的 form）
     * 來源：前端 checkout 頁的 form 提交
     *
     * 參數：
     * - orderNo：我們系統中的訂單編號（createPendingOrder 產生）
     * - amount：總金額
     * - itemName：綠界用的商品字串（A x2#B x1）
     * - dto：收件資訊（電話/地址/Email...）
     *
     * 流程：
     *  1.檢查表單驗證（有錯回 checkout 頁）
     *  2.將收件資訊更新到訂單（避免之後沒資料）
     *  3.組綠界參數，計算 CheckMacValue
     *  4.交給 /frontend/ecpaySubmit.html 自動送出 POST 到綠界
     */
    @PostMapping("/checkout")
    public String checkout(@RequestParam("orderNo") String orderNo,
    					   @RequestParam("amount") String amount,
                           @RequestParam("itemName") String itemName,
                           ChectOutRequestDTO dto,
                           BindingResult result,
                           HttpSession session,
                           Model model) {

    	if (result.hasErrors()) {
    		// 回到結帳頁，並顯示驗證錯誤（你可以把錯誤渲染到畫面）
            model.addAttribute("errors", result.getAllErrors());
            // 同時記得把購物車/總金額等資料再放回去（略）
            return "frontend/checkout"; // Thymeleaf 模板名稱
    	}
    	
    	/** 1.先把收件資訊寫回訂單（後面通知/對帳才不缺資料） */
    	ordersSvc.updateOrderContact(
    			dto.getOrderNo(),
    	        dto.getRecipient(),
    	        dto.getAddress(),   // 這裡要確保前端真的把完整地址塞進 hidden
    	        dto.getPhone(),
    	        dto.getEmail()
    	);
    	
        String tradeDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());

        /** 2.組綠界參數 */
        Map<String, String> params = new HashMap<>();
        params.put("MerchantID", merchantId);
        params.put("MerchantTradeNo", orderNo);     // orders訂單編號
        params.put("MerchantTradeDate", tradeDate);
        params.put("PaymentType", "aio");
        params.put("TotalAmount", amount);
        params.put("TradeDesc", "測試交易描述");
        params.put("ItemName", itemName);
        params.put("ReturnURL", returnUrl);         // Server端接收付款結果
        params.put("OrderResultURL", resultUrl);    // 前端顯示付款結果
        params.put("ChoosePayment", "Credit");      // 一次付清
        params.put("EncryptType", "1");
//        params.put("SimulatePaid", "1");            // 加入模擬付款參數，測試用可打開

        /** 3.計算檢查碼 */
        String checkMacValue = ECPayCheckMacValue.generate(params, hashKey, hashIV);
        params.put("CheckMacValue", checkMacValue);

        System.out.println("送去綠界 orderNo=" + orderNo);
        
        /** 4.丟給 ecpaySubmit 自動送出 */
        model.addAttribute("params", params);
        model.addAttribute("action", apiUrl);

        return "/frontend/ecpaySubmit"; // 轉到自動送出的form
    }

    /**
     * 綠界付款完成通知（Server to Server）
     * - 綠界以 POST 呼叫這個端點（ReturnURL），不經過使用者瀏覽器
     * - 收到 RtnCode=1 → 視為付款成功 → 更新訂單狀態與金額、付款時間
     * - 回傳字串 "1|OK" 代表已收到（綠界規定）
     */
    @PostMapping("/notify")
    @ResponseBody
    public String paymentNotify(@RequestParam Map<String, String> req) {
        System.out.println("ECPay Notify: " + req);
        try {
            // 只在付款成功（RtnCode=1）時更新
            if ("1".equals(req.get("RtnCode"))) {
                String orderNo = req.get("MerchantTradeNo");         // 我們送出去的 tradeNo
                java.math.BigDecimal amt =
                    new java.math.BigDecimal(req.getOrDefault("TradeAmt", "0"));
                java.time.LocalDateTime payTime = java.time.LocalDateTime.now(); // 或把 req.get("PaymentDate") 轉成時間

                // 將 orders.status=2, pay_time=現在時間, amount=綠界回傳金額
                ordersSvc.markPaidByOrderNo(orderNo, amt, payTime);
            }
            return "1|OK"; // 綠界規定要回這字串
        } catch (Exception ex) {
            ex.printStackTrace();
            return "0|ERROR";
        }
    }

    /**
     * 前端回傳（使用者瀏覽器導回的結果頁，OrderResultURL）
     * - 這裡也會再做一次「標記已付款」與「清購物車」的保險處理
     * - 無論如何都導回「result」頁顯示結果
     */
    @PostMapping("/result")
    public String paymentResult(@RequestParam Map<String, String> req,
    							HttpSession session,
    							Model model) {
    	
    	System.out.println("ECPay Result: " + req);
    	
    	// 只要綠界回到這頁，就把訂單視為付款完成
        // 從回傳資料取出我們送出的訂單編號
        String orderNo = req.get("MerchantTradeNo"); // 就是你 checkout 時放進去的 orderNo
        java.math.BigDecimal paidAmt = null;
        try {
            String amtStr = req.get("TradeAmt");     // 沒有就維持 null，不強制覆寫 DB 金額
            if (amtStr != null) paidAmt = new java.math.BigDecimal(amtStr);
        } catch (Exception ignore) {}

        try {
            /** 1.設成已付款；第一次回來才會寫 pay_time=now；update_time=now */
            ordersSvc.markPaidOnResult(orderNo, paidAmt);
        } catch (Exception e) {
            // 不讓使用者看到錯誤頁，但最好在伺服器看 Log，避免中斷導回
            e.printStackTrace();
        }

        /** 2.用 orderNo 反查 memberId，清空該會員購物車（或訪客 session 的購物車） */
        try {
        	OrdersVO order = ordersRepo.findByOrderNo(orderNo)
        		    .orElseThrow(() -> new RuntimeException("找不到訂單: " + orderNo));
            MemberVO member = order.getMemberVO();   // ← 這裡會用到 getMemberVO()
            if (member != null && member.getMemberId() != null) { // ← 這裡會用到 getMemberId()
                String cartKey = "cart:member:" + member.getMemberId();
                cartSvc.clearCart(cartKey);
                System.out.println("Cleared member cart by orderNo. memberId=" + member.getMemberId());
            } else {
                // 沒會員也嘗試清當前 session 購物車（備援）
                String fallbackKey = "cart:session:" + session.getId();
                cartSvc.clearCart(fallbackKey);
                System.out.println("Cleared session cart (fallback). sid=" + session.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /** 3.印完整回傳參數（除錯用） */
        System.out.println("Result 回來 MerchantTradeNo=" + orderNo);
        System.out.println("=== ECPay Result 全部回傳 ===");
        req.forEach((k, v) -> System.out.println(k + " = " + v));

        /** 4.回商城前結果頁 */
        return "result";
    }
}
