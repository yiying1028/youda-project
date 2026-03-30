package com.youda.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youda.common.Result;
import com.youda.dto.ChatMessageDTO;
import com.youda.service.ChatService;
import com.youda.utils.FileUtils;
import com.youda.vo.ChatListVO;
import com.youda.vo.ChatMessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AI对话Controller
 * 管理对话会话、消息发送和图片上传
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private FileUtils fileUtils;

    // ============ 对话管理 ============

    /**
     * 创建新的AI对话会话
     * @return chatId（UUID格式字符串，用于后续消息发送）
     */
    @PostMapping("/create")
    public Result<Map<String, String>> createChat() {
        String chatId = chatService.createChat();
        Map<String, String> data = new HashMap<>();
        data.put("chatId", chatId);
        return Result.success("创建成功", data);
    }

    /**
     * 获取当前用户的对话列表（分页）
     */
    @GetMapping("/list")
    public Result<IPage<ChatListVO>> getChatList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<ChatListVO> page = chatService.getChatList(current, size);
        return Result.success(page);
    }

    /**
     * 获取指定对话的完整消息记录
     */
    @GetMapping("/{chatId}")
    public Result<Map<String, Object>> getChatDetail(@PathVariable String chatId) {
        Map<String, Object> detail = chatService.getChatDetail(chatId);
        return Result.success(detail);
    }

    /**
     * 向指定对话发送消息（可携带图片URL）
     * @return AI回复的消息内容
     */
    @PostMapping("/{chatId}/message")
    public Result<ChatMessageVO> sendMessage(
            @PathVariable String chatId,
            @Valid @RequestBody ChatMessageDTO dto) {
        ChatMessageVO vo = chatService.sendMessage(chatId, dto);
        return Result.success(vo);
    }

    /**
     * 删除对话（同时删除所有消息）
     */
    @DeleteMapping("/{chatId}")
    public Result<?> deleteChat(@PathVariable String chatId) {
        chatService.deleteChat(chatId);
        return Result.success("删除成功", null);
    }

    // ============ 文件上传 ============

    /**
     * 上传题目图片（用于AI识题）
     * @return 图片访问URL，发送消息时携带此URL
     */
    @PostMapping("/upload-image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String url = fileUtils.uploadFile(file, "chat");
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success("上传成功", data);
    }
}
