package com.wch.knowledge.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wch.common.model.po.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库文档（知识库与文件的关联关系）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_document")
public class KnowledgeDocumentPO extends BasePO {

    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * 文件ID（file_header.id）
     */
    private Long fileId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文档状态：0-待处理 1-处理中 2-已完成 3-处理失败
     */
    private Integer status;

    /**
     * 向量化当前进度（已完成分片数）
     */
    private Integer progressCurrent;

    /**
     * 向量化总分片数
     */
    private Integer progressTotal;
}
