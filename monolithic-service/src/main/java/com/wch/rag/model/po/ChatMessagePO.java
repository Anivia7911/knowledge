package com.wch.rag.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wch.common.model.po.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI对话消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_message")
public class ChatMessagePO extends BasePO {

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 角色：user / assistant / system
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;
}
