package com.babymate.mhbfrontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.babymate.member.model.MemberVO;
import com.babymate.mhb.model.MhbService;
import com.babymate.mhb.model.MhbVO;
import com.babymate.todo.model.MhbTodo;
import com.babymate.todo.model.MhbTodoService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/u/todos")
public class FrontTodoController {

	private final MhbService mhbService;
	private final MhbTodoService todoService;

	public FrontTodoController(MhbService mhbService, MhbTodoService todoService) {
		this.mhbService = mhbService;
		this.todoService = todoService;
	}

	// 顯示新增表單
	@GetMapping("/new")
	public String newForm(@RequestParam(value = "mhbId", required = false) Integer mhbId, HttpSession session,
			Model model) {
		MemberVO login = (MemberVO) session.getAttribute("member");
		if (login == null)
			return "redirect:/shop/login";

		MhbVO mhb = (mhbId != null) ? mhbService.getOneMhb(mhbId)
				: mhbService.findActiveByMemberId(login.getMemberId());
		if (mhb == null || !mhb.getMemberId().equals(login.getMemberId())) {
			return "redirect:/blog/full-grid-left?tab=todos";
		}

		MhbTodo todo = new MhbTodo();
		todo.setMotherHandbookId(mhb.getMotherHandbookId());

		model.addAttribute("mhb", mhb);
		model.addAttribute("todo", todo);
		return "frontend/u/todo/new_todo";
	}

	// 接收新增
	@PostMapping
	public String create(@ModelAttribute("todo") MhbTodo form, @RequestParam("mhbId") Integer mhbId,
			HttpSession session) {
		MemberVO login = (MemberVO) session.getAttribute("member");
		if (login == null)
			return "redirect:/shop/login";

		MhbVO mhb = mhbService.getOneMhb(mhbId);
		if (mhb == null || !mhb.getMemberId().equals(login.getMemberId())) {
			return "redirect:/blog/full-grid-left?tab=todos";
		}

		form.setMotherHandbookId(mhbId);
		if (form.getDone() == null)
			form.setDone(false);
		todoService.save(form);

		return "redirect:/blog/full-grid-left?tab=todos&mhbId=" + mhbId;
	}

	// ===== 切換完成 =====
	@PostMapping("/{id}/toggle")
	public String toggle(@PathVariable("id") Integer id, @RequestParam("mhbId") Integer mhbId, HttpSession session) {
		MemberVO login = (MemberVO) session.getAttribute("member");
		if (login == null)
			return "redirect:/shop/login";

		MhbTodo t = todoService.getOne(id);
		if (t == null)
			return "redirect:/blog/full-grid-left?tab=todos&mhbId=" + mhbId;

		MhbVO mhb = mhbService.getOneMhb(t.getMotherHandbookId());
		if (mhb == null || !mhb.getMemberId().equals(login.getMemberId())) {
			return "redirect:/blog/full-grid-left?tab=todos";
		}

		t.setDone(Boolean.TRUE.equals(t.getDone()) ? false : true);
		todoService.save(t);

		return "redirect:/blog/full-grid-left?tab=todos&mhbId=" + mhb.getMotherHandbookId();
	}

	// ===== 編輯表單 =====
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable("id") Integer id, @RequestParam("mhbId") Integer mhbId, HttpSession session,
			Model model) {
		MemberVO login = (MemberVO) session.getAttribute("member");
		if (login == null)
			return "redirect:/shop/login";

		MhbTodo t = todoService.getOne(id);
		if (t == null)
			return "redirect:/blog/full-grid-left?tab=todos&mhbId=" + mhbId;

		MhbVO mhb = mhbService.getOneMhb(t.getMotherHandbookId());
		if (mhb == null || !mhb.getMemberId().equals(login.getMemberId())) {
			return "redirect:/blog/full-grid-left?tab=todos";
		}

		model.addAttribute("mhb", mhb);
		model.addAttribute("todo", t);
		return "frontend/u/todo/edit_todo";
	}

	// ===== 送出編輯 =====
	@PostMapping("/{id}/edit")
	public String update(@PathVariable("id") Integer id, @RequestParam("mhbId") Integer mhbId,
			@ModelAttribute("todo") MhbTodo form, HttpSession session) {
		MemberVO login = (MemberVO) session.getAttribute("member");
		if (login == null)
			return "redirect:/shop/login";

		MhbTodo t = todoService.getOne(id);
		if (t == null)
			return "redirect:/blog/full-grid-left?tab=todos&mhbId=" + mhbId;

		MhbVO mhb = mhbService.getOneMhb(t.getMotherHandbookId());
		if (mhb == null || !mhb.getMemberId().equals(login.getMemberId())) {
			return "redirect:/blog/full-grid-left?tab=todos";
		}

		// 可編輯欄位
		t.setTitle(form.getTitle());
		t.setNote(form.getNote());
		t.setDueDate(form.getDueDate());
		t.setDone(Boolean.TRUE.equals(form.getDone()));

		todoService.save(t);
		return "redirect:/blog/full-grid-left?tab=todos&mhbId=" + mhb.getMotherHandbookId();
	}

	// ===== 刪除 =====
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable("id") Integer id, @RequestParam("mhbId") Integer mhbId, HttpSession session) {
		MemberVO login = (MemberVO) session.getAttribute("member");
		if (login == null)
			return "redirect:/shop/login";

		MhbTodo t = todoService.getOne(id);
		if (t != null) {
			MhbVO mhb = mhbService.getOneMhb(t.getMotherHandbookId());
			if (mhb != null && mhb.getMemberId().equals(login.getMemberId())) {
				todoService.delete(id);
				return "redirect:/blog/full-grid-left?tab=todos&mhbId=" + mhb.getMotherHandbookId();
			}
		}
		return "redirect:/blog/full-grid-left?tab=todos&mhbId=" + mhbId;
	}

}
