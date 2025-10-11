package com.babymate.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncodingUtil {

	/**
	 * 將輸入字串以 MD5 演算法進行雜湊運算。
	 *
	 * @param input 要進行雜湊的原始字串
	 * @return 轉換後的 MD5 十六進位字串（長度 32）
	 * @throws RuntimeException 若系統不支援 MD5 演算法
	 */
	public static String hashMD5(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			// 將字串轉換為位元組陣列並進行雜湊運算
			byte[] digest = md.digest(input.getBytes());

			// 將位元組結果轉為十六進位字串
			StringBuilder hexString = new StringBuilder();
			for (byte b : digest) {
				// 0xff & b 將 byte 轉為不帶符號的整數
				String hex = Integer.toHexString(0xff & b);
				// 若為單一位元（例如 "a"），前面補 0 變成 "0a"
				if (hex.length() == 1)
					hexString.append('0'); // 補零
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("MD5 algorithm not available", e);
		}
	}

}
