package com.youda.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("course_purchase")
public class CoursePurchase {

    public static final int STATUS_PENDING_PAYMENT = 0;
    public static final int STATUS_PAID = 1;
    public static final int STATUS_COMPLETED = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long courseId;

    private Integer pointsCost;

    private BigDecimal paymentAmount;

    private Integer status;

    private LocalDateTime paidTime;

    private LocalDateTime completedTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}