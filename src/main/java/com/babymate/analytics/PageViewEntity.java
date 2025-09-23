package com.babymate.analytics;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "page_view")
public class PageViewEntity implements java.io.Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_id", nullable = false, length = 64)
  private String sessionId;

  @Column(name = "path", nullable = false, length = 255)
  private String path;

  @Column(name = "viewed_at", nullable = false)
  private LocalDateTime viewedAt;
  
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

public String getPath() {
	return path;
}

public void setPath(String path) {
	this.path = path;
}

public LocalDateTime getViewedAt() {
	return viewedAt;
}

public void setViewedAt(LocalDateTime viewedAt) {
	this.viewedAt = viewedAt;
}

}
