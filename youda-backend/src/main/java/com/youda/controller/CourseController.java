package com.youda.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youda.common.Result;
import com.youda.dto.ProgressUpdateDTO;
import com.youda.service.CourseService;
import com.youda.vo.CourseDetailVO;
import com.youda.vo.CourseListVO;
import com.youda.vo.CourseOrderVO;
import com.youda.vo.VideoPlayVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping("/list")
    public Result<IPage<CourseListVO>> getCourseList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) String keyword) {
        return Result.success(courseService.getCourseList(current, size, subjectId, gradeId, keyword));
    }

    @GetMapping("/{courseId}")
    public Result<CourseDetailVO> getCourseDetail(@PathVariable Long courseId) {
        return Result.success(courseService.getCourseDetail(courseId));
    }

    @PostMapping("/{courseId}/purchase")
    public Result<Map<String, Object>> purchaseCourse(@PathVariable Long courseId) {
        return Result.success("Order created", courseService.purchaseCourse(courseId));
    }

    @GetMapping("/order/my")
    public Result<List<CourseOrderVO>> getMyCourseOrders() {
        return Result.success(courseService.getMyCourseOrders());
    }

    @PostMapping("/order/{orderId}/pay")
    public Result<Map<String, Object>> payCourseOrder(@PathVariable Long orderId) {
        return Result.success("Payment successful", courseService.payCourseOrder(orderId));
    }

    @PostMapping("/order/{orderId}/complete")
    public Result<Map<String, Object>> completeCourseOrder(@PathVariable Long orderId) {
        return Result.success("Order completed", courseService.completeCourseOrder(orderId));
    }

    @PostMapping("/order/{orderId}/receive")
    public Result<Map<String, Object>> confirmCourseOrderReceived(@PathVariable Long orderId) {
        return Result.success("Order completed", courseService.completeCourseOrder(orderId));
    }

    @GetMapping("/video/{videoId}")
    public Result<VideoPlayVO> getVideoPlayInfo(@PathVariable Long videoId) {
        return Result.success(courseService.getVideoPlayInfo(videoId));
    }

    @PostMapping("/video/{videoId}/progress")
    public Result<Map<String, Object>> updateProgress(
            @PathVariable Long videoId,
            @Valid @RequestBody ProgressUpdateDTO dto) {
        return Result.success("Update successful", courseService.updateProgress(videoId, dto));
    }

    @GetMapping("/learning-records")
    public Result<List<Map<String, Object>>> getLearningRecords() {
        return Result.success(courseService.getLearningRecords());
    }
}