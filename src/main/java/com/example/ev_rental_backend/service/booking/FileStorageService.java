package com.example.ev_rental_backend.service.booking;

import com.example.ev_rental_backend.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    /**
     * Lưu file và trả về URL
     */
    public String storeFile(MultipartFile file, String subDirectory) {
        // Validate file
        validateFile(file);

        try {
            // Tạo thư mục nếu chưa tồn tại
            Path uploadPath = Paths.get(uploadDir, subDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Tạo tên file unique
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // Copy file vào thư mục
            Path targetLocation = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Trả về URL
            String fileUrl = baseUrl + "/" + subDirectory + "/" + newFilename;
            log.info("File stored successfully: {}", fileUrl);

            return fileUrl;

        } catch (IOException ex) {
            log.error("Could not store file", ex);
            throw new CustomException("Could not store file: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Xóa file
     */
    public void deleteFile(String fileUrl) {
        try {
            // Extract filename from URL
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            String subDirectory = fileUrl.substring(
                    fileUrl.indexOf(uploadDir) + uploadDir.length() + 1,
                    fileUrl.lastIndexOf("/")
            );

            Path filePath = Paths.get(uploadDir, subDirectory, filename);
            Files.deleteIfExists(filePath);

            log.info("File deleted: {}", fileUrl);

        } catch (IOException ex) {
            log.error("Could not delete file: {}", fileUrl, ex);
            throw new CustomException("Could not delete file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Validate file upload
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException("File is empty", HttpStatus.BAD_REQUEST);
        }

        // Check file size (max 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new CustomException("File size exceeds maximum limit of 10MB",
                    HttpStatus.BAD_REQUEST);
        }

        // Check file type (images only)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException("Only image files are allowed", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf(".");
        if (lastDot == -1) {
            return "";
        }
        return filename.substring(lastDot);
    }
}
