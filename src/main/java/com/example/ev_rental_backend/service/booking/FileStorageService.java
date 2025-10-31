package com.example.ev_rental_backend.service.booking;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.ev_rental_backend.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {
    @Autowired
    private Cloudinary cloudinary;

    /**
     * Lưu file và trả về URL
     */
    public String storeFile(MultipartFile file, String subDirectory) {
        // Validate file
        validateFile(file);

        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", subDirectory,
                            "resource_type", "auto" // Cho phép upload cả ảnh/video
                    )
            );

            String fileUrl = uploadResult.get("secure_url").toString();
            log.info("File uploaded to Cloudinary: {}", fileUrl);

            return fileUrl;

        } catch (IOException ex) {
            log.error("Could not upload file to Cloudinary", ex);
            throw new CustomException("Could not upload file to Cloudinary: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Xóa file
     */
    public void deleteFile(String fileUrl) {
        try {
            // Ví dụ fileUrl: https://res.cloudinary.com/demo/image/upload/v123456789/folder/myimage.jpg
            String publicId = extractPublicIdFromUrl(fileUrl);

            if (publicId == null) {
                throw new CustomException("Invalid Cloudinary URL", HttpStatus.BAD_REQUEST);
            }

            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("File deleted from Cloudinary: {}", result);

        } catch (IOException e) {
            log.error("Could not delete file from Cloudinary: {}", fileUrl, e);
            throw new CustomException("Could not delete file from Cloudinary", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Validate file upload
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException("File is empty", HttpStatus.BAD_REQUEST);
        }

        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new CustomException("File size exceeds maximum limit of 10MB", HttpStatus.BAD_REQUEST);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException("Only image files are allowed", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Tách publicId từ URL Cloudinary
     * Ví dụ:
     *  https://res.cloudinary.com/demo/image/upload/v123/folder/myimage.jpg
     *  → publicId = folder/myimage (không có .jpg)
     */
    private String extractPublicIdFromUrl(String fileUrl) {
        try {
            String withoutVersion = fileUrl.substring(fileUrl.indexOf("/upload/") + 8);
            // Bỏ "v123456789/" nếu có
            String[] parts = withoutVersion.split("/");
            if (parts[0].startsWith("v") && parts[0].length() > 1 && Character.isDigit(parts[0].charAt(1))) {
                withoutVersion = withoutVersion.substring(parts[0].length() + 1);
            }

            // Bỏ phần mở rộng (.jpg, .png, ...)
            int dotIndex = withoutVersion.lastIndexOf(".");
            return dotIndex != -1 ? withoutVersion.substring(0, dotIndex) : withoutVersion;

        } catch (Exception e) {
            log.error("Failed to extract publicId from URL: {}", fileUrl, e);
            return null;
        }
    }
}
