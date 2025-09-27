package com.babymate.forum.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// BoardVO 是你的 Entity
@Repository
public interface BoardRepository extends JpaRepository<BoardVO, Integer> {
	// Repository 或 DAO
	@Query("SELECT p FROM PostVO p JOIN FETCH p.boardVO WHERE p.id = :id")
	PostVO findPostWithBoard(@Param("id") Long id);
	
	
	
	
	List<BoardVO> findAllByBoardStatus(Byte boardStatus);

	
}
