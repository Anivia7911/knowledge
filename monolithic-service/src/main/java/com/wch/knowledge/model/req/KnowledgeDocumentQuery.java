package com.wch.knowledge.model.req;

import com.wch.common.model.req.PageQuery;
import lombok.Data;

/**
 * 知识库文档查询条件
 */
@Data
public class KnowledgeDocumentQuery extends PageQuery {

    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * 文件名（模糊搜索）
     */
    private String fileName;
}
