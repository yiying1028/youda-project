package com.youda.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传工具类
 */
@Component
public class FileUtils {

    @Value("${upload.path}")
    private String uploadPath;

    /**
     * 上传文件
     * @param file 文件
     * @param type 类型：avatar/post/resource/video/chat
     * @return 文件访问路径
     */
    public String uploadFile(MultipartFile file, String type) throws IOException {
        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 生成新文件名
        String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

        // 按日期生成目录
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = "/" + type + "/" + datePath;
        String fullPath = uploadPath + relativePath;

        // 创建目录
        Path dirPath = Paths.get(fullPath);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 保存文件
        File destFile = new File(fullPath + "/" + newFilename);
        file.transferTo(destFile);

        // 返回相对路径
        return "/uploads" + relativePath + "/" + newFilename;
    }

    /**
     * 删除文件
     */
    public void deleteFile(String filePath) {
        Path resolvedPath = resolveStoragePath(filePath);
        if (resolvedPath != null) {
            File file = resolvedPath.toFile();
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 将存储路径解析为可直接访问的磁盘绝对路径。
     * 兼容两种历史格式：
     * 1. /uploads/...
     * 2. uploads/...
     */
    public Path resolveStoragePath(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return null;
        }

        Path basePath = Paths.get(uploadPath).toAbsolutePath().normalize();

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

        return path.toAbsolutePath().normalize();
    }

    /**
     * 获取文件扩展名
     */
    public String getExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }
}
