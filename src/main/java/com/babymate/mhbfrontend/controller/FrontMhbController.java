package com.babymate.mhbfrontend.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.babymate.member.model.MemberVO;
import com.babymate.mhb.model.MhbService;
import com.babymate.mhb.model.MhbVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/u/mhb")
public class FrontMhbController {

	private final MhbService mhbService;

	public FrontMhbController(MhbService mhbService) {
		this.mhbService = mhbService;
	}
	
	// 顯示新增表單：只有在 URL 帶了 mhbId 且該 ID 存在時，才顯示「你已經有一本」
	@GetMapping("/new")
	public String newForm(@RequestParam(value = "mhbId", required = false) Integer mhbId,
	                      HttpSession session, Model model) {
	    MemberVO login = (MemberVO) session.getAttribute("member");
	    if (login == null) return "redirect:/shop/login";

	    // ★ 改成以 mhbId 判斷
	    boolean alreadyHasOne = mhbService.existsActiveById(mhbId);
	    model.addAttribute("alreadyHasOne", alreadyHasOne);

	    MhbVO vo = new MhbVO();
	    vo.setMemberId(login.getMemberId());
	    model.addAttribute("mhbVO", vo);
	    return "frontend/u/mhb/new_mhb";
	}

	//接收建立
	@PostMapping
	public String create(@ModelAttribute("mhbVO") MhbVO mhbVO,
	                     @RequestParam(value = "up", required = false) MultipartFile up,
	                     HttpSession session, Model model) throws IOException {

	    MemberVO login = (MemberVO) session.getAttribute("member");
	    if (login == null) return "redirect:/shop/login";

	    mhbVO.setMemberId(login.getMemberId());
	    if (up != null && !up.isEmpty()) mhbVO.setUpFiles(up.getBytes());

	    MhbVO saved = mhbService.addMhb(mhbVO);
	    Integer mhbId = saved.getMotherHandbookId();

	    // ★ 直接把新建那本的 mhbId 帶回去
	    return "redirect:/blog/full-grid-left?tab=mhb&mhbId=" + mhbId;
	}
	
	// 顯示編輯表單（只允許擁有者）
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable("id") Integer id,
	                       HttpSession session, Model model) {
	    MemberVO login = (MemberVO) session.getAttribute("member");
	    if (login == null) return "redirect:/shop/login";

	    MhbVO mhb = mhbService.getOneMhb(id);
	    if (mhb == null || !Objects.equals(mhb.getMemberId(), login.getMemberId())) {
	        // 非擁有者或不存在，導回儀表板
	        return "redirect:/blog/full-grid-left?tab=mhb";
	    }
	    model.addAttribute("mhbVO", mhb);
	    return "frontend/u/mhb/edit_mhb";
	}

	// 接收編輯送出
	@PostMapping("/{id}/edit")
	public String update(@PathVariable("id") Integer id,
	                     @ModelAttribute("mhbVO") MhbVO form,
	                     @RequestParam(value = "up", required = false) MultipartFile up,
	                     HttpSession session) throws IOException {

	    MemberVO login = (MemberVO) session.getAttribute("member");
	    if (login == null) return "redirect:/shop/login";

	    MhbVO mhb = mhbService.getOneMhb(id);
	    if (mhb == null || !Objects.equals(mhb.getMemberId(), login.getMemberId())) {
	        return "redirect:/blog/full-grid-left?tab=mhb";
	    }

	    // 只更新前台可編的欄位
	    mhb.setMotherName(form.getMotherName());
	    mhb.setMotherBirthday(form.getMotherBirthday());
	    mhb.setLastMcDate(form.getLastMcDate());
	    mhb.setExpectedBirthDate(form.getExpectedBirthDate());
	    mhb.setWeight(form.getWeight());

	    if (up != null && !up.isEmpty()) {
	        mhb.setUpFiles(up.getBytes());
	    }

	    mhbService.updateMhb(mhb);
	    return "redirect:/blog/full-grid-left?tab=mhb&mhbId=" + id;
	}
	
	/** 前台：軟刪除（把 deletedAt 設為現在） */
	// FrontMhbController.java（class 上已有 @RequestMapping("/u/mhb")）
	@PostMapping("/delete")
	public ResponseEntity<?> softDeleteAjax(@RequestParam("mother_handbook_id") Integer mhbId,
	                                        HttpSession session) {
	    MemberVO me = (MemberVO) session.getAttribute("member");
	    if (me == null) {
	        return ResponseEntity.status(401).body(Map.of("ok", false, "msg", "請先登入"));
	    }
	    boolean ok = mhbService.softDeleteByIdAndMember(mhbId, me.getMemberId());
	    if (!ok) {
	        return ResponseEntity.status(403).body(Map.of("ok", false, "msg", "無權限或資料不存在"));
	    }
	    return ResponseEntity.ok(Map.of("ok", true));
	}





}
