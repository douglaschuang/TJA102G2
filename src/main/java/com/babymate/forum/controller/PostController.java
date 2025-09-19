package com.babymate.forum.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.babymate.forum.model.BoardService;
import com.babymate.forum.model.BoardVO;
import com.babymate.forum.model.PostService;
import com.babymate.forum.model.PostVO;

import jakarta.validation.Valid;

@Controller
@RequestMapping("")
public class PostController {

    @Autowired
    PostService postSvc;

    @Autowired
    BoardService boardSvc;

/*=====新增文章頁面===========================================================================*/
    @GetMapping("addPost")
    public String addPost(ModelMap model) {
        PostVO postVO = new PostVO();
        model.addAttribute("postVO", postVO);
        return "admin/forum/addPost";  // 對應到 addPost.html
    }

    /*
     * 新增文章提交
     */
    @PostMapping("insert")
    public String insert(@Valid PostVO postVO, BindingResult result, ModelMap model) {

        if (result.hasErrors()) {
            return "back-end/post/addPost";
        }

        // CKEditor 輸入的 HTML 會存在 postVO.getContent()
        postSvc.createPost(postVO);

        List<PostVO> list = postSvc.getAll();
        model.addAttribute("postListData", list);
        model.addAttribute("success", "- (新增成功)");
        return "redirect:/post/listAllPost"; 
    }

    /*
     * 查詢單一文章，用於進入修改頁面
     */
    @GetMapping("/admin/forum/updatePost")
    public String getOneForUpdate(@RequestParam("postId") Integer postId, ModelMap model) {
        System.out.println("Controller hit! postId = " + postId);

        PostVO postVO = postSvc.getOnePost(postId);
        if (postVO == null) {
            // 找不到文章，直接回列表或顯示錯誤
            return "redirect:/admin/forum/listAllPosts";
        }

        List<BoardVO> boards = boardSvc.findAllBoards(); // 下拉選單用
        model.addAttribute("postVO", postVO);
        model.addAttribute("boards", boards);

        System.out.println("postId = " + postVO.getPostId()); 
        System.out.println("postLine = " + postVO.getPostLine());  
        return "admin/forum/updatePost";  
    }

    /*
     * 更新文章（含修改板塊）
     */
    @PostMapping("/admin/forum/updatePost")
    public String update(@Valid PostVO postVO, BindingResult result, 
                         @RequestParam("boardId") Integer boardId,
                         ModelMap model) {

        if (result.hasErrors()) {
            model.addAttribute("boards", boardSvc.findAllBoards());
            return "admin/forum/updatePost";
        }

        Optional<BoardVO> optionalBoard = boardSvc.findBoardById(boardId);
        if (optionalBoard.isPresent()) {
            postVO.setBoardVO(optionalBoard.get());
        } else {
            model.addAttribute("error", "選擇的板塊不存在！");
            model.addAttribute("boards", boardSvc.findAllBoards());
            return "admin/forum/updatePost";
        }

        postSvc.updatePost(postVO.getPostId(), postVO);

        // ⚡ fetch 完整文章與板塊
        PostVO updatedPost = postSvc.getOnePostWithBoard(postVO.getPostId());
        model.addAttribute("postVO", updatedPost);

        model.addAttribute("success", "- (修改成功)");
        return "admin/forum/listOnePost";
    }
    
    /*
     * 刪除文章
     */
    @PostMapping("delete")
    public String delete(@RequestParam("postId") String postId, ModelMap model) {
        postSvc.softDeletePost(Integer.valueOf(postId));  // 改成軟刪
        List<PostVO> list = postSvc.getAll();
        model.addAttribute("postListData", list);
        model.addAttribute("success", "- (刪除成功)");
        return "admin/forum/index"; 
    }
    
    @PostMapping("/post/deleteAjax")
    @ResponseBody
    public String deleteAjax(@RequestParam("postId") Integer postId) {
        postSvc.softDeletePost(postId);  // 軟刪
        return "success";
    }
    
    @PostMapping("/post/toggleStatus")
    @ResponseBody
    public String toggleStatus(@RequestParam("postId") Integer postId) {
        postSvc.togglePostStatus(postId);  // 直接呼叫 service 的 toggle 方法
        PostVO post = postSvc.getOnePost(postId);
        return "success:" + post.getPostStatus();
    }
    
    
    
    

    /*
     * 複合查詢文章
     */
    @PostMapping("listPosts_ByCompositeQuery")
    public String listAllPost(Model model) {
        List<PostVO> list = postSvc.getAll();  // 直接拿所有文章
        model.addAttribute("postListData", list);
        return "back-end/post/listAllPost";
    }
    
    
/*====後台文章管理首頁======================================================================================================*/
    @GetMapping("/admin/forum/index")
    public String listAllPostsForAdmin(ModelMap model) {
        List<PostVO> list = postSvc.getAllWithMemberAndBoard();
        model.addAttribute("postListData", list);
        return "admin/forum/index";
    }


//    /*
//     * 提供看板資料給前端下拉選單 (新增/修改文章用)
//     */
//    @ModelAttribute("boardListData")
//    protected List<BoardVO> referenceBoardListData() {
//        return boardSvc.getAll();
//    }
//
//    /*
//     * 另一種方式: 提供看板 Map 給前端
//     */
//    @ModelAttribute("boardMapData")
//    protected Map<Integer, String> referenceBoardMapData() {
//        Map<Integer, String> map = new LinkedHashMap<>();
//        map.put(1, "閒聊板");
//        map.put(2, "感情板");
//        map.put(3, "遊戲板");
//        map.put(4, "工作板");
//        return map;
//    }
}