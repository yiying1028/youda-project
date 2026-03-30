package com.youda.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程列表VO
 */
@Data
public class CourseListVO {

    private Long courseId;

    private String name;

    private String description;

    private String coverImage;

    private String teacherName;

    private String subjectName;

    private String gradeName;

    private Integer chapterCount;

    private Integer studentCount;

    private LocalDateTime createTime;
}
