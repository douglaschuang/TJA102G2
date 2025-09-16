package com.babymate.albumfrontend.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.babymate.album.model.AlbumPhoto;
import com.babymate.album.model.AlbumPhotoService;
import com.babymate.member.model.MemberVO;

import jakarta.servlet.http.HttpSession;

//FrontAlbumController.java
@Controller
public class FrontAlbumController {

	private final AlbumPhotoService service;

	public FrontAlbumController(AlbumPhotoService service) {
		this.service = service;
	}

	/** 表單：批次上傳 */
	@GetMapping("/u/album/new")
	public String newForm(HttpSession session, Model model) {
		MemberVO login = (MemberVO) session.getAttribute("loginMember");
		if (login == null)
			return "redirect:/shop/login";
		return "frontend/u/album/new_photos"; // ← 換成新的多檔表單
	}

	/** 送出：批次建立（多檔） */
	@PostMapping("/u/album/batch")
	public String batch(@RequestParam("files") MultipartFile[] files, HttpSession session) throws IOException {
		MemberVO login = (MemberVO) session.getAttribute("loginMember");
		if (login == null)
			return "redirect:/shop/login";

		if (files != null) {
			for (MultipartFile f : files) {
				if (f == null || f.isEmpty())
					continue;
				AlbumPhoto p = new AlbumPhoto();
				p.setMemberId(login.getMemberId());
				p.setCaption(Objects.requireNonNullElse(f.getOriginalFilename(), "photo"));
				p.setContentType(f.getContentType());
				p.setData(f.getBytes());
				p.setTakenAt(java.time.LocalDateTime.now());
				service.save(p);
			}
		}
		return "redirect:/blog/full-grid-left?tab=album";
	}

	/** 刪除單張（限本人） */
	@PostMapping("/u/album/{id}/delete")
	public String delete(@PathVariable Integer id, HttpSession session) {
		MemberVO login = (MemberVO) session.getAttribute("loginMember");
		if (login == null)
			return "redirect:/shop/login";
		service.findById(id).ifPresent(p -> {
			if (p.getMemberId().equals(login.getMemberId()))
				service.delete(id);
		});
		return "redirect:/blog/full-grid-left?tab=album";
	}

	/** 取圖（原本的） */
	@GetMapping("/album/photo/{id}")
	public ResponseEntity<byte[]> photo(@PathVariable Integer id, HttpSession session) {
		MemberVO login = (MemberVO) session.getAttribute("loginMember");
		if (login == null)
			return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/shop/login").build();

		Optional<AlbumPhoto> opt = service.findById(id);
		if (opt.isEmpty() || !opt.get().getMemberId().equals(login.getMemberId())) {
			return ResponseEntity.notFound().build();
		}
		AlbumPhoto p = opt.get();
		MediaType type = switch (String.valueOf(p.getContentType()).toLowerCase()) {
		case "image/png" -> MediaType.IMAGE_PNG;
		case "image/gif" -> MediaType.IMAGE_GIF;
		default -> MediaType.IMAGE_JPEG;
		};
		return ResponseEntity.ok().contentType(type).cacheControl(CacheControl.noCache())
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=photo-" + p.getId()).body(p.getData());
	}
}
