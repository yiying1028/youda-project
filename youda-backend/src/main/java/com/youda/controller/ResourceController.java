package com.youda.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youda.common.Result;
import com.youda.entity.Resource;
import com.youda.service.ResourceService;
import com.youda.utils.FileUtils;
import com.youda.vo.ResourceDetailVO;
import com.youda.vo.ResourceListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private FileUtils fileUtils;

    @GetMapping("/list")
    public Result<IPage<ResourceListVO>> getResourceList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) String keyword) {
        return Result.success(resourceService.getResourceList(current, size, subjectId, gradeId, keyword));
    }

    @GetMapping("/{resourceId}")
    public Result<ResourceDetailVO> getResourceDetail(@PathVariable Long resourceId) {
        return Result.success(resourceService.getResourceDetail(resourceId));
    }

    @PostMapping("/{resourceId}/purchase")
    public Result<Map<String, Object>> purchaseResource(@PathVariable Long resourceId) {
        return Result.success("购买成功", resourceService.purchaseResource(resourceId));
    }

    @PostMapping("/upload")
    public Result<Map<String, Long>> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam Long subjectId,
            @RequestParam Long gradeId) {
        Long resourceId = resourceService.uploadResource(file, name, description, subjectId, gradeId);
        Map<String, Long> data = new HashMap<>();
        data.put("resourceId", resourceId);
        return Result.success("上传成功", data);
    }

    @GetMapping("/{resourceId}/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadResource(@PathVariable Long resourceId) {
        Resource resource = resourceService.downloadResource(resourceId);

        try {
            Path filePath = fileUtils.resolveStoragePath(resource.getFilePath());
            UrlResource urlResource = new UrlResource(filePath.toUri());

            String downloadFileName = resource.getFileName() != null && !resource.getFileName().isBlank()
                    ? resource.getFileName()
                    : resource.getName();
            String encodedFileName = URLEncoder.encode(downloadFileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .body(urlResource);
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败");
        }
    }

    @DeleteMapping("/{resourceId}")
    public Result<?> deleteResource(@PathVariable Long resourceId) {
        resourceService.deleteResource(resourceId);
        return Result.success("删除成功", null);
    }
}