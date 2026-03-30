package com.youda.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习资料实体
 */
@Data
@TableName("resource")
public class Resource {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String fileName;

    private String filePath;

    private String fileType;

    private Long fileSize;

    private Long subjectId;

    private Long gradeId;

    private Integer downloadCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer isDeleted;
}
