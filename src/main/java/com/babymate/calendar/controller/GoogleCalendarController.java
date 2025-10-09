package com.babymate.calendar.controller;


import com.babymate.calendar.model.GoogleCalendarService;
import com.babymate.member.model.MemberVO;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

@RestController
public class GoogleCalendarController {

    @Autowired
    private GoogleCalendarService calendarService;

    private MemberVO getMember(HttpSession session) {
        return (MemberVO) session.getAttribute("member");
    }
    
    @GetMapping("/google-auth")
    public void authGoogle(HttpServletResponse response, HttpSession session) throws Exception {
        MemberVO member = (MemberVO) session.getAttribute("member");
        if (member == null) {
            response.sendRedirect("/login");
            return;
        }

        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        InputStream in = getClass().getResourceAsStream("/credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));

        // 完成 OAuth2 callback 後，建立並儲存 Credential
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                transport, jsonFactory, clientSecrets, List.of(CalendarScopes.CALENDAR))
                .setAccessType("offline")
                .setApprovalPrompt("force") // 強制要求授權
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                .build();
    
        String redirectUri = "http://localhost:8080/oauth2callback";
        String authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUri).build();

        response.sendRedirect(authorizationUrl);
    }

    @GetMapping("/oauth2callback")
    public void oauth2Callback(@RequestParam("code") String code, HttpSession session, HttpServletResponse response) throws Exception {
        MemberVO member = (MemberVO) session.getAttribute("member");
        if (member == null) {
            response.sendRedirect("/login");
            return;
        }

        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        InputStream in = getClass().getResourceAsStream("/credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));
        
        // 建立 flow，用來儲存 Credential 到本地
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                transport, jsonFactory, clientSecrets, List.of(CalendarScopes.CALENDAR))
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens"))) // 寫入 tokens 資料夾
                .build();
        
        String redirectUri = "http://localhost:8080/oauth2callback";

        // 使用授權碼換取 token
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                .setRedirectUri(redirectUri)
                .execute();

     // ❗❗ 建立並「儲存」Credential（這一步會自動寫入到 tokens/）
        String userId = member.getMemberId().toString(); // userId 必須與 service 中相同
        Credential credential = flow.createAndStoreCredential(tokenResponse, userId);

        // 可以暫時存到 session，以便當前頁面直接用
        session.setAttribute("google_credential", credential);

        // Debug log
        System.out.println("✅ Access Token: " + credential.getAccessToken());
        System.out.println("✅ Refresh Token: " + credential.getRefreshToken());
        System.out.println("✅ Expire Time: " + credential.getExpirationTimeMilliseconds());
        System.out.println("✅ OAuth 成功，儲存 Credential 給 userId: " + userId);
        
        // 導回日曆畫面
        response.sendRedirect("/blog/full-grid-left?tab=calendar");
    }

    @GetMapping("/api/google-calendar/events")
    public ResponseEntity<?> getEvents(@RequestParam String start, @RequestParam String end, HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute("member");
        if (member == null) {
            return ResponseEntity.status(401).body("請先登入會員");
        }
        
        try {
            System.out.println("🔎 開始取得 Google Calendar 事件，使用者ID：" + member.getMemberId());
            System.out.println("🔎 時間區間：" + start + " ~ " + end);
            
            List<Map<String, Object>> events = calendarService.getEvents(
                member.getMemberId().toString(), start, end
            );
            return ResponseEntity.ok(events);
        } catch (Exception e) {

            e.printStackTrace(); 
            return ResponseEntity.status(500).body("🚫 錯誤：" + e.getMessage());
        }
        
    }


    @PostMapping("/api/google-calendar/events")
    public ResponseEntity<?> createEvent(HttpSession session, @RequestBody Map<String, String> payload) {
        MemberVO member = getMember(session);
        if (member == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "請先登入會員");
        }
        try {
            Map<String, Object> event = calendarService.createEvent(
                member.getMemberId().toString(),
                payload.get("summary"),
                payload.get("start"),
                payload.get("end")
            );
            return ResponseEntity.ok(event);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("新增事件失敗：" + e.getMessage());
        }
    }

    @PutMapping("/api/google-calendar/events/{id}")
    public ResponseEntity<?> updateEvent(HttpSession session, @PathVariable String id,
                                         @RequestBody Map<String, String> payload) {
        MemberVO member = getMember(session);
        if (member == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "請先登入會員");
        }
        try {
            Map<String, Object> event = calendarService.updateEvent(
                member.getMemberId().toString(), id,
                payload.get("summary"),
                payload.get("start"),
                payload.get("end")
            );
            return ResponseEntity.ok(event);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("更新事件失敗：" + e.getMessage());
        }
    }

    @DeleteMapping("/api/google-calendar/events/{id}")
    public ResponseEntity<?> deleteEvent(HttpSession session, @PathVariable String id) {
        MemberVO member = getMember(session);
        if (member == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "請先登入會員");
        }
        try {
            calendarService.deleteEvent(member.getMemberId().toString(), id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("刪除事件失敗：" + e.getMessage());
        }
    }
}
