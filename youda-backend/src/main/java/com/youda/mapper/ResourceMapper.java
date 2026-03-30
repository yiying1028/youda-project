package com.youda.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youda.entity.Resource;
import com.youda.vo.ResourceListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {

    IPage<ResourceListVO> selectResourceList(Page<ResourceListVO> page,
                                              @Param("subjectId") Long subjectId,
                                              @Param("gradeId") Long gradeId,
                                              @Param("keyword") String keyword);
}
