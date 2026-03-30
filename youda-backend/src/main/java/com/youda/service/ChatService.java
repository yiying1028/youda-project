package com.youda.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youda.dto.ChatMessageDTO;
import com.youda.entity.Chat;
import com.youda.vo.ChatListVO;
import com.youda.vo.ChatMessageVO;

import java.util.List;
import java.util.Map;

public interface ChatService extends IService<Chat> {

    /**
     * 创建对话
     */
    String createChat();

    /**
     * 获取对话列表
     */
    IPage<ChatListVO> getChatList(Integer current, Integer size);

    /**
     * 获取对话详情
     */
    Map<String, Object> getChatDetail(String chatId);

    /**
     * 发送消息
     */
    ChatMessageVO sendMessage(String chatId, ChatMessageDTO dto);

    /**
     * 删除对话
     */
    void deleteChat(String chatId);
}
