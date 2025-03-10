package com.store.webstore.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
public class UploadController {

    @PostMapping("/uploadImage")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile image) {
        Map<String, String> response = new HashMap<>();
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File uploadFolder = new File(uploadDir);

            if (!uploadFolder.exists() && !uploadFolder.mkdirs()) {
                throw new RuntimeException("Failed to create upload directory: " + uploadDir);
            }

            String fileName = UUID.randomUUID().toString() + "-" + image.getOriginalFilename();
            File destinationFile = new File(Paths.get(uploadDir, fileName).toString());

            image.transferTo(destinationFile);

            response.put("imageUrl", "/images/" + fileName);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
