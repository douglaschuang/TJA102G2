//package com.babymate.web;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import com.babymate.forum.model.PostService;
//import com.babymate.forum.model.PostVO;
//
//
//@Controller
//@RequestMapping("/forum")
//public class ForumController {
//	
//	@Autowired
//	private PostService postService;
//	
//    // 論壇首頁
//    @GetMapping({ "", "/" })
//    public String forumHome(Model model) {
//        model.addAttribute("posts", postService.getAll());
//        return "forum/index";
//    }
//	
//    // 查看單篇文章
//    @GetMapping("/post/{id}")
//    public String forumPost(@PathVariable Integer id, Model model) {
//        PostVO post = postService.getOnePost(id);
//        model.addAttribute("post", post);
//        return "forum/post";
//    }
//    // 發表新文章表單
//    @GetMapping("/post/new")
//    public String newPostForm(Model model) {
//        model.addAttribute("post", new PostVO());
//        return "forum/post_form";
//    }
//
//    // 提交新文章
//    @PostMapping("/post/new")
//    public String createPost(@ModelAttribute PostVO post) {
//        postService.createPost(post);  // 交給 service 處理細節
//        return "redirect:/forum";
//    }
//
//    // 編輯文章表單
//    @GetMapping("/post/edit/{id}")
//    public String editPostForm(@PathVariable Integer id, Model model) {
//        model.addAttribute("post", postService.getOnePost(id));
//        return "forum/post_form";
//    }
//    // 提交編輯文章
//    @PostMapping("/post/edit/{id}")
//    public String updatePost(@PathVariable Integer id, @ModelAttribute PostVO post) {
//        postService.updatePost(id, post);  // 交給 service
//        return "redirect:/forum/post/" + id;
//    }
// // 軟刪除文章
//    @GetMapping("/post/delete/{id}")
//    public String deletePost(@PathVariable Integer id) {
//        postService.softDeletePost(id);  // 交給 service
//        return "redirect:/forum";
//    }
//}


