package com.babymate.calendar.model;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;

import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.util.*;

@Service
public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "My Google Calendar API";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    // ✅ 從資料夾中讀取該使用者的 Credential（前提是他已經授權過）
    private Credential getCredentials(final NetHttpTransport httpTransport, String userId) throws Exception {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(getClass().getResourceAsStream(CREDENTIALS_FILE_PATH)));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets,
                Collections.singleton(CalendarScopes.CALENDAR))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        System.out.println("🟡 嘗試讀取 userId: " + userId);
        Credential credential = flow.loadCredential(userId);
        if (credential == null) {
            System.out.println("🛑 找不到 userId 的 Credential：請重新連結 Google 帳號");
        }
        
        return flow.loadCredential(userId); // 如果沒有授權過會回傳 null
    }

    private Calendar getCalendarService(String userId) throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = getCredentials(httpTransport, userId);

        if (credential == null || credential.getAccessToken() == null) {
            throw new RuntimeException("尚未授權 Google Calendar，請先進行 OAuth 授權流程");
        }

        return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<Map<String, Object>> getEvents(String userId, String start, String end) throws Exception {
        Calendar calendar = getCalendarService(userId);
        DateTime startDate = new DateTime(start);
        DateTime endDate = new DateTime(end);

        Events events = calendar.events().list("primary")
                .setMaxResults(100)
                .setTimeMin(startDate)
                .setTimeMax(endDate)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Event e : events.getItems()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("title", e.getSummary());
            map.put("start", getDateTimeString(e.getStart()));
            map.put("end", getDateTimeString(e.getEnd()));
            result.add(map);
        }
        return result;
    }

    public Map<String, Object> createEvent(String userId, String summary, String start, String end) throws Exception {
        Calendar calendar = getCalendarService(userId);

        Event event = new Event()
                .setSummary(summary)
                .setStart(new EventDateTime().setDateTime(new DateTime(start)))
                .setEnd(new EventDateTime().setDateTime(new DateTime(end)));

        Event created = calendar.events().insert("primary", event).execute();

        Map<String, Object> map = new HashMap<>();
        map.put("id", created.getId());
        map.put("title", created.getSummary());
        map.put("start", getDateTimeString(created.getStart()));
        map.put("end", getDateTimeString(created.getEnd()));
        return map;
    }

    public Map<String, Object> updateEvent(String userId, String eventId, String summary, String start, String end) throws Exception {
        Calendar calendar = getCalendarService(userId);
        Event event = calendar.events().get("primary", eventId).execute();

        event.setSummary(summary);
        event.setStart(new EventDateTime().setDateTime(new DateTime(start)));
        event.setEnd(new EventDateTime().setDateTime(new DateTime(end)));

        Event updated = calendar.events().update("primary", eventId, event).execute();

        Map<String, Object> map = new HashMap<>();
        map.put("id", updated.getId());
        map.put("title", updated.getSummary());
        map.put("start", getDateTimeString(updated.getStart()));
        map.put("end", getDateTimeString(updated.getEnd()));
        return map;
    }

    public void deleteEvent(String userId, String eventId) throws Exception {
        Calendar calendar = getCalendarService(userId);
        calendar.events().delete("primary", eventId).execute();
    }

    private String getDateTimeString(EventDateTime dateTime) {
        if (dateTime.getDateTime() != null) {
            return dateTime.getDateTime().toStringRfc3339();
        } else if (dateTime.getDate() != null) {
            return dateTime.getDate().toString();  // All-day event
        } else {
            return null;
        }
    }
}
