package com.youda.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youda.common.Result;
import com.youda.dto.ProgressUpdateDTO;
import com.youda.service.CourseService;
import com.youda.vo.CourseDetailVO;
import com.youda.vo.CourseListVO;
import com.youda.vo.VideoPlayVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 课程Controller
 * 处理课程列表、详情、视频播放、学习进度、学习记录等
 */
@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    // ============ 课程浏览 ============

    /**
     * 获取课程分页列表
     * 支持学科/年级/关键词筛选
     */
    @GetMapping("/list")
    public Result<IPage<CourseListVO>> getCourseList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) String keyword) {
        IPage<CourseListVO> page = courseService.getCourseList(current, size, subjectId, gradeId, keyword);
        return Result.success(page);
    }

    /**
     * 获取课程详情（含章节目录、学习进度）
     */
    @GetMapping("/{courseId}")
    public Result<CourseDetailVO> getCourseDetail(@PathVariable Long courseId) {
        CourseDetailVO vo = courseService.getCourseDetail(courseId);
        return Result.success(vo);
    }

    // ============ 视频播放 ============

    /**
     * 获取视频播放信息（视频URL、上次进度、上下节视频ID）
     */
    @GetMapping("/video/{videoId}")
    public Result<VideoPlayVO> getVideoPlayInfo(@PathVariable Long videoId) {
        VideoPlayVO vo = courseService.getVideoPlayInfo(videoId);
        return Result.success(vo);
    }

    /**
     * 更新学习进度（前端定时上报当前播放位置）
     */
    @PostMapping("/video/{videoId}/progress")
    public Result<Map<String, Object>> updateProgress(
            @PathVariable Long videoId,
            @Valid @RequestBody ProgressUpdateDTO dto) {
        Map<String, Object> result = courseService.updateProgress(videoId, dto);
        return Result.success("更新成功", result);
    }

    // ============ 学习记录 ============

    /**
     * 获取当前用户的课程学习记录
     * 返回每门课的学习进度和最近学习时间
     */
    @GetMapping("/learning-records")
    public Result<List<Map<String, Object>>> getLearningRecords() {
        List<Map<String, Object>> records = courseService.getLearningRecords();
        return Result.success(records);
    }
}
