package com.babymate.babyhandbook.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.babymate.babyhandbook.model.BabyhandbookService;
import com.babymate.babyhandbook.model.BabyhandbookVO;

import org.springframework.http.HttpHeaders;


@RestController
@RequestMapping("/api/babyhandbook")
public class BabyhandbookRestController {

	@Autowired
	BabyhandbookService babyhandbookSvc;

    @GetMapping("/active")
    public List<BabyhandbookVO> findAllActive() {
        return babyhandbookSvc.findAllActive(); 
    }

    @GetMapping("/deleted")
    public List<BabyhandbookVO> findAllDeleted() {
        return babyhandbookSvc.findAllDeleted();
    }

    @GetMapping("/photo/{id}")
    public ResponseEntity<byte[]> getPhotoBytesRaw(@PathVariable("id") Integer id) {
        byte[] photoBytes = babyhandbookSvc.getPhotoBytesRaw(id);
        if (photoBytes == null || photoBytes.length == 0) {
            return ResponseEntity.notFound().build();// 404 Not Found
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(photoBytes, headers, HttpStatus.OK);
    
    }
}
