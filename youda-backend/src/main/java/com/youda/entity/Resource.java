package com.youda.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

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

    private Long userId;

    private Integer downloadCount;

    private Integer requiresPoints;

    private Integer pointsCost;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer isDeleted;
}