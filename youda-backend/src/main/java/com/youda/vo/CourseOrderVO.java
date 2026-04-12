package com.youda.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseOrderVO {

    private Long orderId;

    private String orderNo;

    private Integer orderStatus;

    private String orderStatusLabel;

    private Long courseId;

    private String courseName;

    private String courseCoverImage;

    private Integer pointsCost;

    private LocalDateTime createTime;

    private LocalDateTime deliverTime;

    private LocalDateTime receiveTime;

    private Boolean canLearn;
}
