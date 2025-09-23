package com.babymate.diary.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "diary_entry")
public class DiaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id")
    private Integer id;

    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @Column(name = "title", length = 200)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "tags", length = 200)
    private String tags;

    @Column(name = "written_at")
    private LocalDateTime writtenAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (writtenAt == null) writtenAt = createdAt;
        if (title == null || title.isBlank()) title = "（無標題）";
    }
    
    // 圖片
    @Lob
    @Column(name = "image_data")
    private byte[] imageData;

    @Column(name = "image_content_type", length = 100)
    private String imageContentType;

    // 便於模板判斷是否有圖
    @Transient
    public boolean hasImage() { return imageData != null && imageData.length > 0; }

    // getter / setter
    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }
    public String getImageContentType() { return imageContentType; }
    public void setImageContentType(String imageContentType) { this.imageContentType = imageContentType; }


    // Getter / Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public LocalDateTime getWrittenAt() { return writtenAt; }
    public void setWrittenAt(LocalDateTime writtenAt) { this.writtenAt = writtenAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
