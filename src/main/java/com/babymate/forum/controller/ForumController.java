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
import com.babymate.forum.model.PostService;
import com.babymate.forum.model.PostVO;

@Controller
@RequestMapping("")
public class ForumController {
	
	@Autowired
	private PostService postService;


    @Autowired
    private BoardRepository boardRepository;

//	/*====前台文章首頁==========================================================================================================*/
//    @GetMapping("/forum")
//    public String listAllPosts(ModelMap model) {
//        List<PostVO> list = postService.getAllWithMemberAndBoard();
//        model.addAttribute("postListData", list);
//        return "frontend/forum";
//    }
	
    // 查看單篇文章
    @GetMapping("/post/{id}")
    public String forumPost(@PathVariable Integer id, Model model) {
        PostVO post = postService.getOnePost(id);
        model.addAttribute("post", post);
        return "forum/post";
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
    
    
   //側邊
    @GetMapping("/forum/board/{id}")
    public String listPostsByBoard(@PathVariable Integer id, Model model) {
        List<PostVO> list = postService.getPostsByBoard(id);
        model.addAttribute("postListData", list);
        return "frontend/forum";
    }
    //搜尋
    @GetMapping("/forum/search")
    public String searchPosts(@RequestParam String keyword, Model model) {
        List<PostVO> list = postService.searchPosts(keyword);
        model.addAttribute("postListData", list);
        return "frontend/forum";
    }
    
    //分頁
    @GetMapping("/forum")
    public String listAllPosts(@RequestParam(defaultValue="1") int page, Model model) {
        Page<PostVO> postPage = postService.getAllWithPage(page);

        // ✅ Debug 輸出文章清單到 console
        postPage.getContent().forEach(post -> {
            System.out.println(
                "文章ID=" + post.getPostId() +
                ", 標題=" + post.getPostTitle() +
                ", 看板=" + (post.getBoardVO() != null ? post.getBoardVO().getBoardName() : "null") +
                ", 作者=" + (post.getMemberVO() != null ? post.getMemberVO().getName() : "null")
            );
        });

        model.addAttribute("postListData", postPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        return "frontend/forum";
    }


    
    
    
    

}