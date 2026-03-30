package com.youda.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youda.entity.Course;
import com.youda.vo.CourseListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    IPage<CourseListVO> selectCourseList(Page<CourseListVO> page,
                                          @Param("subjectId") Long subjectId,
                                          @Param("gradeId") Long gradeId,
                                          @Param("keyword") String keyword);
}
