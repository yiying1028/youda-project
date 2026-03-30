package com.youda.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youda.common.BusinessException;
import com.youda.dto.ChatMessageDTO;
import com.youda.entity.Chat;
import com.youda.entity.ChatMessage;
import com.youda.mapper.ChatMapper;
import com.youda.mapper.ChatMessageMapper;
import com.youda.service.ChatService;
import com.youda.service.ai.QwenChatClient;
import com.youda.utils.UserContext;
import com.youda.vo.ChatListVO;
import com.youda.vo.ChatMessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat> implements ChatService {

    @Autowired
    private ChatMessageMapper messageMapper;

    @Autowired
    private QwenChatClient qwenChatClient;

    @Override
    public String createChat() {
        Long userId = UserContext.getCurrentUserId();

        Chat chat = new Chat();
        chat.setChatId("chat_" + IdUtil.simpleUUID());
        chat.setUserId(userId);

        this.save(chat);
        return chat.getChatId();
    }

    @Override
    public IPage<ChatListVO> getChatList(Integer current, Integer size) {
        Long userId = UserContext.getCurrentUserId();

        Page<Chat> page = new Page<>(current, size);
        LambdaQueryWrapper<Chat> wrapper = new LambdaQueryWrapper<Chat>()
                .eq(Chat::getUserId, userId)
                .orderByDesc(Chat::getUpdateTime);

        IPage<Chat> chatPage = this.page(page, wrapper);
        return chatPage.convert(chat -> {
            ChatListVO vo = new ChatListVO();
            vo.setChatId(chat.getChatId());
            vo.setTitle(chat.getTitle());
            vo.setUpdateTime(chat.getUpdateTime());

            ChatMessage lastMessage = messageMapper.selectOne(
                    new LambdaQueryWrapper<ChatMessage>()
                            .eq(ChatMessage::getChatId, chat.getChatId())
                            .eq(ChatMessage::getRole, "user")
                            .orderByDesc(ChatMessage::getCreateTime)
                            .last("LIMIT 1")
            );
            if (lastMessage != null && StrUtil.isNotBlank(lastMessage.getContent())) {
                String preview = lastMessage.getContent();
                vo.setLastMessage(preview.length() > 50 ? preview.substring(0, 50) + "..." : preview);
            }

            return vo;
        });
    }

    @Override
    public Map<String, Object> getChatDetail(String chatId) {
        Chat chat = getOwnedChat(chatId);

        List<ChatMessage> messages = messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getChatId, chat.getChatId())
                        .orderByAsc(ChatMessage::getCreateTime)
        );

        List<ChatMessageVO> messageVOList = messages.stream().map(this::toMessageVO).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("chatId", chatId);
        result.put("messages", messageVOList);
        return result;
    }

    @Override
    public ChatMessageVO sendMessage(String chatId, ChatMessageDTO dto) {
        Chat chat = getOwnedChat(chatId);

        String content = dto == null ? "" : StrUtil.trimToEmpty(dto.getContent());
        String image = dto == null ? null : (StrUtil.isBlank(dto.getImage()) ? null : dto.getImage().trim());
        if (StrUtil.isBlank(content) && StrUtil.isBlank(image)) {
            throw new BusinessException("消息内容不能为空");
        }

        ChatMessage userMessage = new ChatMessage();
        userMessage.setChatId(chatId);
        userMessage.setRole("user");
        userMessage.setContent(content);
        userMessage.setImage(image);
        messageMapper.insert(userMessage);

        if (StrUtil.isBlank(chat.getTitle())) {
            chat.setTitle(buildChatTitle(content, image));
        }
        chat.setUpdateTime(LocalDateTime.now());
        this.updateById(chat);

        List<ChatMessage> historyMessages = messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getChatId, chatId)
                        .orderByAsc(ChatMessage::getCreateTime)
        );
        String aiResponse = qwenChatClient.generateReply(historyMessages);

        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setChatId(chatId);
        aiMessage.setRole("assistant");
        aiMessage.setContent(aiResponse);
        messageMapper.insert(aiMessage);

        chat.setUpdateTime(LocalDateTime.now());
        this.updateById(chat);

        return toMessageVO(aiMessage);
    }

    @Override
    public void deleteChat(String chatId) {
        Chat chat = getOwnedChat(chatId);

        messageMapper.delete(new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getChatId, chatId));
        this.removeById(chat.getId());
    }

    private Chat getOwnedChat(String chatId) {
        Long userId = UserContext.getCurrentUserId();
        Chat chat = this.lambdaQuery()
                .eq(Chat::getChatId, chatId)
                .eq(Chat::getUserId, userId)
                .one();
        if (chat == null) {
            throw new BusinessException("对话不存在");
        }
        return chat;
    }

    private ChatMessageVO toMessageVO(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setMessageId("msg_" + message.getId());
        vo.setRole(message.getRole());
        vo.setContent(message.getContent());
        vo.setImage(message.getImage());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }

    private String buildChatTitle(String content, String image) {
        if (StrUtil.isNotBlank(content)) {
            return content.length() > 20 ? content.substring(0, 20) + "..." : content;
        }
        if (StrUtil.isNotBlank(image)) {
            return "图片提问";
        }
        return "新对话";
    }
}