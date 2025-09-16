// src/main/java/com/babymate/frontend/FrontMhbController.java
package com.babymate.mhbfrontend.controller;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.babymate.member.model.MemberVO;
import com.babymate.mhb.model.MhbService;
import com.babymate.mhb.model.MhbVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/u/mhb")
public class FrontMhbController {

    private final MhbService mhbService;
    public FrontMhbController(MhbService mhbService) { this.mhbService = mhbService; }

    // 顯示新增表單
    @GetMapping("/new")
    public String newForm(HttpSession session, Model model) {
        MemberVO login = (MemberVO) session.getAttribute("loginMember");
        if (login == null) return "redirect:/shop/login"; // 依你站內登入頁

        // 若已經有一本，也可以選擇導去儀表板，或放提示
        MhbVO existed = mhbService.findActiveByMemberId(login.getMemberId());
        model.addAttribute("alreadyHasOne", existed != null);

        MhbVO vo = new MhbVO();
        vo.setMemberId(login.getMemberId());
        model.addAttribute("mhbVO", vo);
        return "frontend/u/mhb/new_mhb";
    }

    // 接收建立
    @PostMapping
    public String create(@ModelAttribute("mhbVO") MhbVO mhbVO,
                         @RequestParam(value = "upFiles", required = false) MultipartFile up,
                         HttpSession session, Model model) throws IOException {

        MemberVO login = (MemberVO) session.getAttribute("loginMember");
        if (login == null) return "redirect:/shop/login";

        // 強制綁定會員
        mhbVO.setMemberId(login.getMemberId());

        // 圖片（可選）
        if (up != null && !up.isEmpty()) {
            mhbVO.setUpFiles(up.getBytes());
        }

        MhbVO saved = mhbService.addMhb(mhbVO);
        Integer mhbId = saved.getMotherHandbookId();

        // 成功後導回儀表板「媽媽手冊」頁籤
        return "redirect:/blog/full-grid-left?tab=mhb&mhbId=" + mhbId;
    }
}
