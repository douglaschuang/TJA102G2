package com.babymate.permission.model;

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

@Entity
@Table(name = "permission")
public class PermissionVO implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	@Override
	public String toString() {
		return "PermissionVO [permisisonId=" + permisisonId + ", name=" + name + ", url=" + url + ", menuGroupVO="
				+ menuGroupVO + ", icon=" + icon + ", type=" + type + "]";
	}

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
	private Integer permisisonId; // 權限ID
	
	@Column(name = "name")
	@NotEmpty(message="權限名稱: 請勿空白")
	private String name; // 權限名稱
	
	@Column(name = "url")
	@NotEmpty(message="URL路徑: 請勿空白")
	private String url; // URL路徑
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "group_id", nullable = false)
	private MenuGroupVO menuGroupVO; // 群組ID
	
	@Column(name = "icon")
	private String icon;  // 按鈕類型
	
	@Column(name = "type")
	private Integer type; // 選單顯示類型
	
	@Column(name = "service")
	private String service; // 連結服務名稱
	
	public Integer getPermisisonId() {
		return permisisonId;
	}

	public void setPermisisonId(Integer permisisonId) {
		this.permisisonId = permisisonId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public MenuGroupVO getMenuGroupVO() {
		return menuGroupVO;
	}

	public void setMenuGroupVO(MenuGroupVO menuGroupVO) {
		this.menuGroupVO = menuGroupVO;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

}
