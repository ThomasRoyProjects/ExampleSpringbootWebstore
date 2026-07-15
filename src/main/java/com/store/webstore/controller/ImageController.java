package com.store.webstore.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.store.webstore.service.ImageStorageService;

@Controller
public class ImageController {

    private final ImageStorageService imageStorageService;

    public ImageController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @GetMapping("/images/{imageName}")
    public ResponseEntity<Resource> getProductImage(@PathVariable("imageName") String imageName) {
        return imageStorageService.find(imageName)
                .map(image -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(image.mediaType()))
                        .cacheControl(CacheControl.noCache())
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imageName + "\"")
                        .header("X-Content-Type-Options", "nosniff")
                        .body(image.resource()))
                .orElseGet(() -> ResponseEntity.notFound()
                        .header("X-Content-Type-Options", "nosniff")
                        .build());
    }
}
