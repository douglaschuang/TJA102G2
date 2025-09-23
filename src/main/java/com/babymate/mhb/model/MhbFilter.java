package com.babymate.mhb.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class MhbFilter {
    private Integer id;
    private Integer memberId;
    private String  name;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthdayFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthdayTo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate lmpFrom;   // lastMcDate from
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate lmpTo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate eddFrom;   // expectedBirthDate from
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate eddTo;

    private BigDecimal wMin;
    private BigDecimal wMax;

    private Boolean hasPhoto;     // null = 不限；true=有圖；false=無圖

    public boolean isEmpty() {
        return id==null && memberId==null && (name==null || name.isBlank())
            && birthdayFrom==null && birthdayTo==null
            && lmpFrom==null && lmpTo==null
            && eddFrom==null && eddTo==null
            && wMin==null && wMax==null
            && hasPhoto==null;
    }
    
    // getters/setters
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getMemberId() {
		return memberId;
	}

	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDate getBirthdayFrom() {
		return birthdayFrom;
	}

	public void setBirthdayFrom(LocalDate birthdayFrom) {
		this.birthdayFrom = birthdayFrom;
	}

	public LocalDate getBirthdayTo() {
		return birthdayTo;
	}

	public void setBirthdayTo(LocalDate birthdayTo) {
		this.birthdayTo = birthdayTo;
	}

	public LocalDate getLmpFrom() {
		return lmpFrom;
	}

	public void setLmpFrom(LocalDate lmpFrom) {
		this.lmpFrom = lmpFrom;
	}

	public LocalDate getLmpTo() {
		return lmpTo;
	}

	public void setLmpTo(LocalDate lmpTo) {
		this.lmpTo = lmpTo;
	}

	public LocalDate getEddFrom() {
		return eddFrom;
	}

	public void setEddFrom(LocalDate eddFrom) {
		this.eddFrom = eddFrom;
	}

	public LocalDate getEddTo() {
		return eddTo;
	}

	public void setEddTo(LocalDate eddTo) {
		this.eddTo = eddTo;
	}

	public BigDecimal getwMin() {
		return wMin;
	}

	public void setwMin(BigDecimal wMin) {
		this.wMin = wMin;
	}

	public BigDecimal getwMax() {
		return wMax;
	}

	public void setwMax(BigDecimal wMax) {
		this.wMax = wMax;
	}

	public Boolean getHasPhoto() {
		return hasPhoto;
	}

	public void setHasPhoto(Boolean hasPhoto) {
		this.hasPhoto = hasPhoto;
	}    
}
