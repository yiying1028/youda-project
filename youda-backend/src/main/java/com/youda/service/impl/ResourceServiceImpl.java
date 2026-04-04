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

    /**
     * 分页查询资料列表，并补齐当前用户的购买/下载权限状态。
     */
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
    /**
     * 查询资料详情，并返回当前登录用户是否已购买、是否可下载。
     */
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
    /**
     * 购买资料。
     * 这里会先扣积分，再写购买记录，后续下载直接按购买记录放行。
     */
    public Map<String, Object> purchaseResource(Long resourceId) {
        Long userId = UserContext.getCurrentUserId();
        Resource resource = requireResource(resourceId);
        if (!isPaidResource(resource)) {
            throw new BusinessException(400, "Free resource does not need to be purchased");
        }
        if (hasResourcePurchase(userId, resourceId)) {
            throw new BusinessException(400, "Resource already purchased");
        }

        int cost = normalizeCost(resource.getPointsCost());
        pointsService.spendPoints(userId, ACTION_RESOURCE_PURCHASE, String.valueOf(resourceId), cost,
                "Purchase resource: " + resource.getName());

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
    /**
     * 上传资料并保存付费配置。
     * 价格字段会先做归一化，避免出现免费资料却带价格的脏数据。
     */
    public Long uploadResource(MultipartFile file, String name, String description, Long subjectId, Long gradeId,
                               Integer requiresPoints, Integer pointsCost) {
        try {
            String filePath = fileUtils.uploadFile(file, "resource");
            String fileType = fileUtils.getExtension(file.getOriginalFilename());
            int normalizedRequiresPoints = normalizeRequiresPoints(requiresPoints, pointsCost);
            int normalizedPointsCost = normalizePointsCost(normalizedRequiresPoints, pointsCost);

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
            resource.setRequiresPoints(normalizedRequiresPoints);
            resource.setPointsCost(normalizedPointsCost);

            this.save(resource);
            return resource.getId();
        } catch (IOException e) {
            throw new BusinessException(400, "File upload failed");
        }
    }

    @Override
    /**
     * 下载资料。
     * 如果是付费资料，会先校验当前用户是否已购买。
     */
    public Resource downloadResource(Long resourceId) {
        Resource resource = requireResource(resourceId);
        ensureResourceAccessible(resource);

        Path filePath = fileUtils.resolveStoragePath(resource.getFilePath());
        if (filePath == null || !Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new BusinessException(400, "Resource file does not exist");
        }

        Resource updateResource = new Resource();
        updateResource.setId(resourceId);
        updateResource.setDownloadCount((resource.getDownloadCount() == null ? 0 : resource.getDownloadCount()) + 1);
        this.updateById(updateResource);

        return resource;
    }

    @Override
    /**
     * 删除资料记录和物理文件。
     */
    public void deleteResource(Long resourceId) {
        Resource resource = requireResource(resourceId);
        fileUtils.deleteFile(resource.getFilePath());
        this.removeById(resourceId);
    }

    /**
     * 给列表项补齐下载权限相关字段，前端列表页直接用这些字段渲染按钮和价格。
     */
    private void fillResourceAccessState(ResourceListVO item, Long currentUserId) {
        boolean requiresPoints = Boolean.TRUE.equals(item.getRequiresPoints()) && normalizeCost(item.getPointsCost()) > 0;
        boolean purchased = requiresPoints && currentUserId != null && hasResourcePurchase(currentUserId, item.getResourceId());
        item.setRequiresPoints(requiresPoints);
        item.setPointsCost(requiresPoints ? normalizeCost(item.getPointsCost()) : 0);
        item.setPurchased(purchased);
        item.setCanDownload(!requiresPoints || purchased);
    }

    /**
     * 下载前统一校验资料访问权限。
     */
    private void ensureResourceAccessible(Resource resource) {
        if (!isPaidResource(resource)) {
            return;
        }
        Long currentUserId = UserContext.getCurrentUserIdOrNull();
        if (currentUserId == null) {
            throw new BusinessException(401, "Please log in before downloading this resource");
        }
        if (!hasResourcePurchase(currentUserId, resource.getId())) {
            throw new BusinessException(400, "Please purchase this resource first");
        }
    }

    /**
     * 判断用户是否已经买过当前资料。
     */
    private boolean hasResourcePurchase(Long userId, Long resourceId) {
        return resourcePurchaseMapper.selectCount(
                new LambdaQueryWrapper<ResourcePurchase>()
                        .eq(ResourcePurchase::getUserId, userId)
                        .eq(ResourcePurchase::getResourceId, resourceId)
        ) > 0;
    }

    /**
     * 判断资料是否应该被视为付费资料。
     */
    private boolean isPaidResource(Resource resource) {
        return resource.getRequiresPoints() != null && resource.getRequiresPoints() == 1 && normalizeCost(resource.getPointsCost()) > 0;
    }

    /**
     * 积分价格归一化，负数统一按 0 处理。
     */
    private int normalizeCost(Integer pointsCost) {
        return pointsCost == null || pointsCost < 0 ? 0 : pointsCost;
    }

    /**
     * 是否付费的归一化规则：
     * 只有勾选付费且价格大于 0，才真正落库为付费资料。
     */
    private int normalizeRequiresPoints(Integer requiresPoints, Integer pointsCost) {
        return requiresPoints != null && requiresPoints == 1 && normalizeCost(pointsCost) > 0 ? 1 : 0;
    }

    /**
     * 价格归一化规则：免费资料一律存 0。
     */
    private int normalizePointsCost(Integer requiresPoints, Integer pointsCost) {
        return requiresPoints != null && requiresPoints == 1 ? normalizeCost(pointsCost) : 0;
    }

    /**
     * 查询资料，不存在就直接抛业务异常。
     */
    private Resource requireResource(Long resourceId) {
        Resource resource = this.getById(resourceId);
        if (resource == null) {
            throw new BusinessException(400, "Resource not found");
        }
        return resource;
    }
}
