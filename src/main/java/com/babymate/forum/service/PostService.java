package com.babymate.forum.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.babymate.forum.model.LikeRepository;
import com.babymate.forum.model.LikeVO;
import com.babymate.forum.model.PostRepository;
import com.babymate.forum.model.PostVO;
import com.babymate.member.model.MemberVO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;





@Service("postService")
public class PostService {


	@Autowired
	PostRepository postRepository;
	
	@Autowired
    private SessionFactory sessionFactory;
	
	@Autowired
	LikeRepository likeRepository;
	
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
    // ▼▼▼ 6. 這是我們要改造的唯一版本的方法 ▼▼▼
    public Page<PostVO> findAllVisiblePostsPaged(MemberVO currentUser, Pageable pageable) {

        // 步驟 1: 像往常一樣，先從資料庫取得文章分頁結果
    	Page<PostVO> postPage = postRepository.findAllVisiblePosts(pageable);

        // 步驟 2: 如果使用者有登入，且當頁有文章，才需要檢查收藏狀態
        if (currentUser != null && !postPage.isEmpty()) {

            // 步驟 3: 從當頁文章中，收集所有 postId
            List<Integer> postIds = postPage.getContent().stream()
                                            .map(PostVO::getPostId)
                                            .collect(Collectors.toList());

            // 步驟 4: 只用一次查詢，從 likes 表裡找出這位使用者收藏了哪些文章
            //         這個方法我們等一下會在 LikeRepository 裡建立
            Set<Integer> likedPostIds = likeRepository.findLikedPostIdsByMemberAndPosts(currentUser.getMemberId(), postIds);

            // 步驟 5: 遍歷文章列表，如果文章的 ID 在上面查到的 Set 裡，就把狀態設為 true
            for (PostVO post : postPage.getContent()) {
                if (likedPostIds.contains(post.getPostId())) {
                    post.setLikedByCurrentUser(true);
                }
            }
        }

        // 步驟 6: 返回處理過的 Page 物件
        return postPage;
    }
    
    @Transactional
    public boolean toggleLikeStatus(Integer memberId, Integer postId) {
        // 1. 去 likes 表格裡找看看，這個會員是否已經對這篇文章有過紀錄
        Optional<LikeVO> existingLikeOpt = likeRepository.findByMemberVO_MemberIdAndPostVO_PostId(memberId, postId);

        if (existingLikeOpt.isPresent()) {
            // 2. 找到了！代表用戶之前互動過，我們來「更新」狀態
            LikeVO like = existingLikeOpt.get();
            
            if (like.getLikeStatus() == 1) {
                like.setLikeStatus((byte) 0); // 本來是 1 (喜歡)，現在改成 0 (取消喜歡)
            } else {
                like.setLikeStatus((byte) 1); // 本來是 0 (或 null)，現在改成 1 (重新喜歡)
            }
            
            likeRepository.save(like); // 儲存更新後的狀態
            
            return like.getLikeStatus() == 1; // 回傳最新的狀態 (true 代表喜歡, false 代表不喜歡)

        } else {
            // 3. 沒找到！代表是第一次喜歡，我們來「新增」一筆紀錄
            LikeVO newLike = new LikeVO();
            
            MemberVO member = new MemberVO();
            member.setMemberId(memberId);
            newLike.setMemberVO(member);
            
            PostVO post = new PostVO();
            post.setPostId(postId);
            newLike.setPostVO(post);
            
            newLike.setLikeStatus((byte) 1); // 第一次一定是「喜歡」狀態

            likeRepository.save(newLike);
            
            return true; // 返回最新的狀態：已喜歡
        }
    }
    
    
 // 在 PostService.java 裡
    @Transactional
    public PostVO createPost(PostVO postVO) {
        
        String unsafeHtml = postVO.getPostLine();
        if (unsafeHtml != null) {
            // 1. 我們從一個更基礎的白名單開始，它本身不處理圖片
            Safelist safelist = Safelist.basic();
            
            // 2. 我們手動、明確地告訴它，<img> 標籤是安全的
            //    並且追加一些你可能需要的文字格式標籤
            safelist.addTags("img", "p", "br", "u", "strike", "strong", "em", "b", "i");
            
            // 3. 我們告訴它，<img> 標籤可以擁有這些屬性
            safelist.addAttributes("img", "src", "style", "width", "height", "alt");
            
            // 4. 【最關鍵】我們 *不* 呼叫 addProtocols() 方法。
            //    不特別去限制 src 屬性必須符合 http/https協定，Jsoup 預設就會放行相對路徑。
            
            String safeHtml = Jsoup.clean(unsafeHtml, safelist);
            postVO.setPostLine(safeHtml);
        }

        postVO.setPostStatus((byte) 1);
        // 時間的部分由 @PrePersist 自動處理，這裡不需要
        
        return postRepository.save(postVO);
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
    
    
    
    
    @Transactional
    public PostVO updatePostByUser(PostVO formPost, MemberVO currentUser) {
        
        Integer postId = formPost.getPostId();
        PostVO originalPost = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("找不到ID為 " + postId + " 的文章"));

        if (!originalPost.getMemberVO().getMemberId().equals(currentUser.getMemberId())) {
            throw new SecurityException("權限不足，您不能編輯他人文章");
        }

        originalPost.setPostTitle(formPost.getPostTitle());
        
        // --- 同樣在這裡使用我們最終版的白名單邏輯 ---
        String unsafeHtml = formPost.getPostLine();
        if (unsafeHtml != null) {
            Safelist safelist = Safelist.basic();
            safelist.addTags("img", "p", "br", "u", "strike", "strong", "em", "b", "i");
            safelist.addAttributes("img", "src", "style", "width", "height", "alt");
            String safeHtml = Jsoup.clean(unsafeHtml, safelist);
            originalPost.setPostLine(safeHtml);
        }
        
        // 時間的部分由 @PreUpdate 自動處理，這裡不需要
        return postRepository.save(originalPost);
    }
    
    
    public void softDeletePost(Integer postId) {
    	System.out.println("Deleting postId = " + postId);
        postRepository.findById(postId).ifPresent(p -> {
            p.setPostStatus((byte)0);
            postRepository.save(p);
        });
    }
    @Transactional // 確保這個方法在一個交易中執行
    public void softDeletePost(Integer postId, Integer currentMemberId) {
        // 1. 根據 ID 從資料庫找出這篇文章，如果找不到就拋出例外
        PostVO post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("文章不存在，ID: " + postId));

        // 2. 進行權限檢查：確保當前登入的使用者就是這篇文章的作者
        if (!post.getMemberVO().getMemberId().equals(currentMemberId)) {
            // 如果 ID 不匹配，就拋出安全性例外，阻止操作
            throw new SecurityException("你沒有權限刪除這篇文章");
        }
        
        // 3. 執行軟刪除：更新文章狀態（假設 1=顯示, 0=隱藏, 2=已刪除）
        post.setPostStatus((byte) 0); 
        
        // 4. 儲存變更回資料庫
        postRepository.save(post);
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
    
    public PostVO getOnePost(Integer postId, MemberVO currentUser) {
        // 1. 先用你原本的方法抓到文章
        PostVO post = this.getOnePost(postId);
        
        // 2. 如果文章存在，且使用者有登入，就檢查收藏狀態
        if (post != null && currentUser != null) {
            // 用我們之前寫好的 Repository 方法來檢查
            boolean isLiked = likeRepository.findByMemberVO_MemberIdAndPostVO_PostId(currentUser.getMemberId(), postId).isPresent();
            // 3. 把狀態設定回 post 物件裡
            post.setLikedByCurrentUser(isLiked);
        }
        
        return post;
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
   //抓看板用
    public Page<PostVO> findPostsByBoardIdPaged(Integer boardId, MemberVO currentUser, Pageable pageable) {
        byte postStatus = 1; 
        
        // 1. 先從 Repository 取得分頁資料
        Page<PostVO> postPage = postRepository.findPostsWithMemberByBoardId(boardId, postStatus, pageable);
        
        // 2. 這段邏輯跟 findAllVisiblePostsPaged 裡的一模一樣，直接複製過來用
        if (currentUser != null && !postPage.isEmpty()) {
            List<Integer> postIds = postPage.getContent().stream()
                                            .map(PostVO::getPostId)
                                            .collect(Collectors.toList());
                                            
            Set<Integer> likedPostIds = likeRepository.findLikedPostIdsByMemberAndPosts(currentUser.getMemberId(), postIds);

            for (PostVO post : postPage.getContent()) {
                if (likedPostIds.contains(post.getPostId())) {
                    post.setLikedByCurrentUser(true);
                }
            }
        }
        
        return postPage;
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
    
    
    
    @Transactional(readOnly = true)
    public Page<PostVO> findLikedPostsByUser(MemberVO currentUser, Pageable pageable) {
        // 1. 呼叫我們 Repository 的新方法
        Page<LikeVO> pageOfLikes = likeRepository.findActiveLikesByMemberIdWithDetails(currentUser.getMemberId(), (byte) 1, pageable);

        // 2. 直接用 map 把 PostVO 抽出來就行了，因為所有關聯都已經被 JOIN FETCH 載入了
        Page<PostVO> pageOfPosts = pageOfLikes.map(LikeVO::getPostVO);

        // 3. 收藏頁的文章，收藏狀態當然是 true
        pageOfPosts.getContent().forEach(post -> post.setLikedByCurrentUser(true));

        return pageOfPosts;
    }
    
    
    
    
    
    public Page<PostVO> searchPostsByKeyword(String keyword, Pageable pageable) {
        // 直接把任務轉交給 Repository
        return postRepository.searchByTitleOrContent(keyword, pageable);
    }
    

}
