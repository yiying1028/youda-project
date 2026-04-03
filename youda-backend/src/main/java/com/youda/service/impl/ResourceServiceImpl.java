package com.youda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youda.common.BusinessException;
import com.youda.entity.Grade;
import com.youda.entity.Resource;
import com.youda.entity.ResourcePurchase;
import com.youda.entity.Subject;
import com.youda.mapper.GradeMapper;
import com.youda.mapper.ResourceMapper;
import com.youda.mapper.ResourcePurchaseMapper;
import com.youda.mapper.SubjectMapper;
import com.youda.service.PointsService;
import com.youda.service.ResourceService;
import com.youda.utils.FileUtils;
import com.youda.utils.UserContext;
import com.youda.vo.ResourceDetailVO;
import com.youda.vo.ResourceListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {

    private static final String ACTION_RESOURCE_PURCHASE = "RESOURCE_PURCHASE";

    @Autowired
    private FileUtils fileUtils;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private ResourcePurchaseMapper resourcePurchaseMapper;

    @Autowired
    private PointsService pointsService;

    @Override
    public IPage<ResourceListVO> getResourceList(Integer current, Integer size, Long subjectId, Long gradeId, String keyword) {
        Page<ResourceListVO> page = new Page<>(current, size);
        IPage<ResourceListVO> result = baseMapper.selectResourceList(page, subjectId, gradeId, keyword);
        Long currentUserId = UserContext.getCurrentUserIdOrNull();
        for (ResourceListVO item : result.getRecords()) {
            fillResourceAccessState(item, currentUserId);
        }
        return result;
    }

    @Override
    public ResourceDetailVO getResourceDetail(Long resourceId) {
        Resource resource = requireResource(resourceId);
        ResourceDetailVO vo = new ResourceDetailVO();
        vo.setResourceId(resource.getId());
        vo.setName(resource.getName());
        vo.setDescription(resource.getDescription());
        vo.setSubjectId(resource.getSubjectId());
        vo.setGradeId(resource.getGradeId());
        vo.setFileType(resource.getFileType());
        vo.setFileSize(resource.getFileSize());
        vo.setDownloadCount(resource.getDownloadCount());
        vo.setCreateTime(resource.getCreateTime());
        vo.setRequiresPoints(isPaidResource(resource));
        vo.setPointsCost(isPaidResource(resource) ? normalizeCost(resource.getPointsCost()) : 0);

        Subject subject = subjectMapper.selectById(resource.getSubjectId());
        if (subject != null) {
            vo.setSubjectName(subject.getName());
        }

        Grade grade = gradeMapper.selectById(resource.getGradeId());
        if (grade != null) {
            vo.setGradeName(grade.getName());
        }

        Long currentUserId = UserContext.getCurrentUserIdOrNull();
        boolean purchased = currentUserId != null && hasResourcePurchase(currentUserId, resourceId);
        vo.setPurchased(purchased);
        vo.setCanDownload(!isPaidResource(resource) || purchased);
        return vo;
    }

    @Override
    @Transactional
    public Map<String, Object> purchaseResource(Long resourceId) {
        Long userId = UserContext.getCurrentUserId();
        Resource resource = requireResource(resourceId);
        if (!isPaidResource(resource)) {
            throw new BusinessException(400, "免费资料无需购买");
        }
        if (hasResourcePurchase(userId, resourceId)) {
            throw new BusinessException(400, "该资料已购买");
        }

        int cost = normalizeCost(resource.getPointsCost());
        pointsService.spendPoints(userId, ACTION_RESOURCE_PURCHASE, String.valueOf(resourceId), cost, "购买资料：" + resource.getName());

        ResourcePurchase purchase = new ResourcePurchase();
        purchase.setUserId(userId);
        purchase.setResourceId(resourceId);
        purchase.setPointsCost(cost);
        resourcePurchaseMapper.insert(purchase);

        Map<String, Object> result = new HashMap<>();
        result.put("resourceId", resourceId);
        result.put("pointsCost", cost);
        return result;
    }

    @Override
    public Long uploadResource(MultipartFile file, String name, String description, Long subjectId, Long gradeId) {
        try {
            String filePath = fileUtils.uploadFile(file, "resource");
            String fileType = fileUtils.getExtension(file.getOriginalFilename());

            Resource resource = new Resource();
            resource.setName(name);
            resource.setDescription(description);
            resource.setFileName(file.getOriginalFilename());
            resource.setFilePath(filePath);
            resource.setFileType(fileType);
            resource.setFileSize(file.getSize());
            resource.setSubjectId(subjectId);
            resource.setGradeId(gradeId);
            resource.setUserId(UserContext.getCurrentUserIdOrNull());
            resource.setDownloadCount(0);
            resource.setRequiresPoints(0);
            resource.setPointsCost(0);

            this.save(resource);
            return resource.getId();
        } catch (IOException e) {
            throw new BusinessException(400, "文件上传失败");
        }
    }

    @Override
    public Resource downloadResource(Long resourceId) {
        Resource resource = requireResource(resourceId);
        ensureResourceAccessible(resource);

        Path filePath = fileUtils.resolveStoragePath(resource.getFilePath());
        if (filePath == null || !Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new BusinessException(400, "资料文件不存在，暂时无法下载");
        }

        Resource updateResource = new Resource();
        updateResource.setId(resourceId);
        updateResource.setDownloadCount((resource.getDownloadCount() == null ? 0 : resource.getDownloadCount()) + 1);
        this.updateById(updateResource);

        return resource;
    }

    @Override
    public void deleteResource(Long resourceId) {
        Resource resource = requireResource(resourceId);
        fileUtils.deleteFile(resource.getFilePath());
        this.removeById(resourceId);
    }

    private void fillResourceAccessState(ResourceListVO item, Long currentUserId) {
        boolean requiresPoints = Boolean.TRUE.equals(item.getRequiresPoints()) && normalizeCost(item.getPointsCost()) > 0;
        boolean purchased = requiresPoints && currentUserId != null && hasResourcePurchase(currentUserId, item.getResourceId());
        item.setRequiresPoints(requiresPoints);
        item.setPointsCost(requiresPoints ? normalizeCost(item.getPointsCost()) : 0);
        item.setPurchased(purchased);
        item.setCanDownload(!requiresPoints || purchased);
    }

    private void ensureResourceAccessible(Resource resource) {
        if (!isPaidResource(resource)) {
            return;
        }
        Long currentUserId = UserContext.getCurrentUserIdOrNull();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录并购买该资料");
        }
        if (!hasResourcePurchase(currentUserId, resource.getId())) {
            throw new BusinessException(400, "请先购买该资料后再下载");
        }
    }

    private boolean hasResourcePurchase(Long userId, Long resourceId) {
        return resourcePurchaseMapper.selectCount(
                new LambdaQueryWrapper<ResourcePurchase>()
                        .eq(ResourcePurchase::getUserId, userId)
                        .eq(ResourcePurchase::getResourceId, resourceId)
        ) > 0;
    }

    private boolean isPaidResource(Resource resource) {
        return resource.getRequiresPoints() != null && resource.getRequiresPoints() == 1 && normalizeCost(resource.getPointsCost()) > 0;
    }

    private int normalizeCost(Integer pointsCost) {
        return pointsCost == null || pointsCost < 0 ? 0 : pointsCost;
    }

    private Resource requireResource(Long resourceId) {
        Resource resource = this.getById(resourceId);
        if (resource == null) {
            throw new BusinessException(400, "资料不存在");
        }
        return resource;
    }
}