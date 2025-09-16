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
@Table(name = "reports")
public class ReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer reportId;
    private PostVO postVO;
    private MemberVO memberVO;
    private Timestamp reportTime;
    private Byte reportReason; // 例如 1: 廣告, 2: 騷擾, 3: 不當內容
    private Byte reportStatus = 1; // 1: 未處理, 0: 已處理 / 無效
    private Timestamp reportCloseTime;

    public ReportVO() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    public Integer getReportId() { return reportId; }
    public void setReportId(Integer reportId) { this.reportId = reportId; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    public PostVO getPostVO() { return postVO; }
    public void setPostVO(PostVO postVO) { this.postVO = postVO; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    public MemberVO getMemberVO() { return memberVO; }
    public void setMemberVO(MemberVO memberVO) { this.memberVO = memberVO; }

    @Column(name = "report_time", insertable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    public Timestamp getReportTime() { return reportTime; }
    public void setReportTime(Timestamp reportTime) { this.reportTime = reportTime; }

    @Column(name = "report_reason", nullable = false)
    public Byte getReportReason() { return reportReason; }
    public void setReportReason(Byte reportReason) { this.reportReason = reportReason; }

    @Column(name = "report_status", nullable = false)
    public Byte getReportStatus() { return reportStatus; }
    public void setReportStatus(Byte reportStatus) { this.reportStatus = reportStatus; }
    
    @Column(name = "report_close_time")
    public Timestamp getReportCloseTime() { return reportCloseTime; }
    public void setReportCloseTime(Timestamp reportCloseTime) { this.reportCloseTime = reportCloseTime; }
}
