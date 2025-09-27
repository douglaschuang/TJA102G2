package com.babymate.babyhandbook.model;

import java.sql.Timestamp;
import java.util.Date;

import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.babymate.member.model.MemberVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "baby_handbook" )
public class BabyhandbookVO implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "baby_handbook_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer babyhandbookid;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "member_id")
	@NotNull(message = "會員ID不得為空")
	private MemberVO member;
	
	@Column(name = "baby_name")
	@NotEmpty(message="小孩姓名:請勿空白")
	@Pattern(regexp = "^[(\u4e00-\u9fa5)(a-zA-Z0-9_)]{2,10}$", message = "小孩姓名: 只能是中、英文字母、數字和_ , 且長度必需在2到10之間")
	private String babyname;
	
	@Column(name = "baby_gender")
	@NotEmpty(message="請選擇小孩性別")
	@Pattern(regexp = "男|女", message = "性別只能是男或女")
	private String babygender;
	
	@Column(name = "baby_birthday")
	@NotNull(message="請選擇小孩生日")
	@Past(message="日期必須是在今日(含)之前")
	@DateTimeFormat(pattern="yyyy-MM-dd") 
	private Date babybirthday;
	
	@Column(name = "baby_handbook_files")
	private byte[] babyhandbookfiles;
	
	@Column(name = "update_time")
	@UpdateTimestamp
	private Timestamp updatetime;
	
	//軟刪除欄位
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;


	public BabyhandbookVO() {
		this.member = new MemberVO();
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public Integer getBabyhandbookid() {
		return babyhandbookid;
	}
	
	public void setBabyhandbookid(Integer babyhandbookid) {
		this.babyhandbookid = babyhandbookid;
	}
	
	public MemberVO getMember() {
	    return member;
	}

	public void setMember(MemberVO member) {
	    this.member = member;
	}
	
	public String getBabyname() {
		return babyname;
	}
	
	public void setBabyname(String babyname) {
		this.babyname = babyname;
	}

	public String getBabygender() {
		return babygender;
	}
	public void setBabygender(String babygender) {
		this.babygender = babygender;
	}
	
	public Date getBabybirthday() {
		return babybirthday;
	}
	public void setBabybirthday(Date babybirthday) {
		this.babybirthday = babybirthday;
	}
	
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	
	public byte[] getBabyhandbookfiles() {
		return babyhandbookfiles;
	}
	public void setBabyhandbookfiles(byte[] babyhandbookfiles) {
		this.babyhandbookfiles = babyhandbookfiles;
	}
	
}
