package com.babymate.ecpay.controller;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.*;

public class ECPayCheckMacValue {
    public static String generate(Map<String, String> params, String hashKey, String hashIV) {
        // 依照參數名稱排序
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

//            MessageDigest md5 = MessageDigest.getInstance("SHA-256");
//            byte[] digest = md5.digest(urlEncode.getBytes("UTF-8"));
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
