package com.babymate.forum.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;



// ★ Annotation 1: @RestController
//告訴 Spring Boot 這是一個專門回傳「資料」(如 JSON) 的 API 控制器
@RestController
public class ImageUploadController {

    // 我們仍然把圖片存在專案內部，維持對團隊最友善的作法
    private final String UPLOAD_DIR = "src/main/resources/static/images/forum-uploads/";

    // =================================================================
    //  方法一：「收貨員」- 負責接收上傳的圖片並儲存
    // =================================================================
    @PostMapping("/forum/uploadImage")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("upload") MultipartFile file) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "上傳的檔案不能是空的"));
        }

        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            Path filePath = Paths.get(UPLOAD_DIR + uniqueFileName);
            Files.write(filePath, file.getBytes());

            // 產生相對路徑，讓前端使用
            String fileUrl = "/images/forum-uploads/" + uniqueFileName;

            Map<String, String> response = Collections.singletonMap("url", fileUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "檔案上傳失敗"));
        }
    }

    // =================================================================
    //  ★★ 方法二：「發貨員」- 負責根據 URL 路徑讀取圖片並回傳 ★★
    // =================================================================
    @GetMapping("/images/forum-uploads/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            // 1. 根據傳入的檔名，組合出檔案在硬碟上的完整路徑
            Path file = Paths.get(UPLOAD_DIR).resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            // 2. 檢查檔案是否存在且可讀
            if (resource.exists() || resource.isReadable()) {
                // 3. 如果存在，就回傳檔案內容給瀏覽器
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(file))
                        .body(resource);
            } else {
                // 4. 如果檔案不存在，就回傳 404 Not Found
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            // 捕獲所有可能的錯誤
            return ResponseEntity.status(500).build();
        }
    }
}
