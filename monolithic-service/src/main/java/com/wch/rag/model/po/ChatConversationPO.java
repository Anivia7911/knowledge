package com.wch.rag.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wch.common.model.po.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI对话会话
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_conversation")
public class ChatConversationPO extends BasePO {

    /**
     * 会话标题
     */
    private String title;

    /**
     * 使用的模型ID
     */
    private Long modelId;

    /**
     * 会话使用的知识库ID列表（逗号分隔），用于下次打开时恢复选择
     */
    private String knowledgeBaseIds;
}
