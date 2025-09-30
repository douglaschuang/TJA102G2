package com.babymate.forum.model;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PostRepository extends JpaRepository<PostVO, Integer> {

	@Transactional
	@Modifying
    // 撈出還有效的貼文（status = 1）
	@Query("SELECT p FROM PostVO p JOIN FETCH p.boardVO WHERE p.postStatus = 1")
    List<PostVO> findAllActive();

    // 如果還要查某個版塊有效貼文
	@Query("SELECT p FROM PostVO p WHERE p.postStatus = 1 AND p.boardVO.boardId = ?1")
	List<PostVO> findActiveByBoardId(Integer boardId);
	
    @Query("SELECT p FROM PostVO p WHERE p.postStatus = 1 ORDER BY p.postTime DESC")
    List<PostVO> findRecentPosts(); // 查詢最新文章
    //撈出有效貼文並排頁數
    @Query(value = "SELECT p FROM PostVO p LEFT JOIN FETCH p.memberVO LEFT JOIN FETCH p.boardVO WHERE p.postStatus = 1",
            countQuery = "SELECT count(p) FROM PostVO p WHERE p.postStatus = 1")
     Page<PostVO> findAllVisiblePosts(Pageable pageable);

    /**
     * 根據 boardId 和 postStatus 查詢文章，並預先載入會員資訊 (MemberVO)，支援分頁。
     */
    @Query(value = "SELECT p FROM PostVO p JOIN FETCH p.memberVO JOIN FETCH p.boardVO WHERE p.boardVO.boardId = :boardId AND p.postStatus = :postStatus",
            countQuery = "SELECT count(p) FROM PostVO p WHERE p.boardVO.boardId = :boardId AND p.postStatus = :postStatus")
    Page<PostVO> findPostsWithMemberByBoardId(
        @Param("boardId") Integer boardId,
        @Param("postStatus") byte postStatus,
        Pageable pageable
    );
}

