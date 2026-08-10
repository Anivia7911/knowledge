package com.wch.rag.model.req;

import lombok.Data;

import java.util.List;

/**
 * AI对话请求
 */
@Data
public class ChatRequest {

    /**
     * 会话ID（为空则创建新会话）
     */
    private Long conversationId;

    /**
     * 用户消息
     */
    private String message;

    /**
     * 指定模型ID（为空则使用默认模型）
     */
    private Long modelId;

    /**
     * 检索增强的知识库ID列表（为空则不启用RAG）
     */
    private List<Long> knowledgeBaseIds;
}
