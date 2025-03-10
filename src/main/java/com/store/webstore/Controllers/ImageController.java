package com.store.webstore.Controllers;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class ImageController {

    private static final String UPLOAD_DIR = "uploads/"; // external storage for your uploaded images
    private static final String STATIC_DIR = "src/main/resources/static/images/"; // preloaded images

    @GetMapping("/images/{imageName}")
    public ResponseEntity<Resource> getProductImage(@PathVariable("imageName") String imageName) throws Exception {
        //check if the image exists in the uploads folder
        Path imagePath = Paths.get(UPLOAD_DIR).resolve(imageName);

        if (!Files.exists(imagePath)) {
            //if not found in uploads, checks static images
            imagePath = Paths.get(STATIC_DIR).resolve(imageName);
        }

        if (Files.exists(imagePath)) {
            Resource resource = new UrlResource(imagePath.toUri());

            // probe content type of image (image/jpeg, image/png, etc.)
            String contentType = Files.probeContentType(imagePath);
            if (contentType == null) {
                contentType = "application/octet-stream"; // fallback
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))  // set content type
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imageName + "\"") // display the image inline
                    .body(resource); 
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
