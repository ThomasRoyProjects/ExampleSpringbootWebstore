package com.store.webstore.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorageService {

    private static final long MAX_IMAGE_BYTES = 2L * 1024L * 1024L;
    private static final int MAX_DIMENSION = 2000;

    private final Path uploadRoot;

    public ImageStorageService(@Value("${webstore.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public StoredImage store(MultipartFile image) throws ImageStorageException {
        if (image == null || image.isEmpty()) {
            throw new ImageStorageException("Image is required");
        }
        if (image.getSize() > MAX_IMAGE_BYTES) {
            throw new ImageStorageException("Image is too large");
        }

        DecodedImage decoded = decode(image);
        String filename = UUID.randomUUID() + decoded.extension();
        Path destination = uploadRoot.resolve(filename).normalize();
        if (!destination.startsWith(uploadRoot)) {
            throw new ImageStorageException("Invalid image path");
        }

        try {
            Files.createDirectories(uploadRoot);
            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, destination);
            }
        } catch (IOException ex) {
            throw new ImageStorageException("Image could not be stored", ex);
        }

        return new StoredImage(filename, "/images/" + filename, decoded.mediaType());
    }

    public Optional<ResolvedImage> find(String filename) {
        if (!StringUtils.hasText(filename) || filename.contains("/") || filename.contains("\\")) {
            return Optional.empty();
        }

        Path uploadPath = uploadRoot.resolve(filename).normalize();
        if (uploadPath.startsWith(uploadRoot) && Files.isRegularFile(uploadPath)) {
            try {
                DecodedImage decoded = decode(uploadPath);
                return Optional.of(new ResolvedImage(new UrlResource(uploadPath.toUri()), decoded.mediaType()));
            } catch (IOException | ImageStorageException ex) {
                return Optional.empty();
            }
        }

        ClassPathResource classPathResource = new ClassPathResource("static/images/" + filename);
        if (!classPathResource.exists() || !classPathResource.isReadable()) {
            return Optional.empty();
        }
        try {
            DecodedImage decoded = decode(classPathResource);
            return Optional.of(new ResolvedImage(classPathResource, decoded.mediaType()));
        } catch (IOException | ImageStorageException ex) {
            return Optional.empty();
        }
    }

    private DecodedImage decode(MultipartFile image) throws ImageStorageException {
        try (InputStream inputStream = image.getInputStream()) {
            return decode(inputStream);
        } catch (IOException ex) {
            throw new ImageStorageException("Image could not be read", ex);
        }
    }

    private DecodedImage decode(Path path) throws IOException, ImageStorageException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            return decode(inputStream);
        }
    }

    private DecodedImage decode(Resource resource) throws IOException, ImageStorageException {
        try (InputStream inputStream = resource.getInputStream()) {
            return decode(inputStream);
        }
    }

    private DecodedImage decode(InputStream inputStream) throws ImageStorageException, IOException {
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
            if (imageInputStream == null) {
                throw new ImageStorageException("Unsupported image type");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
            if (!readers.hasNext()) {
                throw new ImageStorageException("Unsupported image type");
            }

            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName().toLowerCase();
                if (!format.equals("jpeg") && !format.equals("jpg") && !format.equals("png")) {
                    throw new ImageStorageException("Unsupported image type");
                }
                reader.setInput(imageInputStream, true, true);
                BufferedImage bufferedImage = reader.read(0);
                if (bufferedImage.getWidth() > MAX_DIMENSION || bufferedImage.getHeight() > MAX_DIMENSION) {
                    throw new ImageStorageException("Image dimensions are too large");
                }
                return format.equals("png")
                        ? new DecodedImage(".png", "image/png")
                        : new DecodedImage(".jpg", "image/jpeg");
            } finally {
                reader.dispose();
            }
        }
    }

    private record DecodedImage(String extension, String mediaType) {
    }

    public record StoredImage(String filename, String imageUrl, String mediaType) {
    }

    public record ResolvedImage(Resource resource, String mediaType) {
    }

    public static class ImageStorageException extends Exception {
        public ImageStorageException(String message) {
            super(message);
        }

        public ImageStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
