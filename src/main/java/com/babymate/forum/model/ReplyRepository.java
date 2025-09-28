package com.babymate.forum.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyRepository extends JpaRepository<ReplyVO, Integer> {

    /**
     * 根據文章ID，找出所有「可見」的回覆，並按時間升序排列。
     * 這裡使用 LEFT JOIN FETCH 來一次性抓取回覆者的資訊，避免 LazyInitializationException。
     * @param postId 文章ID
     * @param status 回覆狀態 (例如 1 代表可見)
     * @return 該文章的回覆列表
     */
    @Query("SELECT r FROM ReplyVO r LEFT JOIN FETCH r.memberVO WHERE r.postVO.postId = :postId AND r.replyStatus = :status ORDER BY r.replyTime ASC")
    List<ReplyVO> findVisibleRepliesByPostId(Integer postId, Byte status);

}