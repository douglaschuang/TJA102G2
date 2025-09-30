package com.babymate.forum.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.babymate.forum.model.PostVO;
import com.babymate.forum.model.ReplyRepository;
import com.babymate.forum.model.ReplyVO;
import com.babymate.member.model.MemberVO;

@Service
@Transactional
public class ReplyService {

    @Autowired
    private ReplyRepository replyRepository;

    /**
     * 根據文章ID，獲取所有可見的回覆列表
     */
    public List<ReplyVO> findRepliesByPostId(Integer postId) {
        return replyRepository.findVisibleRepliesByPostId(postId, (byte) 1);
    }

    /**
     * 儲存一筆新的回覆
     * @param postId 該回覆所屬的文章ID
     * @param memberId 發表回覆的會員ID
     * @param content 回覆的內容
     */
    public void addReply(Integer postId, MemberVO member, String content) { // <-- 參數從 Integer memberId 改成 MemberVO member
        ReplyVO newReply = new ReplyVO();
        
        newReply.setReplyLine(content);
        
        // 設置關聯的文章
        PostVO post = new PostVO();
        post.setPostId(postId);
        newReply.setPostVO(post);

        // 【關鍵改動】
        // 直接使用從 Controller 傳遞過來的、完整的 MemberVO 物件
        newReply.setMemberVO(member);

        // 4. 【【【 關鍵修正！設定時間和其他預設值 】】】
        // 1. 先用新的 API 取得當前精確時間 (這仍然是好習慣)
        LocalDateTime now = LocalDateTime.now();

        // 2. 把 LocalDateTime 物件「轉換成」資料庫看得懂的 Timestamp 物件
        Timestamp timestampNow = Timestamp.valueOf(now);

        // 3. 用轉換後的 Timestamp 物件來設定你的 VO
        newReply.setReplyTime(timestampNow);     // 設定建立時間
        newReply.setReplyModify(timestampNow);   // 設定修改時間

        newReply.setReplyStatus((byte) 1); // 假設 1 代表「顯示中」

        replyRepository.save(newReply);
    }
}