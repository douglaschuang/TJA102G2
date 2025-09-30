package com.babymate.forum.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
            @RequestParam(name = "page", defaultValue = "1") int page,
            HttpSession session, // ★★★ 2. 把 HttpSession 加到參數裡 ★★★
            Model model) {

        System.out.println("\n\n>>> 鐵證！請求已進入 ForumController 的 forumIndex 方法！ <<<\n\n");

        int pageNumber = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageNumber, 3, Sort.by("postTime").descending());

        // ★★★ 3. 從 session 中取得當前登入的會員物件 ★★★
        //    注意：這裡的 "member" 必須跟你當初登入成功時，存入 session 的 key 一致
        MemberVO currentUser = (MemberVO) session.getAttribute("member");

        // ★★★ 4. 呼叫 Service 方法時，把 currentUser 傳進去 ★★★
        //    就算 currentUser 是 null (未登入)，Service 裡的邏輯也能處理
        Page<PostVO> postPage = postService.findAllVisiblePostsPaged(currentUser, pageable);

        // 後面的邏輯完全一樣
        model.addAttribute("postPage", postPage);

        List<BoardVO> allBoards = boardService.findAllActiveBoards();
        model.addAttribute("boards", allBoards);

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
        PostVO post = postService.getOnePost(postId, memberFromSession);
        model.addAttribute("post", post);
        
        List<BoardVO> allBoards = boardService.findAllActiveBoards();
        model.addAttribute("boards", allBoards);
        
        List<ReplyVO> replyList = replyService.findRepliesByPostId(postId);
        model.addAttribute("replyList", replyList);
        
        model.addAttribute("breadcrumbTitle", post.getPostTitle());
        
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
    
    //找看板用
    @GetMapping("/forum/board/{boardId}")
    public String showBoardPage(
            @PathVariable("boardId") Integer boardId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model,
            HttpSession session) { // <-- 加上 HttpSession

        // --- 準備側邊欄和目前看板的資料 ---
        List<BoardVO> allBoards = boardService.findAllActiveBoards();
        model.addAttribute("boards", allBoards);
        
        BoardVO currentBoard = boardService.findBoardById(boardId).orElse(null);
        if (currentBoard == null) {
            return "redirect:/forum"; 
        }
        model.addAttribute("board", currentBoard);

        // --- 準備這個看板的文章列表 (含分頁) ---
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, 5, Sort.by("postTime").descending());
        
        // 1. 取得當前使用者
        MemberVO currentUser = (MemberVO) session.getAttribute("member");
        
        // 2. 呼叫我們升級後的 Service 方法
        Page<PostVO> postPage = postService.findPostsByBoardIdPaged(boardId, currentUser, pageable);
        
        // 3. 把完整的 Page 物件傳給前端，不再拆解！
        model.addAttribute("postPage", postPage);
        
        model.addAttribute("breadcrumbTitle", currentBoard.getBoardName());

        return "frontend/forum_board"; 
    }
    
    
    //喜歡功能
    @PostMapping("/api/posts/{postId}/toggle-like") // 使用 POST 請求，路徑清楚明瞭
    @ResponseBody // 這個註解很重要！它告訴 Spring Boot 返回的是資料(JSON)，而不是一個網頁
    public ResponseEntity<?> toggleLike(@PathVariable("postId") Integer postId, HttpSession session) {
        
        // 1. 檢查使用者是否登入
        MemberVO currentUser = (MemberVO) session.getAttribute("member");
        if (currentUser == null) {
            // 如果沒登入，返回一個錯誤訊息
            return ResponseEntity.status(401).body(Map.of("error", "請先登入"));
        }
        
        // 2. 呼叫 Service 層去處理核心業務邏輯
        //    這個 toggleLikeStatus 方法我們等一下會在 Service 裡建立
        boolean isLiked = postService.toggleLikeStatus(currentUser.getMemberId(), postId);
        
        // 3. 返回一個成功的 JSON 結果，告訴前端現在的收藏狀態是什麼
        return ResponseEntity.ok(Map.of("liked", isLiked));
    }

    
    
    
    
    
    // 發表新文章表單
    // ★★★ 新增這個方法，用來顯示發文表單頁面 ★★★
 // 在 ForumController.java 裡

    @GetMapping("/forum/board/{boardId}/post/new")
    public String newPostForm(@PathVariable("boardId") Integer boardId, Model model, HttpSession session) {
        
        if (session.getAttribute("member") == null) {
            return "redirect:/shop/login";
        }

       
        // 為了讓側邊欄能顯示，我們需要把所有看板的列表也查出來放進 Model
        List<BoardVO> allBoards = boardService.findAllActiveBoards();
        model.addAttribute("boards", allBoards);
       

        // 後面的程式碼維持不變
        model.addAttribute("postVO", new PostVO());
        BoardVO board = boardService.findBoardById(boardId).orElse(null);
        model.addAttribute("board", board);
        model.addAttribute("breadcrumbTitle", "發表新文章");
        
        return "frontend/forum_new_post";
    }
    // 提交新文章
    // ★★★ 新增這個方法，用來處理表單提交 ★★★
    @PostMapping("/forum/post/create")
    public String createPost(
            @ModelAttribute("postVO") PostVO postVO, 
            @RequestParam("boardId") Integer boardId,
            HttpSession session) {

        // 1. 從 session 取得當前使用者，設定為文章作者
        MemberVO currentUser = (MemberVO) session.getAttribute("member");
        if (currentUser == null) {
            return "redirect:/shop/login"; // 再次驗證登入狀態
        }
        postVO.setMemberVO(currentUser);
        
        // 2. 設定文章所屬的看板
        BoardVO board = new BoardVO();
        board.setBoardId(boardId);
        postVO.setBoardVO(board);
        
        // 3. 呼叫 Service 儲存文章
        postService.createPost(postVO);
        
        // 4. 成功後，重導向回該看板的列表頁
        return "redirect:/forum/board/" + boardId;
    }




    
    
    
    
    
    @GetMapping("/forum/post/edit/{postId}")
    public String showEditPostForm(@PathVariable("postId") Integer postId, Model model, HttpSession session) {
        
        // 1. 權限檢查：看看是不是本人，或是管理員
        MemberVO currentUser = (MemberVO) session.getAttribute("member");
        if (currentUser == null) {
            return "redirect:/shop/login"; // 沒登入，請他去登入
        }
        
        // 2. 從資料庫把舊文章撈出來
        PostVO postToEdit = postService.getOnePost(postId); 
        
        // 3. 再次權限檢查：確保這篇文章的作者就是現在登入的這位
        //    (如果未來有管理員，這裡的邏輯要再擴充)
        if (postToEdit == null || !postToEdit.getMemberVO().getMemberId().equals(currentUser.getMemberId())) {
            // 如果文章不存在，或者作者對不上，就踢回論壇首頁，不給亂改
            return "redirect:/forum";
        }

        // 4. 準備頁面需要的資料
        //    - 把要編輯的文章物件放進去，讓 th:object 能接到
        model.addAttribute("postVO", postToEdit); 
        //    - 為了讓側邊欄能正常顯示，所有看板的列表還是得查一次
        model.addAttribute("boards", boardService.findAllActiveBoards());
        //    - 麵包屑導覽的標題
        model.addAttribute("breadcrumbTitle", "編輯文章");

        // 5. 指向咱們的編輯頁面
        return "frontend/forum_edit_post";
    }


    /**
     * 處理「提交編輯後文章」的請求
     * @param postVO Spring MVC 自動從表單打包好的文章物件 (裡面會有標題、內容、還有隱藏的 postId)
     * @param session 用來取得當前使用者
     * @return 重導向的路徑
     */
    @PostMapping("/forum/post/update")
    public String updatePost(@ModelAttribute("postVO") PostVO postVO, HttpSession session) {
        
        // 1. 再次驗證權限
        MemberVO currentUser = (MemberVO) session.getAttribute("member");
        if (currentUser == null) {
            return "redirect:/shop/login";
        }

        // 2. 呼叫 Service 層執行更新，把打包好的 postVO 和當前使用者傳進去
        //    Service 裡面會再做一次權限比對，雙重保險
        try {
            PostVO updatedPost = postService.updatePostByUser(postVO, currentUser);
            
            // 3. 更新成功後，跳轉回文章的詳細頁面
            Integer boardId = updatedPost.getBoardVO().getBoardId();
            Integer postId = updatedPost.getPostId();
            return "redirect:/forum/board/" + boardId + "/post/" + postId;

        } catch (Exception e) {
            // 如果 Service 層在驗證權限時拋出例外
            System.err.println("更新文章失敗: " + e.getMessage());
            return "redirect:/forum"; // 踢回首頁
        }
    }

    // ==================== 編輯文章 END ====================
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
 // 軟刪除文章22222222
    @PostMapping("/forum/post/delete/{postId}")
    public String deletePost(@PathVariable Integer postId, HttpSession session) {
        // 1. 檢查使用者是否登入
        MemberVO currentUser = (MemberVO) session.getAttribute("member");
        if (currentUser == null) {
            return "redirect:/shop/login"; // 沒登入，就請他去登入
        }
        
        // 2. 在刪除前，先取得文章物件，目的是為了拿到 boardId，這樣我們才知道要跳轉回哪個看板
        PostVO post = postService.getOnePost(postId); 
        Integer boardId = (post != null) ? post.getBoardVO().getBoardId() : null;

        try {
            // 3. 呼叫 Service 層執行刪除，並傳入文章ID和當前使用者的ID進行權限驗證
            postService.softDeletePost(postId, currentUser.getMemberId());
        } catch (Exception e) {
            // 如果 Service 拋出例外（例如權限不足），可以在這裡處理
            System.err.println("刪除文章時發生錯誤: " + e.getMessage());
        }
        
        // 4. 刪除成功後，重導向回原本的看板頁面
        if (boardId != null) {
            return "redirect:/forum/board/" + boardId;
        } else {
            return "redirect:/forum";
        }
    }

}