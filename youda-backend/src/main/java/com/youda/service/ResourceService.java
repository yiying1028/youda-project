package com.youda.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youda.entity.Resource;
import com.youda.vo.ResourceDetailVO;
import com.youda.vo.ResourceListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ResourceService extends IService<Resource> {

    IPage<ResourceListVO> getResourceList(Integer current, Integer size, Long subjectId, Long gradeId, String keyword);

    ResourceDetailVO getResourceDetail(Long resourceId);

    Map<String, Object> purchaseResource(Long resourceId);

    Long uploadResource(MultipartFile file, String name, String description, Long subjectId, Long gradeId,
                        Integer requiresPoints, Integer pointsCost);

    Resource downloadResource(Long resourceId);

    void deleteResource(Long resourceId);
}