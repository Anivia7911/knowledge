package com.wch.rag.controller;

import com.wch.rag.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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


}
