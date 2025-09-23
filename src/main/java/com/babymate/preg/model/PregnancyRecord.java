package com.babymate.preg.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.babymate.clinic.model.Clinic;

@Entity
@Table(name = "pregnancy_record")
public class PregnancyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pregnancy_record_id")
    private Integer pregnancyRecordId;

    @Column(name = "mother_handbook_id", nullable = false)
    private Integer motherHandbookId;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "pregnancy_week", nullable = false)
    private Integer pregnancyWeek;

    @Column(name = "clinic_id")
    private Integer clinicId;

    // @Transient
    // private String clinicName;

    @Column(name = "body_condition")
    private String bodyCondition;

    @Column(name = "next_check_date")
    private LocalDate nextCheckDate;

    @Column(name = "next_reminder")
    private String nextReminder;

    @Column(name = "weight", precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "sp")
    private Integer sp;

    @Column(name = "dp")
    private Integer dp;

    @Column(name = "fhs")
    private String fhs;

    @Column(name = "update_time", insertable = false, updatable = false)
    private LocalDateTime updateTime;

    // ====== Getter/Setter ======
    public Integer getPregnancyRecordId() { return pregnancyRecordId; }
    public void setPregnancyRecordId(Integer id) { this.pregnancyRecordId = id; }

    public Integer getMotherHandbookId() { return motherHandbookId; }
    public void setMotherHandbookId(Integer motherHandbookId) { this.motherHandbookId = motherHandbookId; }

    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }

    public Integer getPregnancyWeek() { return pregnancyWeek; }
    public void setPregnancyWeek(Integer pregnancyWeek) { this.pregnancyWeek = pregnancyWeek; }

    public Integer getClinicId() { return clinicId; }
    public void setClinicId(Integer clinicId) { this.clinicId = clinicId; }

    public String getBodyCondition() { return bodyCondition; }
    public void setBodyCondition(String bodyCondition) { this.bodyCondition = bodyCondition; }

    public LocalDate getNextCheckDate() { return nextCheckDate; }
    public void setNextCheckDate(LocalDate nextCheckDate) { this.nextCheckDate = nextCheckDate; }

    public String getNextReminder() { return nextReminder; }
    public void setNextReminder(String nextReminder) { this.nextReminder = nextReminder; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }

    public Integer getSp() { return sp; }
    public void setSp(Integer sp) { this.sp = sp; }

    public Integer getDp() { return dp; }
    public void setDp(Integer dp) { this.dp = dp; }

    public String getFhs() { return fhs; }
    public void setFhs(String fhs) { this.fhs = fhs; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", referencedColumnName = "clinic_id",
                insertable = false, updatable = false)
    private Clinic clinic;

    public Clinic getClinic() { return clinic; }
    public void setClinic(Clinic clinic) { this.clinic = clinic; }
}
