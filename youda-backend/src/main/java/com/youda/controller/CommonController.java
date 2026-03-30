package com.youda.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youda.common.Result;
import com.youda.entity.Announcement;
import com.youda.entity.Category;
import com.youda.entity.Grade;
import com.youda.entity.Subject;
import com.youda.mapper.AnnouncementMapper;
import com.youda.mapper.CategoryMapper;
import com.youda.mapper.GradeMapper;
import com.youda.mapper.SubjectMapper;
import com.youda.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class CommonController {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Autowired
    private FileUtils fileUtils;

    @GetMapping({"/api/common/categories", "/api/category/list"})
    public Result<List<Map<String, Object>>> getCategories() {
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSort)
        );

        List<Map<String, Object>> list = new ArrayList<>();
        for (Category category : categories) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", category.getId());
            map.put("categoryId", category.getId());
            map.put("name", category.getName());
            list.add(map);
        }
        return Result.success(list);
    }

    @GetMapping({"/api/common/subjects", "/api/subject/list"})
    public Result<List<Map<String, Object>>> getSubjects() {
        List<Subject> subjects = subjectMapper.selectList(
                new LambdaQueryWrapper<Subject>()
                        .eq(Subject::getStatus, 1)
                        .orderByAsc(Subject::getSort)
        );

        List<Map<String, Object>> list = new ArrayList<>();
        for (Subject subject : subjects) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", subject.getId());
            map.put("subjectId", subject.getId());
            map.put("name", subject.getName());
            list.add(map);
        }
        return Result.success(list);
    }

    @GetMapping({"/api/common/grades", "/api/grade/list"})
    public Result<List<Map<String, Object>>> getGrades() {
        List<Grade> grades = gradeMapper.selectList(
                new LambdaQueryWrapper<Grade>()
                        .eq(Grade::getStatus, 1)
                        .orderByAsc(Grade::getSort)
        );

        List<Map<String, Object>> list = new ArrayList<>();
        for (Grade grade : grades) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", grade.getId());
            map.put("gradeId", grade.getId());
            map.put("name", grade.getName());
            map.put("level", grade.getLevel());
            list.add(map);
        }
        return Result.success(list);
    }

    @GetMapping({"/api/common/announcements", "/api/announcement/list"})
    public Result<List<Map<String, Object>>> getAnnouncements() {
        List<Announcement> announcements = announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>()
                        .eq(Announcement::getStatus, 1)
                        .orderByDesc(Announcement::getCreateTime)
                        .last("LIMIT 10")
        );

        List<Map<String, Object>> list = new ArrayList<>();
        for (Announcement announcement : announcements) {
            Map<String, Object> map = new HashMap<>();
            map.put("announcementId", announcement.getId());
            map.put("title", announcement.getTitle());
            map.put("content", announcement.getContent());
            map.put("createTime", announcement.getCreateTime());
            list.add(map);
        }
        return Result.success(list);
    }

    @PostMapping("/api/common/upload")
    public Result<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "post") String type) throws IOException {
        String url = fileUtils.uploadFile(file, type);
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success("上传成功", data);
    }
}
