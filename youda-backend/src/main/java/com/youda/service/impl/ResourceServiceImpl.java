package com.youda.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youda.common.BusinessException;
import com.youda.entity.Grade;
import com.youda.entity.Resource;
import com.youda.entity.Subject;
import com.youda.mapper.GradeMapper;
import com.youda.mapper.ResourceMapper;
import com.youda.mapper.SubjectMapper;
import com.youda.service.ResourceService;
import com.youda.utils.FileUtils;
import com.youda.vo.ResourceDetailVO;
import com.youda.vo.ResourceListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {

    @Autowired
    private FileUtils fileUtils;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private GradeMapper gradeMapper;

    @Override
    public IPage<ResourceListVO> getResourceList(Integer current, Integer size, Long subjectId, Long gradeId, String keyword) {
        Page<ResourceListVO> page = new Page<>(current, size);
        return baseMapper.selectResourceList(page, subjectId, gradeId, keyword);
    }

    @Override
    public ResourceDetailVO getResourceDetail(Long resourceId) {
        Resource resource = this.getById(resourceId);
        if (resource == null) {
            throw new BusinessException("资料不存在");
        }

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

        Subject subject = subjectMapper.selectById(resource.getSubjectId());
        if (subject != null) {
            vo.setSubjectName(subject.getName());
        }

        Grade grade = gradeMapper.selectById(resource.getGradeId());
        if (grade != null) {
            vo.setGradeName(grade.getName());
        }

        return vo;
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
            resource.setDownloadCount(0);

            this.save(resource);
            return resource.getId();
        } catch (IOException e) {
            throw new BusinessException("文件上传失败");
        }
    }

    @Override
    public Resource downloadResource(Long resourceId) {
        Resource resource = this.getById(resourceId);
        if (resource == null) {
            throw new BusinessException("资料不存在");
        }

        // 增加下载次数
        Resource updateResource = new Resource();
        updateResource.setId(resourceId);
        updateResource.setDownloadCount(resource.getDownloadCount() + 1);
        this.updateById(updateResource);

        return resource;
    }

    @Override
    public void deleteResource(Long resourceId) {
        Resource resource = this.getById(resourceId);
        if (resource == null) {
            throw new BusinessException("资料不存在");
        }

        // 删除文件
        fileUtils.deleteFile(resource.getFilePath());

        this.removeById(resourceId);
    }
}
