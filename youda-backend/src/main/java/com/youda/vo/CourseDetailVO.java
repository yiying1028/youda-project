package com.youda.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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

    private Boolean requiresPoints;

    private Integer pointsCost;

    private Boolean purchased;

    private Boolean canLearn;

    private Long orderId;

    private String orderNo;

    private Integer orderStatus;

    private String orderStatusLabel;

    private LocalDateTime orderDeliverTime;

    private LocalDateTime orderReceiveTime;

    private List<ChapterVO> chapters;

    private ProgressVO progress;
}
