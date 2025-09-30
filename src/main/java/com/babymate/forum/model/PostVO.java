package com.babymate.forum.model;

import java.sql.Timestamp;

import org.jsoup.Jsoup;

import com.babymate.member.model.MemberVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
//import org.hibernate.validator.constraints.NotEmpty;
import jakarta.validation.constraints.NotEmpty;

/*
 * 註1: classpath必須有javax.persistence-api-x.x.jar 
 * 註2: Annotation可以添加在屬性上，也可以添加在getXxx()方法之上
 */


@Entity  //要加上@Entity才能成為JPA的一個Entity類別
@Table(name = "posts") //代表這個class是對應到資料庫的實體table，目前對應的table是EMP2 
public class PostVO implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	
	

    private Integer postId;
    private BoardVO boardVO;   // 多對一：文章屬於一個看板
    private MemberVO memberVO; // 多對一：文章屬於一個會員
    private String postTitle;
    private String postLine;
    private Timestamp postTime;
    private Timestamp postModify;
    private Byte postStatus;
    
    
    private boolean likedByCurrentUser = false;
    
    

	public PostVO() { //必需有一個不傳參數建構子(JavaBean基本知識)
	}

	@Id //@Id代表這個屬性是這個Entity的唯一識別屬性，並且對映到Table的主鍵 
	@Column(name = "post_id")  //@Column指這個屬性是對應到資料庫Table的哪一個欄位   //【非必要，但當欄位名稱與屬性名稱不同時則一定要用】
	@GeneratedValue(strategy = GenerationType.IDENTITY) //@GeneratedValue的generator屬性指定要用哪個generator //【strategy的GenerationType, 有四種值: AUTO, IDENTITY, SEQUENCE, TABLE】 
	public Integer getPostId() {
		return this.postId;
	}

	public void setPostId(Integer postId) {
		this.postId = postId;
	}
	@Transient
    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }
    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }
	
	
	
	
	

    // 關聯到 BoardVO
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    public BoardVO getBoardVO() {
        return boardVO;
    }
    public void setBoardVO(BoardVO boardVO) {
        this.boardVO = boardVO;
    }

    // 關聯到 MemberVO
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    public MemberVO getMemberVO() {
        return memberVO;
    }
    public void setMemberVO(MemberVO memberVO) {
        this.memberVO = memberVO;
    }
    
    @Column(name = "post_title", length = 50, nullable = false)
    @NotEmpty(message="標題欄位: 請勿空白")
    public String getPostTitle() {
    	return postTitle;
    }
    public void setPostTitle(String postTitle) {
    	this.postTitle = postTitle;
    }
	
	
    @Lob //大資料
    @Column(name = "post_line", nullable = false)
    public String getPostLine() { return postLine; }
    public void setPostLine(String postLine) { this.postLine = postLine; }
    
    
    
    // ✅ 新增 transient 屬性
    @Transient
    public String getSummary() {
        if (postLine == null) return "";
        String text = Jsoup.parse(postLine).text(); // 去掉 HTML
        int maxLength = 10; // 你想要抓的字數
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    
    
    
    
    
	
    @Column(name = "post_time", insertable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    public Timestamp getPostTime() { return postTime; }
    public void setPostTime(Timestamp postTime) { this.postTime = postTime; }


    @Column(name = "post_modify")
    public Timestamp getPostModify() { return postModify; }
    public void setPostModify(Timestamp postModify) { this.postModify = postModify; }
    //初始設定為1(顯示)
    @Column(name = "post_status", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    public Byte getPostStatus() { return postStatus; }
    public void setPostStatus(Byte postStatus) { this.postStatus = postStatus; }
    // ⚡ 這裡重點：更新前自動執行
    
    // ★★★ 在原本的 @PreUpdate 上方，多加一個 @PrePersist ★★★
    @PrePersist // 在「新增」資料前執行
    protected void onCreate() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        this.postTime = now;
        this.postModify = now; // 新增時，建立時間 = 修改時間
    } 
    
    
    
    @PreUpdate
    protected void onUpdate() {
        this.postModify = new Timestamp(System.currentTimeMillis());
    }
    @Transient
    public boolean isDeleted() {
        return this.postStatus == 0;
    }

	
}
