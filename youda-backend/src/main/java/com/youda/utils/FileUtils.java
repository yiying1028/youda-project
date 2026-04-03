package com.youda.utils;

import com.youda.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Component
public class FileUtils {

    private static final Set<String> ALLOWED_UPLOAD_TYPES = Set.of("avatar", "post", "resource", "video", "chat");
    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Value("${upload.path}")
    private String uploadPath;

    public String uploadFile(MultipartFile file, String type) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "上传文件不能为空");
        }

        String normalizedType = normalizeType(type);
        String extension = extractExtension(file.getOriginalFilename());
        String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;
        String datePath = LocalDate.now().format(DATE_PATH_FORMATTER);

        Path basePath = getUploadBasePath();
        Path dirPath = basePath.resolve(normalizedType).resolve(datePath).normalize();
        Files.createDirectories(dirPath);

        Path destPath = dirPath.resolve(newFilename).normalize();
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/" + normalizedType + "/" + datePath + "/" + newFilename;
    }

    public void deleteFile(String filePath) {
        Path resolvedPath = resolveStoragePath(filePath);
        if (resolvedPath != null) {
            try {
                Files.deleteIfExists(resolvedPath);
            } catch (IOException ignored) {
            }
        }
    }

    public Path resolveStoragePath(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return null;
        }

        Path basePath = getUploadBasePath();

        if ("/uploads".equals(filePath)) {
            return basePath;
        }

        if (filePath.startsWith("/uploads/")) {
            return basePath.resolve(filePath.substring("/uploads/".length())).normalize();
        }

        if (filePath.startsWith("uploads/")) {
            return basePath.resolve(filePath.substring("uploads/".length())).normalize();
        }

        Path path = Paths.get(filePath);
        if (path.isAbsolute()) {
            return path.normalize();
        }

        return basePath.resolve(path).normalize();
    }

    public String getExtension(String filename) {
        String extension = extractExtension(filename);
        if (extension.isEmpty()) {
            return "";
        }
        return extension.substring(1).toLowerCase();
    }

    private Path getUploadBasePath() {
        return Paths.get(uploadPath).toAbsolutePath().normalize();
    }

    private String normalizeType(String type) {
        String normalizedType = (type == null || type.isBlank()) ? "post" : type.trim().toLowerCase();
        if (!ALLOWED_UPLOAD_TYPES.contains(normalizedType)) {
            throw new BusinessException(400, "不支持的上传类型: " + normalizedType);
        }
        return normalizedType;
    }

    private String extractExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.'));
        }
        return "";
    }
}
