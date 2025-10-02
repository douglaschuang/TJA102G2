package com.babymate.forum.service;

import java.sql.Timestamp;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.babymate.forum.model.PostVO;
import com.babymate.forum.model.ReportRepository;
import com.babymate.forum.model.ReportVO;
import com.babymate.member.model.MemberVO;
@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Transactional
    public void createReport(Integer postId, Integer memberId, Byte reason) {
        
        // 為了設定關聯，我們先建立只帶有 ID 的 PostVO 和 MemberVO "代理" 物件
        PostVO postProxy = new PostVO();
        postProxy.setPostId(postId);

        MemberVO memberProxy = new MemberVO();
        memberProxy.setMemberId(memberId);

        // ★ 步驟一：檢查是否重複檢舉 (呼叫我們剛定義好的 Repository 方法)
        if (reportRepository.existsByPostVOAndMemberVO(postProxy, memberProxy)) {
            throw new IllegalStateException("您已經檢舉過這篇文章了。");
        }

        // ★ 步驟二：建立新的檢舉單實體
        ReportVO report = new ReportVO();
        report.setPostVO(postProxy); // 設定被檢舉的文章
        report.setMemberVO(memberProxy); // 設定檢舉人
        report.setReportReason(reason); // 設定檢舉理由
        // reportStatus 在 ReportVO 裡已經預設為 1 (未處理)，所以這裡不用再設

        // ★ 步驟三：儲存到資料庫
        reportRepository.save(report);
    }
    
    // ... 未來你可以在這裡加上後台管理需要的方法，例如：
    // ★ 新增方法一：分頁查詢所有檢舉 (讓後台可以一頁一頁看)
    public Page<ReportVO> findAllReports(Pageable pageable) {
        // 這裡未來還可以加上排序，比如總是讓「未處理」的排在最前面
//    	return reportRepository.findAllWithDetails(pageable);
    	 return reportRepository.findAll(pageable);
    }

    // ★ 新增方法二：更新檢舉狀態
    @Transactional
    public void updateReportStatus(Integer reportId, Byte newStatus) {
        ReportVO report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NoSuchElementException("找不到 ID 為 " + reportId + " 的檢舉單"));

        report.setReportStatus(newStatus);
        
        if (newStatus == 0) {
            report.setReportCloseTime(new Timestamp(System.currentTimeMillis()));
        }

        reportRepository.save(report);
    }
}