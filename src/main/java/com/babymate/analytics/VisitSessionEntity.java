package com.babymate.analytics;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visit_session")
public class VisitSessionEntity implements java.io.Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_id", nullable = false, unique = true, length = 64)
  private String sessionId;

  @Column(name = "ip", length = 64)
  private String ip;

  @Column(name = "user_agent", length = 255)
  private String userAgent;

  @Column(name = "started_at", nullable = false)
  private LocalDateTime startedAt;

  @Column(name = "last_seen_at", nullable = false)
  private LocalDateTime lastSeenAt;
  
//getters / setters

public Long getId() {
	return id;
}

public void setId(Long id) {
	this.id = id;
}

public String getSessionId() {
	return sessionId;
}

public void setSessionId(String sessionId) {
	this.sessionId = sessionId;
}

public String getIp() {
	return ip;
}

public void setIp(String ip) {
	this.ip = ip;
}

public String getUserAgent() {
	return userAgent;
}

public void setUserAgent(String userAgent) {
	this.userAgent = userAgent;
}

public LocalDateTime getStartedAt() {
	return startedAt;
}

public void setStartedAt(LocalDateTime startedAt) {
	this.startedAt = startedAt;
}

public LocalDateTime getLastSeenAt() {
	return lastSeenAt;
}

public void setLastSeenAt(LocalDateTime lastSeenAt) {
	this.lastSeenAt = lastSeenAt;
}

  
  
}
