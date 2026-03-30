package com.youda.vo;

import lombok.Data;

import java.util.List;

/**
 * 课程详情VO
 */
@Data
public class CourseDetailVO {

    private Long courseId;

    private String name;

    private String description;

    private String coverImage;

    private String teacherName;

    private String subjectName;

    private String gradeName;

    private Integer studentCount;

    private List<ChapterVO> chapters;

    private ProgressVO progress;
}
