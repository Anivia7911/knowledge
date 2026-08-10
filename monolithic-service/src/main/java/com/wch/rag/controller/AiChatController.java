package com.wch.rag.controller;

import com.wch.common.model.resp.RespResult;
import com.wch.rag.model.po.ChatConversationPO;
import com.wch.rag.model.po.ChatMessagePO;
import com.wch.rag.model.req.ChatRequest;
import com.wch.rag.model.req.ChatResponse;
import com.wch.rag.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: Jie Bugui
 * @create: 2026-05-01 0:56
 */
@RestController
@RequestMapping("/ai-chat")
public class AiChatController {

    private AiChatService aiChatService;

    @Autowired
    void setService(
            AiChatService aiChatService
    ) {
        this.aiChatService = aiChatService;
    }

    /**
     * 发送对话消息
     */
    @PostMapping("/send")
    public RespResult<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = aiChatService.chat(request);
        return RespResult.success(response);
    }

    /**
     * 获取会话历史消息
     */
    @GetMapping("/history")
    public RespResult<List<ChatMessagePO>> history(@RequestParam("conversationId") Long conversationId) {
        return RespResult.success(aiChatService.getHistory(conversationId));
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    public RespResult<List<ChatConversationPO>> conversations() {
        return RespResult.success(aiChatService.listConversations());
    }

    /**
     * 删除会话
     */
    @PostMapping("/conversation/delete")
    public RespResult<Void> deleteConversation(@RequestParam("conversationId") Long conversationId) {
        aiChatService.deleteConversation(conversationId);
        return RespResult.success();
    }
}
