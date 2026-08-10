package com.wch.knowledge.model.req;

import lombok.Data;

import java.util.List;

/**
 * 知识库文档关联请求
 */
@Data
public class KnowledgeDocumentDTO {

    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * 文件ID列表（file_header.id）
     */
    private List<Long> fileIds;
}
