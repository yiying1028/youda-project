package com.youda.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youda.dto.ProgressUpdateDTO;
import com.youda.entity.Course;
import com.youda.vo.CourseDetailVO;
import com.youda.vo.CourseListVO;
import com.youda.vo.VideoPlayVO;

import java.util.List;
import java.util.Map;

public interface CourseService extends IService<Course> {

    IPage<CourseListVO> getCourseList(Integer current, Integer size, Long subjectId, Long gradeId, String keyword);

    CourseDetailVO getCourseDetail(Long courseId);

    Map<String, Object> purchaseCourse(Long courseId);

    VideoPlayVO getVideoPlayInfo(Long videoId);

    Map<String, Object> updateProgress(Long videoId, ProgressUpdateDTO dto);

    List<Map<String, Object>> getLearningRecords();
}