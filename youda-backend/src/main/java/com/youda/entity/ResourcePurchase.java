package com.youda.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resource_purchase")
public class ResourcePurchase {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long resourceId;

    private Integer pointsCost;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}