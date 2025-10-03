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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "likes")
public class LikeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer likeId;
    private MemberVO memberVO;
    private PostVO postVO;
    private Timestamp likeTime;
    private Byte likeStatus = 1; // 1: 有效, 0: 已取消

    public LikeVO() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    public Integer getLikeId() { return likeId; }
    public void setLikeId(Integer likeId) { this.likeId = likeId; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    public MemberVO getMemberVO() { return memberVO; }
    public void setMemberVO(MemberVO memberVO) { this.memberVO = memberVO; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    public PostVO getPostVO() { return postVO; }
    public void setPostVO(PostVO postVO) { this.postVO = postVO; }

    @Column(name = "like_time", insertable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    public Timestamp getLikeTime() { return likeTime; }
    public void setLikeTime(Timestamp likeTime) { this.likeTime = likeTime; }

    @Column(name = "like_status", nullable = false)
    public Byte getLikeStatus() { return likeStatus; }
    public void setLikeStatus(Byte likeStatus) { this.likeStatus = likeStatus; }
}