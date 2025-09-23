package com.babymate.staff.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import com.babymate.role.model.RoleVO;
import com.babymate.valid.CreateGroup;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "staff")
public class StaffVO implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		return "StaffVO [staffId=" + staffId + ", account=" + account + ", "
				+ "nickname=" + nickname + ", role=" + roleVO.getName() + ", "
				+ "status=" + status + ", password=" + password + ", "
				+ "updateDate=" + updateDate + "]";
	}
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
	private Integer staffId; // 員工ID
	
	@Column(name = "account")
	@NotEmpty(message="員工帳號: 請勿空白")
	@Pattern(regexp = "^[(a-zA-Z0-9)]{3,10}$", message = "員工帳號: 只能是英文字母、數字, 且長度必需在2到20之間")
	private String account; // 登入帳號
	
	@Column(name = "nickname")
	@NotEmpty(message="員工暱稱: 請勿空白")
	private String nickname; // 暱稱
	
//	@Column(name = "role")
//	@NotEmpty(message="員工角色: 請勿空白")
//	private String role; // 角色
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "role_id", nullable = false)
	private RoleVO roleVO;
	
	@Column(name = "status")
	private Integer status; // 狀態 0: 停用, 1: 啟用',
	
	@Column(name = "password")
	@NotEmpty(message="密碼: 請勿空白", groups = CreateGroup.class)
	private String password; // 密碼
	
	@Lob
	@Column(name = "pic")
    private byte[] pic; // 頭像
    
    @Column(name = "update_date")
    private LocalDateTime updateDate; // 更新時間
    
    // 預設建構子
    public StaffVO() {}
    
	public Integer getStaffId() {
		return staffId;
	}
	public void setStaffId(Integer staffId) {
		this.staffId = staffId;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
//	public String getRole() {
//		return role;
//	}
	public RoleVO getRoleVO() {
		return this.roleVO;
	}
//	public void setRole(String role) {
//		this.role = role;
//	}
	public void setRoleVO(RoleVO roleVO) {
		this.roleVO = roleVO;
	}
	
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public byte[] getPic() {
		return pic;
	}
	public void setPic(byte[] pic) {
		this.pic = pic;
	}
	public LocalDateTime getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}
	
	@Transient
	public String getBase64Pic() {
        if (pic != null) {
            return Base64.getEncoder().encodeToString(pic);  // 將 byte[] 轉換為 Base64 字串
        }
        return null;
    }
	
	@Transient
	public String getFormattedUpdateDate() {
	    if (updateDate == null) return "";
	    return updateDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

}


