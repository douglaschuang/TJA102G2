package com.babymate.util;

import java.security.SecureRandom;

public class SimpleCaptchaGenerator {
    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static String generateCaptcha(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHAR_POOL.length());
            sb.append(CHAR_POOL.charAt(index));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        // 測試: 產生 6 位數的驗證碼
        String captcha = generateCaptcha(6);
        System.out.println("驗證碼: " + captcha);
    }
}
