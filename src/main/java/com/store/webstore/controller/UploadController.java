package com.store.webstore.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.store.webstore.service.ImageStorageService;
import com.store.webstore.service.ImageStorageService.ImageStorageException;

@RestController
@RequestMapping("/admin/products")
public class UploadController {

    private final ImageStorageService imageStorageService;

    public UploadController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @PostMapping("/images")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile image) {
        try {
            ImageStorageService.StoredImage storedImage = imageStorageService.store(image);
            return ResponseEntity.ok(Map.of("imageUrl", storedImage.imageUrl()));
        } catch (ImageStorageException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Image upload failed"));
        }
    }
}
