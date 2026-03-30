package com.youda.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息VO
 */
@Data
public class ChatMessageVO {

    private String messageId;

    private String role;

    private String content;

    private String image;

    private LocalDateTime createTime;
}
