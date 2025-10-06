package com.babymate.checkout.model;

import java.math.BigDecimal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ChectOutRequestDTO {
	
	@NotBlank(message = "訂單編號遺失，請重新結帳")
	private String orderNo;
	
	@NotBlank(message = "請填寫收件人")
	private String recipient;
	
	// 前端送來的完整地址(zipcode+縣市+鄉鎮+詳細地址)
	@NotBlank(message = "請填寫地址")
	private String address;
	
	@NotBlank(message = "請填寫聯絡電話")
	@Pattern(regexp = "^(?!([0-9])\\1{9})09\\d{8}$", message = "電話格式不正確（需 09 開頭共 10 碼）")
	private String phone;
	
	// 可空白，有填才驗
	@Email(message = "Email 格式不正確")
	private String email;
	
	private String itemName;
	
	private BigDecimal amount;

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
}
