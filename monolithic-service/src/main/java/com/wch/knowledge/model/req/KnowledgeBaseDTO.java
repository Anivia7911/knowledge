package com.wch.knowledge.model.req;

import com.wch.knowledge.model.po.KnowledgeBasePO;
import lombok.Data;

/**
 * 知识库创建/编辑请求
 */
@Data
public class KnowledgeBaseDTO {

    private Long id;

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 知识库描述
     */
    private String description;

    /**
     * 父知识库ID（为空或0表示顶级）
     */
    private Long parentId;

    public static KnowledgeBasePO transfer(KnowledgeBaseDTO dto) {
        KnowledgeBasePO po = new KnowledgeBasePO();
        po.setId(dto.getId());
        po.setName(dto.getName());
        po.setDescription(dto.getDescription());
        po.setDocCount(0);
        po.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        return po;
    }
}
