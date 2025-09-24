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
	    
	
	    @Query(value = "SELECT * FROM post " +
	            "WHERE LOWER(post_title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
	            "OR LOWER(CAST(post_line AS CHAR)) LIKE LOWER(CONCAT('%', :keyword, '%'))",
	    nativeQuery = true)
	List<PostVO> searchPosts(@Param("keyword") String keyword);
	
	    
	    
	    
	    
	    // ✅ 分頁 + fetch join，避免 LazyInitializationException
	    @Query(
	            value = "SELECT p FROM PostVO p " +
	                    "JOIN FETCH p.boardVO b " +
	                    "JOIN FETCH p.memberVO m " +
	                    "WHERE p.postStatus = 1",
	            countQuery = "SELECT COUNT(p) FROM PostVO p WHERE p.postStatus = 1"
	        )
	        Page<PostVO> findAllActiveWithBoardAndMember(Pageable pageable);
	    
	    
	    
	    
	    	
	    
	    
	    @Query(
	    	    value = "SELECT p FROM PostVO p " +
	    	            "JOIN FETCH p.boardVO b " +
	    	            "JOIN FETCH p.memberVO m " +
	    	            "WHERE p.postStatus = 1 AND b.boardId = :boardId",
	    	    countQuery = "SELECT COUNT(p) FROM PostVO p WHERE p.postStatus = 1 AND p.boardVO.boardId = :boardId"
	    	)
	    	Page<PostVO> findAllActiveByBoardId(@Param("boardId") Integer boardId, Pageable pageable);

	    
	    
	    
	    
	    }
	
	
	
	
	
	
	
	
	
	    
	    
	