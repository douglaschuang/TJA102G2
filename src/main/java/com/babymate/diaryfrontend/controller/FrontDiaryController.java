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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping("/u/diary")
public class FrontDiaryController {

	private final DiaryEntryService service;

	public FrontDiaryController(DiaryEntryService service) {
		this.service = service;
	}

	@GetMapping("/new")
	public String newForm(HttpSession session, Model model) {
		MemberVO login = (MemberVO) session.getAttribute("member");
		if (login == null)
			return "redirect:/shop/login";
		model.addAttribute("now", java.time.LocalDateTime.now().toString().substring(0, 16)); // yyyy-MM-ddTHH:mm
		return "frontend/u/diary/new_diary";
	}

	@PostMapping
	public String create(@RequestParam("content") String content,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "tags", required = false) String tags,
			@RequestParam(value = "writtenAt", required = false) String writtenAtStr,
			@RequestParam(value = "image", required = false) MultipartFile image,
			HttpSession session
			) {
		MemberVO login = (MemberVO) session.getAttribute("member");
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
		
		// 圖片處理（可加大小/型別檢查）
	    if (image != null && !image.isEmpty()) {
	        try {
	            // 簡單限制：3MB 內、content-type 必須是 image/*
	            if (image.getSize() <= 3 * 1024 * 1024 && image.getContentType() != null
	                && image.getContentType().startsWith("image/")) {
	                e.setImageData(image.getBytes());
	                e.setImageContentType(image.getContentType());
	            }
	        } catch (Exception ex) {
	            // 失敗就略過圖片，不中斷流程
	        }
	    }
		service.save(e);
		return "redirect:/blog/full-grid-left?tab=diary";
	}

	/** 編輯表單 */
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Integer id, HttpSession session, Model model) {
		MemberVO login = (MemberVO) session.getAttribute("member");
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
			@RequestParam(value = "writtenAt", required = false) String writtenAtStr,
			@RequestParam(value = "image", required = false) MultipartFile image,
			@RequestParam(value = "removeImage", required = false) String removeImage,
			HttpSession session
			) {
		MemberVO login = (MemberVO) session.getAttribute("member");
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
		
		// 先處理「移除」
	    if (removeImage != null) {
	        e.setImageData(null);
	        e.setImageContentType(null);
	    }
		
		// 如果上傳了新檔，就覆蓋；若沒上傳，保留舊圖
	    if (image != null && !image.isEmpty()) {
	        try {
	            if (image.getSize() <= 3 * 1024 * 1024 && image.getContentType() != null
	                && image.getContentType().startsWith("image/")) {
	                e.setImageData(image.getBytes());
	                e.setImageContentType(image.getContentType());
	            }
	        } catch (Exception ex) { /* 略過錯誤 */ }
	    }
		service.save(e);
		return "redirect:/blog/full-grid-left?tab=diary";
	}

	/** 刪除 */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Integer id, HttpSession session) {
		MemberVO login = (MemberVO) session.getAttribute("member");
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
		MemberVO login = (MemberVO) session.getAttribute("member");
		if (login == null)
			return "redirect:/shop/login";
		Optional<DiaryEntry> opt = service.findById(id);
		if (opt.isEmpty() || !opt.get().getMemberId().equals(login.getMemberId()))
			return "redirect:/blog/full-grid-left?tab=diary";
		model.addAttribute("entry", opt.get());
		return "frontend/u/diary/show_diary";
	}
	
	@GetMapping("/{id}/image")
	public ResponseEntity<byte[]> image(@PathVariable Integer id, HttpSession session) {
	    MemberVO login = (MemberVO) session.getAttribute("member");
	    if (login == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

	    Optional<DiaryEntry> opt = service.findById(id);
	    if (opt.isEmpty() || !opt.get().getMemberId().equals(login.getMemberId()))
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

	    DiaryEntry e = opt.get();
	    if (e.getImageData() == null || e.getImageData().length == 0)
	        return ResponseEntity.notFound().build();

	    HttpHeaders headers = new HttpHeaders();
	    String ct = (e.getImageContentType() != null) ? e.getImageContentType() : MediaType.IMAGE_JPEG_VALUE;
	    headers.set(HttpHeaders.CONTENT_TYPE, ct);
	 // 🔧 關閉快取（關鍵）
	    headers.set(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0");
	    headers.add("Pragma", "no-cache");

	    return new ResponseEntity<>(e.getImageData(), headers, HttpStatus.OK);
	}

}
