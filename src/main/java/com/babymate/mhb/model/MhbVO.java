package com.babymate.mhb.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "mother_handbook")
@SQLDelete(sql = "UPDATE mother_handbook SET deleted = 1, deleted_at = CURRENT_TIMESTAMP WHERE mother_handbook_id = ?")
@Where(clause = "deleted = 0") // 預設只查未刪資料（垃圾桶請用 native）
public class MhbVO implements Serializable {
    private static final long serialVersionUID = 1L;

    // ==== 主鍵 ====
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mother_handbook_id")
    private Integer motherHandbookId;

    // ==== 基本欄位 ====
    @NotNull(message = "會員編號: 請勿空白")
    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @NotEmpty(message = "媽媽姓名: 請勿空白")
    @Size(min = 2, max = 10, message = "媽媽姓名: 長度必需在{min}到{max}之間")
    @Column(name = "mother_name", length = 50, nullable = false)
    private String motherName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "mother_birthday")
    private LocalDate motherBirthday;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "last_mc_date")
    private LocalDate lastMcDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "expected_birth_date")
    private LocalDate expectedBirthDate;

    @PositiveOrZero(message = "體重不可為負數")
    @Digits(integer = 5, fraction = 2)
    @Column(name = "weight", precision = 5, scale = 2)
    private BigDecimal weight;

    // ==== 圖片上傳（BLOB）====
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "upfiles")
    private byte[] upFiles;

    // ==== 自動更新時間（交由 DB 維護）====
    @Column(name = "update_time", nullable = false, insertable = false, updatable = false)
    private Timestamp updateTime;

    // ==== 軟刪欄位 ====
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ===== Getter / Setter =====
    public Integer getMotherHandbookId() { return motherHandbookId; }
    public void setMotherHandbookId(Integer motherHandbookId) { this.motherHandbookId = motherHandbookId; }

    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }

    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }

    public LocalDate getMotherBirthday() { return motherBirthday; }
    public void setMotherBirthday(LocalDate d) { this.motherBirthday = d; }

    public LocalDate getLastMcDate() { return lastMcDate; }
    public void setLastMcDate(LocalDate d) { this.lastMcDate = d; }

    public LocalDate getExpectedBirthDate() { return expectedBirthDate; }
    public void setExpectedBirthDate(LocalDate d) { this.expectedBirthDate = d; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }

    public byte[] getUpFiles() { return upFiles; }
    public void setUpFiles(byte[] upFiles) { this.upFiles = upFiles; }

    public Timestamp getUpdateTime() { return updateTime; }
    public void setUpdateTime(Timestamp updateTime) { this.updateTime = updateTime; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
