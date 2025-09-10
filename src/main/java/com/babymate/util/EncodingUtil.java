package com.babymate.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncodingUtil {
	
//	private static final PasswordEncoder encoder = new BCryptPasswordEncoder();
	
	public static String hashMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());

            // 將 byte[] 轉成 hex 字串
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0'); // 補零
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    // 加密密碼
//    public static String encodePassword(String rawPassword) {
//        return encoder.encode(rawPassword);
//    }

    // 驗證密碼
//    public static boolean matches(String rawPassword, String encodedPassword) {
//        return encoder.matches(rawPassword, encodedPassword);
//    }

}
