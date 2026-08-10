package com.wch.knowledge.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wch.common.model.po.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_base")
public class KnowledgeBasePO extends BasePO {

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 知识库描述
     */
    private String description;

    /**
     * 文档数量
     */
    private Integer docCount;

    /**
     * 父知识库ID，0表示顶级
     */
    private Long parentId;
}
