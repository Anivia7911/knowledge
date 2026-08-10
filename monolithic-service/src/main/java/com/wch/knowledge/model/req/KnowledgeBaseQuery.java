package com.wch.knowledge.model.req;

import com.wch.common.model.req.PageQuery;
import lombok.Data;

/**
 * 知识库查询条件
 */
@Data
public class KnowledgeBaseQuery extends PageQuery {

    /**
     * 知识库名称（模糊搜索）
     */
    private String name;
}
