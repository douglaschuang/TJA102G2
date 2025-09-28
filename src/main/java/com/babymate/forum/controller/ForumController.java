package com.babymate.forum.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.babymate.forum.model.BoardVO;
import com.babymate.forum.model.PostVO;
import com.babymate.forum.model.ReplyVO;
import com.babymate.forum.service.BoardService;
import com.babymate.forum.service.PostService;
import com.babymate.forum.service.ReplyService;
import com.babymate.member.model.MemberVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("")
public class ForumController {
	
	@Autowired
	private PostService postService;
	
	
	@Autowired
	private BoardService boardService;
	
	@Autowired
	private ReplyService replyService;
	
	
	/*====前台文章首頁==========================================================================================================*/
//    @GetMapping("/forum")
//    public String listAllPosts(Model model) {
//        // 1. 取得主內容區的文章列表 (保留)
//        List<PostVO> postList = postService.getAllWithMemberAndBoard();
//        model.addAttribute("postListData", postList);
//
//        // 2. 取得側邊欄要用的「所有看板列表」(保留)
//        List<BoardVO> allBoards = boardService.findAllActiveBoards(); // 假設你已改用這個方法
//        model.addAttribute("boards", allBoards);
//        
//        // 3. 【拿掉這裡】不再需要準備 favoriteBoardList
//        // List<BoardVO> favoriteBoards = allBoards.stream().limit(4).collect(Collectors.toList());
//        // model.addAttribute("favoriteBoardList", favoriteBoards);
//        
//        return "frontend/forum";
//    }
    @GetMapping("/forum")
    public String forumIndex(
            // 接收 URL 傳來的 page 參數，如果沒有則預設為 1
            @RequestParam(name = "page", defaultValue = "1") int page, 
            Model model) {
    	
    	
        // ▼▼▼ 在方法的最開頭加上這個監視器 ▼▼▼
        System.out.println("\n\n>>> 鐵證！請求已進入 ForumController 的 forumIndex 方法！ <<<\n\n");
        
        // Spring 的頁碼是從 0 開始算的，所以我們要 -1
        int pageNumber = page - 1; 
        
        // 定義每頁顯示幾筆資料，以及排序方式 (例如依 postTime 降冪)
        Pageable pageable = PageRequest.of(pageNumber, 3, Sort.by("postTime").descending());
        
        // 呼叫新的 Service 方法，取得 Page 物件
        Page<PostVO> postPage = postService.findAllVisiblePostsPaged(pageable);
        
        // --- 把 Page 物件裡的資訊，一個個放進 Model 給前端用 ---
        
        // 1. 當頁的文章列表
        model.addAttribute("postListData", postPage.getContent());
        
        // 2. 總頁數
        model.addAttribute("totalPages", postPage.getTotalPages());
        
        // 3. 目前頁碼 (傳回給前端的是從 1 開始的頁碼)
        model.addAttribute("currentPage", page);
        
        // --- 側邊欄的資料維持不變 ---
        List<BoardVO> allBoards = boardService.findAllActiveBoards();
        model.addAttribute("boards", allBoards);
        model.addAttribute("pageTitle", "論壇首頁");
        return "frontend/forum";
    }
	

    @GetMapping("/forum/board/{boardId}/post/{postId}")
    public String showPostDetail(
            @PathVariable("boardId") Integer boardId,
            @PathVariable("postId") Integer postId,
            Model model,
            HttpSession session) {
        
        // ▼▼▼ 就是改這一行！把 "loggedInMember" 改成 "member" ▼▼▼
        MemberVO memberFromSession = (MemberVO) session.getAttribute("member");
        
        // 為了讓你自己的前端模板和邏輯保持一致，我們依然用 "loggedInMember" 這個名字放進 model
        model.addAttribute("loggedInMember", memberFromSession);

        // --- 後面的業務邏輯維持不變 ---
        PostVO post = postService.getOnePost(postId);
        model.addAttribute("post", post);
        
        List<BoardVO> allBoards = boardService.findAllActiveBoards();
        model.addAttribute("boards", allBoards);
        
        List<ReplyVO> replyList = replyService.findRepliesByPostId(postId);
        model.addAttribute("replyList", replyList);
        
        return "frontend/forum_post";
    }
    
    @PostMapping("/forum/post/{postId}/reply")
    public String addReply(
            @PathVariable("postId") Integer postId,
            @RequestParam("replyLine") String replyContent,
            @RequestParam("boardId") Integer boardId,
            HttpSession session) {
        
        // ▼▼▼ 同樣改這一行！把 "loggedInMember" 改成 "member" ▼▼"
        MemberVO memberWhoReplied = (MemberVO) session.getAttribute("member");

        if (memberWhoReplied == null) {
            // 如果沒登入，理論上前端會擋掉，但後端還是要再驗證一次
            return "redirect:/shop/login"; 
        }
        
        replyService.addReply(postId, memberWhoReplied, replyContent);
        
        return "redirect:/forum/board/" + boardId + "/post/" + postId;
    }
    
    
    
    
    
    
    
    
    
    
    
    // 發表新文章表單
    @GetMapping("/post/new")
    public String newPostForm(Model model) {
        model.addAttribute("post", new PostVO());
        return "forum/post_form";
    }

    // 提交新文章
    @PostMapping("/post/new")
    public String createPost(@ModelAttribute PostVO post) {
        postService.createPost(post);  // 交給 service 處理細節
        return "redirect:/forum";
    }

    // 編輯文章表單
    @GetMapping("/post/edit/{id}")
    public String editPostForm(@PathVariable Integer id, Model model) {
        model.addAttribute("post", postService.getOnePost(id));
        return "forum/post_form";
    }
    // 提交編輯文章
    @PostMapping("/post/edit/{id}")
    public String updatePost(@PathVariable Integer id, @ModelAttribute PostVO post) {
        postService.updatePost(id, post);  // 交給 service
        return "redirect:/forum/post/" + id;
    }
 // 軟刪除文章22222222
    @GetMapping("/post/delete/{id}")
    public String deletePost(@PathVariable Integer id) {
        postService.softDeletePost(id);  // 交給 service
        return "redirect:/forum";
    }

}