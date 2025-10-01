package com.babymate.util;

import java.security.SecureRandom;
import java.util.Objects;

public class SimpleCaptchaGenerator {
	
	// 支援大寫、小寫與數字的字符集
    private static final String DEFAULT_CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 產生指定長度的驗證碼（使用預設字元池）
     * @param length 驗證碼長度
     * @return 驗證碼字串
     * @throws IllegalArgumentException if length <= 0
     */
    public static String generateCaptcha(int length) {
        return generateCaptcha(length, DEFAULT_CHAR_POOL);
    }
    
    /**
     * 產生指定長度與自定義字元池的驗證碼
     * @param length 驗證碼長度
     * @param charPool 使用的字元池
     * @return 驗證碼字串
     * @throws IllegalArgumentException if length <= 0 or charPool is null/empty
     */
    public static String generateCaptcha(int length, String charPool) {
        if (length <= 0) {
            throw new IllegalArgumentException("驗證碼長度必須大於 0");
        }
        if (Objects.isNull(charPool) || charPool.isEmpty()) {
            throw new IllegalArgumentException("字元池不能為 null 或空字串");
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(charPool.length());
            sb.append(charPool.charAt(index));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        // 測試：產生 6 位驗證碼（預設字元池）
        String captcha = generateCaptcha(6);
        System.out.println("驗證碼（預設）: " + captcha);

        // 測試：只用數字產生驗證碼
        String numericCaptcha = generateCaptcha(6, "0123456789");
        System.out.println("驗證碼（數字）: " + numericCaptcha);
    }
}
