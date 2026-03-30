package com.youda.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 年级实体
 */
@Data
@TableName("grade")
public class Grade {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String level;

    private Integer sort;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
