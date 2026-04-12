package com.youda.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseOrderVO {

    private Long orderId;

    private String orderNo;

    private Long userId;

    private String username;

    private String nickname;

    private Integer orderStatus;

    private String orderStatusLabel;

    private Long courseId;

    private String courseName;

    private String courseCoverImage;

    private BigDecimal paymentAmount;

    private LocalDateTime createTime;

    private LocalDateTime paidTime;

    private LocalDateTime completedTime;

    private Boolean canPay;

    private Boolean canLearn;
}