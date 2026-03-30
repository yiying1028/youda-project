package com.youda.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youda.common.Result;
import com.youda.entity.Resource;
import com.youda.service.ResourceService;
import com.youda.vo.ResourceDetailVO;
import com.youda.vo.ResourceListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    /**
     * 获取资料列表
     */
    @GetMapping("/list")
    public Result<IPage<ResourceListVO>> getResourceList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) String keyword) {
        IPage<ResourceListVO> page = resourceService.getResourceList(current, size, subjectId, gradeId, keyword);
        return Result.success(page);
    }

    /**
     * 获取资料详情
     */
    @GetMapping("/{resourceId}")
    public Result<ResourceDetailVO> getResourceDetail(@PathVariable Long resourceId) {
        ResourceDetailVO vo = resourceService.getResourceDetail(resourceId);
        return Result.success(vo);
    }

    /**
     * 上传资料
     */
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

    /**
     * 下载资料
     */
    @GetMapping("/{resourceId}/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadResource(@PathVariable Long resourceId) {
        Resource resource = resourceService.downloadResource(resourceId);

        try {
            Path filePath = Paths.get(resource.getFilePath());
            UrlResource urlResource = new UrlResource(filePath.toUri());

            String encodedFileName = URLEncoder.encode(resource.getName(), StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .body(urlResource);
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败");
        }
    }

    /**
     * 删除资料
     */
    @DeleteMapping("/{resourceId}")
    public Result<?> deleteResource(@PathVariable Long resourceId) {
        resourceService.deleteResource(resourceId);
        return Result.success("删除成功", null);
    }
}
