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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class MailService {
	
	private static final Logger logger = LoggerFactory.getLogger(MailService.class);
	
	private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "465";
    private static final String SMTP_SOCKET_FACTORY_CLASS = "javax.net.ssl.SSLSocketFactory";
	
	@Autowired
	private MailProperties mailProperties;
	
	@PostConstruct
	public void init() {
		
		if (mailProperties != null) {
            logger.info("MailProperties loaded: {}", mailProperties.getSender());
        } else {
            logger.error("MailProperties is null! Email sending may fail.");
        }
	}

	/**
     * 寄送 Email
     *
     * @param to             收件人
     * @param subject        主旨
     * @param messageContent 內容
     * @param isHtml         是否為 HTML 格式
     */
	public void sendMail(String to, String subject, String messageContent, boolean isHtml) {
		
        if (mailProperties == null) {
            logger.error("MailProperties is null! 無法寄送郵件.");
            return;
        }
        
        final String myGmail = mailProperties.getSender();
		final String myGmail_password = mailProperties.getPassword();

		try {
			// 設定使用SSL連線至 Gmail SMTP Server
			Properties props = new Properties();		
			props.put("mail.smtp.host", SMTP_HOST);
	        props.put("mail.smtp.socketFactory.port", SMTP_PORT);
	        props.put("mail.smtp.socketFactory.class", SMTP_SOCKET_FACTORY_CLASS);
	        props.put("mail.smtp.auth", "true");
	        props.put("mail.smtp.port", SMTP_PORT);

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
			if (isHtml) {
	            // 使用 setContent 方法來傳送 HTML 內容
	            message.setContent(messageContent, "text/html; charset=UTF-8");
	        } else {
	            // 使用 setText 方法來傳送純文字內容
	            message.setText(messageContent);
	        }

			Transport.send(message);
			logger.info("郵件已成功傳送至 {}", to);
		} catch (MessagingException e) {
			logger.error("傳送失敗! {}", e.getMessage(), e);
		}
	}

}
