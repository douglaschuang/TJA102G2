package com.babymate.mhb.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.babymate.mhb.model.MhbService;
import com.babymate.mhb.model.MhbVO;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/mhb")
public class MhbController {

	private final MhbService mhbSvc;

	public MhbController(MhbService mhbSvc) {
		this.mhbSvc = mhbSvc;
	}

	@GetMapping("addMhb")
	public String addMhb(ModelMap model) {
		MhbVO mhbVO = new MhbVO();
		mhbVO.setMemberId(1); // 之後要用登入者 id
		model.addAttribute("mhbVO", mhbVO);
		
		// 提供給 layout 用的頁名
	    model.addAttribute("pageTitle", "媽媽手冊｜新增");
	    
		return "admin/mhb/addMhb";
	}

	@PostMapping("insert")
	public String insert(@Valid @ModelAttribute("mhbVO") MhbVO mhbVO, BindingResult result, ModelMap model,
			@RequestParam(value = "upFiles", required = false) MultipartFile[] parts, RedirectAttributes ra)
			throws IOException {

		System.out.println(">>> [MhbController] insert() CALLED");
		result = removeFieldError(mhbVO, result, "upFiles");

		// 圖片可選
		if (parts != null) {
			for (MultipartFile mf : parts) {
				if (mf != null && !mf.isEmpty()) {
					mhbVO.setUpFiles(mf.getBytes());
				}
			}
		}

		if (result.hasErrors()) {
			System.out.println(">>> [MhbController] Binding errors:");
			result.getAllErrors().forEach(e -> System.out.println(" - " + e.toString()));
			return "admin/mhb/addMhb";
		}

		MhbVO saved = mhbSvc.addMhb(mhbVO);
		Integer id = saved != null ? saved.getMotherHandbookId() : null;
		System.out.println(">>> [MhbController] INSERT OK, new id = " + id);

		ra.addFlashAttribute("success", "- (新增成功 #" + id + ")");
		return "redirect:/admin/mhb/list";
	}

	@PostMapping("getOne_For_Update")
	public String getOne_For_Update(@RequestParam("mother_handbook_id") Integer id, ModelMap model) {
		model.addAttribute("mhbVO", mhbSvc.getOneMhb(id));
		model.addAttribute("pageTitle", "媽媽手冊｜修改");
		return "admin/mhb/update_mhb_input";
	}

	@PostMapping("update")
	public String update(@Valid @ModelAttribute("mhbVO") MhbVO mhbVO, BindingResult result, ModelMap model,
			@RequestParam(value = "upFiles", required = false) MultipartFile[] parts) throws IOException {

		result = removeFieldError(mhbVO, result, "upFiles");

		boolean hasUpload = parts != null && parts.length > 0 && !parts[0].isEmpty();
		if (hasUpload) {
			for (MultipartFile mf : parts) {
				if (mf != null && !mf.isEmpty()) {
					mhbVO.setUpFiles(mf.getBytes());
				}
			}
		} else {
			byte[] old = mhbSvc.getOneMhb(mhbVO.getMotherHandbookId()).getUpFiles();
			mhbVO.setUpFiles(old);
		}

		if (result.hasErrors())
			return "admin/mhb/update_mhb_input";

		mhbSvc.updateMhb(mhbVO);

		model.addAttribute("success", "- (修改成功)");
		model.addAttribute("mhbVO", mhbSvc.getOneMhb(mhbVO.getMotherHandbookId()));
		model.addAttribute("pageTitle", "媽媽手冊｜修改成功");
		return "admin/mhb/listOneMhb";
	}

	// 「刪除」這裡會走軟刪（已在 Entity 用 @SQLDelete 或在 Service 內做 update deleted=1）
	@PostMapping("delete")
	public String delete(@RequestParam("mother_handbook_id") Integer id, RedirectAttributes ra) {
		mhbSvc.deleteMhb(id); // 軟刪
		ra.addFlashAttribute("success", "- (刪除成功)");
		return "redirect:/admin/mhb/list";
	}

	/* ====== 提供圖片 ====== */

	@GetMapping("/photo/{id}")
	public ResponseEntity<byte[]> photo(@PathVariable Integer id) throws IOException {
		MhbVO vo = mhbSvc.getOneMhb(id); // 受 @Where 過濾（未刪）
		if (vo == null || vo.getUpFiles() == null || vo.getUpFiles().length == 0) {
			return ResponseEntity.notFound().build();
		}
		byte[] bytes = vo.getUpFiles();
		MediaType type = sniff(bytes);
		return ResponseEntity.ok().contentType(type).cacheControl(CacheControl.noCache())
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=mhb-" + id).body(bytes);
	}

	private MediaType sniff(byte[] b) {
		if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 0x50 && b[2] == 0x4E && b[3] == 0x47)
			return MediaType.IMAGE_PNG;
		if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8)
			return MediaType.IMAGE_JPEG;
		if (b.length >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F')
			return MediaType.IMAGE_GIF;
		return MediaType.APPLICATION_OCTET_STREAM;
	}

	// 垃圾桶用：忽略 @Where，直接拿 byte[]
	@GetMapping("/photo-deleted/{id}")
	public void photoDeleted(@PathVariable Integer id, jakarta.servlet.http.HttpServletResponse resp)
			throws IOException {
		byte[] bytes = mhbSvc.getPhotoBytesRaw(id); // 你在 service 用 nativeQuery 直接撈
		if (bytes == null) {
			resp.setStatus(404);
			return;
		}
		resp.setContentType(detectImageMimeType(bytes));
		resp.getOutputStream().write(bytes);
	}

	private static String detectImageMimeType(byte[] data) {
		if (data.length >= 8 && (data[0] & 0xFF) == 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47
				&& data[4] == 0x0D && data[5] == 0x0A && data[6] == 0x1A && data[7] == 0x0A)
			return "image/png";
		if (data.length >= 3 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8)
			return "image/jpeg";
		if (data.length >= 6 && data[0] == 'G' && data[1] == 'I' && data[2] == 'F')
			return "image/gif";
		return "application/octet-stream";
	}

	/* ====== 垃圾桶動作（提供給 admin 殼呼叫） ====== */

	// 復原
	@PostMapping("restore")
	public String restore(@RequestParam("mother_handbook_id") Integer id, RedirectAttributes ra) {
		int n = mhbSvc.restoreMhb(id); // UPDATE mother_handbook SET deleted=0 WHERE id=? AND deleted=1
		ra.addFlashAttribute("success", n > 0 ? "- (已復原 #" + id + ")" : "- (未找到或已復原)");
		return "redirect:/admin/mhb/deleted"; // 復原後回垃圾桶
	}
	
	


	/* ====== 小工具 ====== */

	public BindingResult removeFieldError(MhbVO mhbVO, BindingResult result, String removedFieldname) {
		List<FieldError> keep = result.getFieldErrors().stream().filter(e -> !e.getField().equals(removedFieldname))
				.collect(Collectors.toList());
		BindingResult newResult = new BeanPropertyBindingResult(mhbVO, "mhbVO");
		keep.forEach(newResult::addError);
		return newResult;
	}
}
