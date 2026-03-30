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

/**
 * 课程服务接口
 */
public interface CourseService extends IService<Course> {

    /** 获取课程分页列表（支持学科/年级/关键词筛选） */
    IPage<CourseListVO> getCourseList(Integer current, Integer size, Long subjectId, Long gradeId, String keyword);

    /** 获取课程详情（含章节、视频列表、当前用户学习进度） */
    CourseDetailVO getCourseDetail(Long courseId);

    /** 获取视频播放信息（含上次播放位置、上下视频ID） */
    VideoPlayVO getVideoPlayInfo(Long videoId);

    /** 更新视频学习进度 */
    Map<String, Object> updateProgress(Long videoId, ProgressUpdateDTO dto);

    /**
     * 获取当前用户的学习记录（每门课程的最新进度）
     * @return 包含courseId、courseName、coverImage、progress(%)、lastStudyTime的列表
     */
    List<Map<String, Object>> getLearningRecords();
}
