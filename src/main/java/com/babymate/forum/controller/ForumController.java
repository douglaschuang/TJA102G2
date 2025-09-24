package com.babymate.forum.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.babymate.forum.model.BoardRepository;
import com.babymate.forum.model.BoardVO;
import com.babymate.forum.model.PostRepository;
import com.babymate.forum.model.PostService;
import com.babymate.forum.model.PostVO;

@Controller
@RequestMapping("")
public class ForumController {
	
    @Autowired
    private PostService postService;

    @Autowired
    private BoardRepository boardRepository;
    
    
	@Autowired
	private PostRepository postRepository;

	// 顯示單篇文章（包含看板 ID）
	@GetMapping("/forum/board/{boardId}/post/{postId}")
	public String listOnePost(
	        @PathVariable Integer boardId,
	        @PathVariable Integer postId,
	        Model model) {

	    // 先撈文章
	    PostVO post = postService.findOneOptional(postId)
	            .orElseThrow(() -> new RuntimeException("文章不存在，ID=" + postId));

	    // 驗證文章是否屬於該看板
	    if (!post.getBoardVO().getBoardId().equals(boardId)) {
	        throw new RuntimeException("文章不屬於此看板，boardId=" + boardId);
	    }

	    model.addAttribute("post", post);
	    model.addAttribute("boardList", boardRepository.findAll());
	    model.addAttribute("board", post.getBoardVO()); // 順便把看板物件丟進去，前端可以用

	    return "frontend/forum_post";
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
        postService.createPost(post);  
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
        postService.updatePost(id, post);  
        return "redirect:/post/" + id;
    }

    // 軟刪除文章
    @GetMapping("/post/delete/{id}")
    public String deletePost(@PathVariable Integer id) {
        postService.softDeletePost(id);  
        return "redirect:/forum";
    }
    
    @GetMapping("/forum/board/{id}")
    public String listPostsByBoard(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<PostVO> postPage = postService.getPostsByBoard(id, page, size);

        model.addAttribute("postListData", postPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());

        // ✅ 側邊欄需要的看板清單
        model.addAttribute("boardsSidebar", boardRepository.findAll());

        // ✅ 把當前看板塞進去，前端才能用 ${board.boardName}
        BoardVO board = boardRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("看板不存在，ID=" + id));
        model.addAttribute("board", board);

        return "frontend/forum_board";
    }



    // 搜尋文章
    @GetMapping("/forum/search")
    public String searchPosts(@RequestParam String keyword, Model model) {
        List<PostVO> list = postService.searchPosts(keyword);
        model.addAttribute("postListData", list);

        // ✅ 側邊欄需要的看板清單
        model.addAttribute("boardList", boardRepository.findAll());

        return "frontend/forum";
    }
    
    // 分頁文章列表
    @GetMapping("/forum")
    public String listAllPosts(@RequestParam(defaultValue="1") int page, Model model) {
        Page<PostVO> postPage = postService.getAllWithPage(page);

        model.addAttribute("postListData", postPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());

        // ✅ 側邊欄需要的看板清單
        model.addAttribute("boards", boardRepository.findAll());

        return "frontend/forum";
    }
}
