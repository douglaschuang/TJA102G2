package com.babymate.forum.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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
    

    
}
