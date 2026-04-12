package com.youda.vo;

import lombok.Data;

import java.math.BigDecimal;
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

    private Boolean requiresPurchase;

    private BigDecimal priceAmount;

    private Boolean hasOrder;

    private Boolean purchased;

    private Boolean canLearn;

    private Long orderId;

    private String orderNo;

    private Integer orderStatus;

    private String orderStatusLabel;

    private LocalDateTime orderCreateTime;

    private LocalDateTime orderPaidTime;

    private LocalDateTime orderCompletedTime;

    private List<ChapterVO> chapters;

    private ProgressVO progress;
}