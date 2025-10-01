package com.babymate.forum.model;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<LikeVO, Integer> {

    // ★★★ 這就是我們在 Service 裡呼叫的新方法 ★★★
    // 這個 JPQL 查詢會非常有效率地找出：
    // 在給定的文章ID列表(postIds)中，有哪些是某個會員(memberId)按過讚的。
    @Query("SELECT l.postVO.postId FROM LikeVO l WHERE l.memberVO.memberId = :memberId AND l.postVO.postId IN :postIds AND l.likeStatus = 1")
    Set<Integer> findLikedPostIdsByMemberAndPosts(@Param("memberId") Integer memberId, @Param("postIds") List<Integer> postIds);

    
    
    Optional<LikeVO> findByMemberVO_MemberIdAndPostVO_PostId(Integer memberId, Integer postId);
    
    // 根據會員ID和收藏狀態(1)來查找，並回傳分頁結果
    @Query(value = "SELECT l FROM LikeVO l " +
            "JOIN FETCH l.postVO p " +
            "JOIN FETCH p.memberVO " +
            "JOIN FETCH p.boardVO " +
            "WHERE l.memberVO.memberId = :memberId AND l.likeStatus = :status",
    countQuery = "SELECT count(l) FROM LikeVO l WHERE l.memberVO.memberId = :memberId AND l.likeStatus = :status")
Page<LikeVO> findActiveLikesByMemberIdWithDetails(
 @Param("memberId") Integer memberId, 
 @Param("status") byte status, 
 Pageable pageable
);
}
