package com.babymate.diaryfrontend.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.babymate.diary.model.DiaryEntry;
import com.babymate.diary.model.DiaryEntryService;
import com.babymate.member.model.MemberVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/u/diary")
public class FrontDiaryController {

	private final DiaryEntryService service;

	public FrontDiaryController(DiaryEntryService service) {
		this.service = service;
	}

	@GetMapping("/new")
	public String newForm(HttpSession session, Model model) {
		MemberVO login = (MemberVO) session.getAttribute("loginMember");
		if (login == null)
			return "redirect:/shop/login";
		model.addAttribute("now", java.time.LocalDateTime.now().toString().substring(0, 16)); // yyyy-MM-ddTHH:mm
		return "frontend/u/diary/new_diary";
	}

	@PostMapping
	public String create(@RequestParam("content") String content,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "tags", required = false) String tags,
			@RequestParam(value = "writtenAt", required = false) String writtenAtStr, HttpSession session) {
		MemberVO login = (MemberVO) session.getAttribute("loginMember");
		if (login == null)
			return "redirect:/shop/login";
		if (content == null || content.isBlank())
			return "redirect:/blog/full-grid-left?tab=diary";

		DiaryEntry e = new DiaryEntry();
		e.setMemberId(login.getMemberId());
		e.setTitle(title);
		e.setContent(content);
		e.setTags(tags);
		if (writtenAtStr != null && !writtenAtStr.isBlank()) {
			e.setWrittenAt(java.time.LocalDateTime.parse(writtenAtStr));
		}
		service.save(e);
		return "redirect:/blog/full-grid-left?tab=diary";
	}

	/** 編輯表單 */
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Integer id, HttpSession session, Model model) {
		MemberVO login = (MemberVO) session.getAttribute("loginMember");
		if (login == null)
			return "redirect:/shop/login";
		Optional<DiaryEntry> opt = service.findById(id);
		if (opt.isEmpty() || !opt.get().getMemberId().equals(login.getMemberId()))
			return "redirect:/blog/full-grid-left?tab=diary";
		model.addAttribute("entry", opt.get());
		return "frontend/u/diary/edit_diary";
	}

	/** 送出更新 */
	@PostMapping("/{id}/edit")
	public String update(@PathVariable Integer id, @RequestParam("content") String content,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "tags", required = false) String tags,
			@RequestParam(value = "writtenAt", required = false) String writtenAtStr, HttpSession session) {
		MemberVO login = (MemberVO) session.getAttribute("loginMember");
		if (login == null)
			return "redirect:/shop/login";
		Optional<DiaryEntry> opt = service.findById(id);
		if (opt.isEmpty() || !opt.get().getMemberId().equals(login.getMemberId()))
			return "redirect:/blog/full-grid-left?tab=diary";

		DiaryEntry e = opt.get();
		e.setTitle(title);
		e.setContent(content);
		e.setTags(tags);
		if (writtenAtStr != null && !writtenAtStr.isBlank()) {
			e.setWrittenAt(java.time.LocalDateTime.parse(writtenAtStr));
		}
		service.save(e);
		return "redirect:/blog/full-grid-left?tab=diary";
	}

	/** 刪除 */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Integer id, HttpSession session) {
		MemberVO login = (MemberVO) session.getAttribute("loginMember");
		if (login == null)
			return "redirect:/shop/login";
		service.findById(id).ifPresent(e -> {
			if (e.getMemberId().equals(login.getMemberId()))
				service.delete(id);
		});
		return "redirect:/blog/full-grid-left?tab=diary";
	}

	/** 單篇顯示（原本就有的話保留） */
	@GetMapping("/{id}")
	public String show(@PathVariable Integer id, HttpSession session, Model model) {
		MemberVO login = (MemberVO) session.getAttribute("loginMember");
		if (login == null)
			return "redirect:/shop/login";
		Optional<DiaryEntry> opt = service.findById(id);
		if (opt.isEmpty() || !opt.get().getMemberId().equals(login.getMemberId()))
			return "redirect:/blog/full-grid-left?tab=diary";
		model.addAttribute("entry", opt.get());
		return "frontend/u/diary/show_diary";
	}
}
