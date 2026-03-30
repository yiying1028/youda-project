package com.youda.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youda.entity.Resource;
import com.youda.vo.ResourceDetailVO;
import com.youda.vo.ResourceListVO;
import org.springframework.web.multipart.MultipartFile;

public interface ResourceService extends IService<Resource> {

    /**
     * 获取资料列表
     */
    IPage<ResourceListVO> getResourceList(Integer current, Integer size, Long subjectId, Long gradeId, String keyword);

    /**
     * 获取资料详情
     */
    ResourceDetailVO getResourceDetail(Long resourceId);

    /**
     * 上传资料
     */
    Long uploadResource(MultipartFile file, String name, String description, Long subjectId, Long gradeId);

    /**
     * 下载资料
     */
    Resource downloadResource(Long resourceId);

    /**
     * 删除资料
     */
    void deleteResource(Long resourceId);
}
