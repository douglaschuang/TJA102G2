package com.babymate.ecpay.controller;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.*;

/**
 * 綠界 CheckMacValue 產生工具
 *
 * 主要步驟：
 *  1) 參數依「鍵名」排序（不含 CheckMacValue 自身）
 *  2) 前後加上 HashKey/HashIV
 *  3) 特定 URL encode / 置換
 *  4) 做 SHA-256 雜湊，轉大寫十六進位
 *
 * 注意：實務上需與綠界文件完全一致（大小寫、編碼、替換規則），否則會驗證失敗。
 */
public class ECPayCheckMacValue {
    public static String generate(Map<String, String> params, String hashKey, String hashIV) {
        /** 1.依照參數名稱排序（自然排序） */
        SortedMap<String, String> sortedParams = new TreeMap<>(params);

        StringBuilder sb = new StringBuilder();
        sb.append("HashKey=").append(hashKey);
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (!entry.getKey().equals("CheckMacValue")) {
                sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        sb.append("&HashIV=").append(hashIV);

        try {
        	/** 2.URL Encode（並做必要的取代，確保與綠界規則一致） */
            String urlEncode = URLEncoder.encode(sb.toString(), "UTF-8")
                    .toLowerCase()
                    .replace("%21", "!")
                    .replace("%28", "(")
                    .replace("%29", ")")
                    .replace("%20", "+")
                    .replace("%2a", "*")
                    .replace("%2d", "-")
                    .replace("%2e", ".")
                    .replace("%5f", "_");

            /** 3.SHA-256 雜湊（結果再轉大寫十六進位字串） */
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] digest = sha256.digest(urlEncode.getBytes("UTF-8"));

            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append("0");
                hex.append(h);
            }
            return hex.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("CheckMacValue 生成失敗", e);
        }
    }
}
