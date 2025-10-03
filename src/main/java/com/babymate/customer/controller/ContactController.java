	package com.babymate.customer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.babymate.util.MailService;


@Controller
@RequestMapping("")
public class ContactController {

    @Autowired
    private MailService mailService; // 把我們的郵差注入進來

    // 方法一：顯示聯絡表單頁面
    @GetMapping("/contact")
    public String showContactForm() {
        return "frontend/contact_form";
    }

    // 方法二：處理表單提交
    @PostMapping("/contact/submit")
    public String handleContactForm(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            RedirectAttributes redirectAttributes) {

        // --- 任務一：寄信給你自己 (管理員) ---
        String toAdminSubject = "【網站回報】來自 " + name + " 的訊息：" + subject;
        
        // 用 HTML 格式化信件內容，比較好看
        String toAdminContent = String.format(
            "<h3>您有一則新的網站回報：</h3>" +
            "<ul>" +
            "<li><b>姓名：</b> %s</li>" +
            "<li><b>Email：</b> %s</li>" +
            "<li><b>主旨：</b> %s</li>" +
            "</ul>" +
            "<h4>訊息內容：</h4>" +
            "<p>%s</p>",
            name, email, subject, message.replace("\n", "<br>")
        );
        
        // 呼叫郵差，把信寄給你自己
        // **注意**：把 "your-admin-email@gmail.com" 換成你自己的 Gmail
        mailService.sendMail("teamusers0816@gmail.com", toAdminSubject, toAdminContent, true);

        // --- 任務二：寄一封自動回覆信給使用者 ---
        String toUserSubject = "Babymate 客戶服務：我們已收到您的訊息";
        String toUserContent = String.format(
            "<h3>親愛的 %s 您好：</h3>" +
            "<p>感謝您的來信，我們已經收到您回報的訊息，將會盡快處理並與您聯繫。</p>" +
            "<p>以下是您提交的訊息摘要：</p>" +
            "<hr>" +
            "<p><b>主旨：</b> %s</p>" +
            "<p><b>內容：</b><br>%s</p>" +
            "<hr>" +
            "<p>祝您有美好的一天！</p>" +
            "<p>Babymate 團隊 敬上</p>",
            name, subject, message.replace("\n", "<br>")
        );

        // 呼叫郵差，把信寄給填表單的人
        mailService.sendMail(email, toUserSubject, toUserContent, true);
        
        // --- 任務三：重導向並顯示成功訊息 ---
        redirectAttributes.addFlashAttribute("successMessage", "訊息已成功送出，感謝您的回報！");
        return "redirect:/contact";
    }
}