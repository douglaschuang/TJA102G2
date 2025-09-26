package com.babymate.util;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailService {

	// 設定傳送郵件:至收信人的Email信箱,Email主旨,Email內容
	public void sendMail(String to, String subject, String messageContent, boolean isHtml) {

		try {
			// 設定使用SSL連線至 Gmail smtp Server
			Properties props = new Properties();
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.socketFactory.port", "465");
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.port", "465");

	        // ●設定 gmail 的帳號 & 密碼 (將藉由你的Gmail來傳送Email)
	        // ●1) 登入你的Gmail的: 
	        // ●2) 點選【管理你的 Google 帳戶】
	        // ●3) 點選左側的【安全性】
	       
	        // ●4) 完成【兩步驟驗證】的所有要求如下:
	        //     ●4-1) (請自行依照步驟要求操作之.....)
	       
	        // ●5) 完成【應用程式密碼】的所有要求如下:
	        //     ●5-1) 下拉式選單【選取應用程式】--> 選取【郵件】
	        //     ●5-2) 下拉式選單【選取裝置】--> 選取【Windows 電腦】
	        //     ●5-3) 最後按【產生】密碼
			final String myGmail = "ixlogic.wu@gmail.com";
			final String myGmail_password = "ddjomltcnypgcstn";
			Session session = Session.getInstance(props, new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(myGmail, myGmail_password);
				}
			});

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(myGmail));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

			// 設定信中的主旨
			message.setSubject(subject);
			// 設定信中的內容
//			message.setText(messageText);
			if (isHtml) {
	            // 使用 setContent 方法來傳送 HTML 內容
	            message.setContent(messageContent, "text/html; charset=UTF-8");
	        } else {
	            // 使用 setText 方法來傳送純文字內容
	            message.setText(messageContent);
	        }

			Transport.send(message);
			System.out.println("傳送成功!");
		} catch (MessagingException e) {
			System.out.println("傳送失敗!");
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {

		String to = "douglas.chuang@gmail.com";

		String subject = "密碼通知";

		String ch_name = "peter1";
		String passRandom = "111";
		String messageText = "Hello! " + ch_name + " 請謹記此密碼: " + passRandom + "\n" + " (已經啟用)";

		MailService mailService = new MailService();
//		mailService.sendMail(to, subject, messageText, false);
		
		String messageHtml = "<!DOCTYPE html> " +
		"<html lang=\"zh-TW\"> " +
		"<head> " +
		    "<meta charset=\"UTF-8\"> " +
		    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"> " +
		    "<title>驗證您的身份</title> " +
		"</head> " +
		"<body style=\"font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0;\"> " +
		"<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"background-color: #f4f4f4;\"> " +
		    "<tr> " +
		        "<td align=\"center\" style=\"padding: 20px 0;\"> " +
		            "<table width=\"600\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);\"> " +
		                "<tr> " +
		                    "<div style=\"font-size: 36px; font-weight: bold; padding: 10px 0; letter-spacing: 2px; font-family: 'Arial Rounded MT Bold', Arial, sans-serif;\"> " + 
		                    "BABYMATE" +
		                    "</div> " +
		                "</tr> " +
		                "<tr> " +
		                    "<td style=\"padding: 40px; text-align: center;\"> " +
		                        "<h1 style=\"color: #333333; font-size: 24px; font-weight: bold; margin: 0 0 10px 0;\">驗證您的身份</h1> " +
		                        "<p style=\"color: #666666; font-size: 16px; margin: 0 0 30px 0;\">請輸入此驗證碼以完成您的登入或操作。</p> " +
		                        "<div style=\"background-color: #f0f0f0; border: 1px solid #dddddd; padding: 20px; border-radius: 6px; display: inline-block;\"> " +
		                            "<h2 style=\"color: #333333; font-size: 36px; font-weight: bold; margin: 0; letter-spacing: 5px;\">123456</h2> " +
		                        "</div> " +
		                    "</td> " +
		                "</tr> " +
		                "<tr> " +
		                    "<td style=\"padding: 40px; background-color: #f8f8f8; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px;\"> " +
		                        "<p style=\"color: #333333; font-size: 14px; line-height: 1.5; margin: 0 0 10px 0;\"> " +
		                            "如果您需要額外協助，請至BabyMate官網聯絡客服。 " +
		                        "</p> " +
		                        "<p style=\"color: #666666; font-size: 14px; line-height: 1.5; margin: 0;\"> " +
		                            "請注意，我們絕不會透過電子郵件向您索取密碼、信用卡或銀行帳號等個人資訊。如果您收到可疑信件，請不要點擊任何連結，並立即回報。" +
		                        "</p> " +
		                    "</td> " +
		                "</tr> " +
		            "</table> " +
		        "</td> " +
		    "</tr> " +
		"</table> " +
		"</body> " +
		"</html>";
		
		mailService.sendMail(to, subject, messageHtml, true);
	}

}
