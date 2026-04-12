package com.youda.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    private Boolean requiresPurchase;

    private BigDecimal priceAmount;

    private Boolean hasOrder;

    private Boolean purchased;

    private Boolean canLearn;

    private Integer orderStatus;

    private String orderStatusLabel;

    private LocalDateTime createTime;
}