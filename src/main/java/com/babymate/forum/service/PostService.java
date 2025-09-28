package com.babymate.forum.service;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.babymate.forum.model.PostRepository;
import com.babymate.forum.model.PostVO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;





@Service("postService")
public class PostService {


	@Autowired
	PostRepository postRepository;
	
	@Autowired
    private SessionFactory sessionFactory;
	
    @PersistenceContext
    private EntityManager entityManager;

    public List<PostVO> getAllWithMemberAndBoard() {
        String jpql = "SELECT p FROM PostVO p " +
                      "LEFT JOIN FETCH p.memberVO " +
                      "LEFT JOIN FETCH p.boardVO " +
                      "ORDER BY p.postId DESC";
        return entityManager.createQuery(jpql, PostVO.class).getResultList();
    }
    //拿到分頁後的可見文章
    public Page<PostVO> findAllVisiblePostsPaged(Pageable pageable) {
        return postRepository.findAllVisiblePosts(pageable);
    }
    
    
    
    
    public void createPost(PostVO post) {
        post.setPostStatus((byte)1);           // 預設顯示
        post.setPostTime(new Timestamp(System.currentTimeMillis())); // 自動時間
        postRepository.save(post);
    }

    public void updatePost(Integer id, PostVO post) {
        PostVO existing = postRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("文章不存在 id=" + id));

        existing.setPostTitle(post.getPostTitle());
        existing.setPostLine(post.getPostLine());
        
        // ⚡ 更新板塊
        existing.setBoardVO(post.getBoardVO());

        postRepository.save(existing);
    }
    
    
    
    
    
    public void softDeletePost(Integer postId) {
    	System.out.println("Deleting postId = " + postId);
        postRepository.findById(postId).ifPresent(p -> {
            p.setPostStatus((byte)0);
            postRepository.save(p);
        });
    }
    
    
    public void togglePostStatus(Integer postId) {
        postRepository.findById(postId).ifPresent(post -> {
            Byte newStatus = (post.getPostStatus() == 1) ? (byte)0 : (byte)1;
            post.setPostStatus(newStatus);
            postRepository.save(post); // ⚡直接 save 就行
        });
    }
    
    
    
    
    
//    public PostVO getOnePost(Integer postId) {
//        return postRepository.findById(postId).orElse(null);
//    }
//  
    
//取代上面的抓單篇文章的舊方法
    public PostVO getOnePost(Integer postId) {
        // 使用 JPQL 查詢，並透過 JOIN FETCH 一次性抓取關聯的 boardVO 和 memberVO
        String jpql = "SELECT p FROM PostVO p "
                    + "LEFT JOIN FETCH p.boardVO "
                    + "LEFT JOIN FETCH p.memberVO "
                    + "WHERE p.postId = :postId";
        try {
            return entityManager.createQuery(jpql, PostVO.class)
                                .setParameter("postId", postId)
                                .getSingleResult();
        } catch (Exception e) {
            // 如果找不到文章，可以回傳 null 或拋出更明確的例外
            return null;
        }
    }
    
 // 新增方法：抓一篇文章並立即初始化 boardVO
    public PostVO getOnePostWithBoard(Integer postId) {
        String jpql = "SELECT p FROM PostVO p "
                    + "LEFT JOIN FETCH p.boardVO "
                    + "WHERE p.postId = :id";
        return entityManager.createQuery(jpql, PostVO.class)
                            .setParameter("id", postId)
                            .getSingleResult();
    }
    
    
    
    

    public List<PostVO> getAll() {
        return postRepository.findAllActive();
    }

	
	
	
    public List<PostVO> findRecentPosts(int limit) {
        List<PostVO> allPosts = postRepository.findRecentPosts();
        if (allPosts.size() > limit) {
            return allPosts.subList(0, limit);
        }
        return allPosts;
    }

}
