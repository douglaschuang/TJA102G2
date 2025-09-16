package com.babymate.forum.model;

import java.io.Serializable;
import java.sql.Timestamp;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "replies")
public class ReplyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer replyId;
    private PostVO postVO;       // 多對一：回覆屬於文章
    private MemberVO memberVO;   // 多對一：回覆屬於會員
    private String replyLine;
    private Timestamp replyTime;
    private Timestamp replyModify;
    private Byte replyStatus;

    public ReplyVO() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id")
    public Integer getReplyId() {
        return replyId;
    }
    public void setReplyId(Integer replyId) {
        this.replyId = replyId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    public PostVO getPostVO() {
        return postVO;
    }
    public void setPostVO(PostVO postVO) {
        this.postVO = postVO;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    public MemberVO getMemberVO() {
        return memberVO;
    }
    public void setMemberVO(MemberVO memberVO) {
        this.memberVO = memberVO;
    }

    @Lob
    @Column(name = "reply_line", nullable = false)
    public String getReplyLine() {
        return replyLine;
    }
    public void setReplyLine(String replyLine) {
        this.replyLine = replyLine;
    }

    @Column(name = "reply_time", insertable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    public Timestamp getReplyTime() {
        return replyTime;
    }
    public void setReplyTime(Timestamp replyTime) {
        this.replyTime = replyTime;
    }

    @Column(name = "reply_modify")
    public Timestamp getReplyModify() {
        return replyModify;
    }
    public void setReplyModify(Timestamp replyModify) {
        this.replyModify = replyModify;
    }
    
    
    //初始設定為1(顯示)
    @Column(name = "reply_status", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    public Byte getReplyStatus() { return replyStatus; }
    public void setReplyStatus(Byte replyStatus) { this.replyStatus = replyStatus; }
    // ⚡ 這裡重點：更新前自動執行
    @PreUpdate
    protected void onUpdate() {
        this.replyModify = new Timestamp(System.currentTimeMillis());
    }

    
    
    
    
    
    
    
}
