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
@Table(name = "followboards")
public class FollowBoardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer followBoardId;
    private MemberVO memberVO;
    private BoardVO boardVO;
    private Timestamp followBoardTime;
    private Byte followBoardStatus;

    public FollowBoardVO() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "follow_board_id")
    public Integer getFollowBoardId() { return followBoardId; }
    public void setFollowBoardId(Integer followId) { this.followBoardId = followId; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    public MemberVO getMemberVO() { return memberVO; }
    public void setMemberVO(MemberVO memberVO) { this.memberVO = memberVO; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    public BoardVO getBoardVO() { return boardVO; }
    public void setBoardVO(BoardVO boardVO) { this.boardVO = boardVO; }

    @Column(name = "follow_board_time", insertable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    public Timestamp getFollowBoardTime() { return followBoardTime; }
    public void setFollowBoardTime(Timestamp followTime) { this.followBoardTime = followTime; }
    
    //初始設定為1(顯示)
    @Column(name = "follow_board_status", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    public Byte getFollowBoardStatus() { return followBoardStatus; }
    public void setFollowBoardStatus(Byte followBoardStatus) { this.followBoardStatus = followBoardStatus; }
    
    
}