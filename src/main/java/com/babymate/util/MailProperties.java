package com.babymate.util;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mail.gmail")
//告訴 Spring Boot 要將設定檔中以 "mail.gmail" 開頭的屬性
//自動注入 (bind) 到這個類別的欄位。
public class MailProperties {

	// 寄件者 Gmail 帳號
	private String sender;
	// 寄件者 Gmail 密碼
	private String password;

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
