package com.youda.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天列表VO
 */
@Data
public class ChatListVO {

    private String chatId;

    private String title;

    private String lastMessage;

    private LocalDateTime updateTime;
}
