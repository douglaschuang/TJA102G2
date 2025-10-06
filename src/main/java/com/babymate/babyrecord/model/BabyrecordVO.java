package com.babymate.babyrecord.model;

import java.math.BigDecimal;
import java.util.Date;
import java.sql.Timestamp;

import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.babymate.babyhandbook.model.BabyhandbookVO;
import com.babymate.clinic.model.Clinic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

//
@Entity
@Table(name = "baby_record")
public class BabyrecordVO implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "baby_record_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer babyrecordid;

	@ManyToOne
	@JoinColumn(name = "baby_handbook_id")
	@NotNull(message = "嬰兒手冊ID不得為空")
	private BabyhandbookVO babyhandbook;

	@Column(name = "baby_week")
	@NotNull(message = "嬰兒週數不得為空")
	private Integer babyweek;

	@Column(name = "visit_date")
	@NotNull(message = "請選擇檢查日期")
	@Past(message = "日期必須是在今日(含)之前")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date visitdate;

	@Column(name = "clinic_id")
	private Integer clinicid;
	
	@ManyToOne
	@JoinColumn(name = "clinic_id", insertable = false, updatable = false)
	private Clinic clinic;

	@Column(name = "body_condition")
	@Size(max = 100, message = "不得超過 100 字元")
	private String bodycondition;

	@Column(name = "next_check_date")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date nextcheckdate;

	@Column(name = "next_reminder")
	@Size(max = 100, message = "不得超過 100 字元")
	private String nextreminder;

	@Column(name = "height", precision = 5, scale = 2)
	private BigDecimal height;

	@Column(name = "weight", precision = 5, scale = 2)
	private BigDecimal weight;

	@Column(name = "hc", precision = 5, scale = 2)
	private BigDecimal hc;

	@Column(name = "baby_record_files")
	private byte[] babyrecordfiles;

	@Column(name = "update_time")
	@UpdateTimestamp
	private Timestamp updatetime;

	public Integer getBabyrecordid() {
		return babyrecordid;
	}

	public void setBabyrecordid(Integer babyrecordid) {
		this.babyrecordid = babyrecordid;
	}

	public BabyhandbookVO getBabyhandbook() {
		return babyhandbook;
	}

	public void setBabyhandbook(BabyhandbookVO babyhandbook) {
		this.babyhandbook = babyhandbook;
	}

	public Integer getBabyweek() {
		return babyweek;
	}

	public void setBabyweek(Integer babyweek) {
		this.babyweek = babyweek;
	}

	public Date getVisitdate() {
		return visitdate;
	}

	public void setVisitdate(Date visitdate) {
		this.visitdate = visitdate;
	}

	public Integer getClinicid() {
		return clinicid;
	}

	public void setClinicid(Integer clinicid) {
		this.clinicid = clinicid;
	}

	public String getBodycondition() {
		return bodycondition;
	}

	public void setBodycondition(String bodycondition) {
		this.bodycondition = bodycondition;
	}

	public Date getNextcheckdate() {
		return nextcheckdate;
	}

	public void setNextcheckdate(Date nextcheckdate) {
		this.nextcheckdate = nextcheckdate;
	}

	public String getNextreminder() {
		return nextreminder;
	}

	public void setNextreminder(String nextreminder) {
		this.nextreminder = nextreminder;
	}

	public BigDecimal getHeight() {
		return height;
	}

	public void setHeight(BigDecimal height) {
		this.height = height;
	}

	public BigDecimal getWeight() {
		return weight;
	}

	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}

	public BigDecimal getHc() {
		return hc;
	}

	public void setHc(BigDecimal hc) {
		this.hc = hc;
	}

	public byte[] getBabyrecordfiles() {
		return babyrecordfiles;
	}

	public void setBabyrecordfiles(byte[] babyrecordfiles) {
		this.babyrecordfiles = babyrecordfiles;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

	public Clinic getClinic() {
		return clinic;
	}

	public void setClinic(Clinic clinic) {
		this.clinic = clinic;
	}

}
