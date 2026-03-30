package com.youda.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话消息实体
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String chatId;

    /**
     * 角色：user/assistant
     */
    private String role;

    private String content;

    private String image;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
