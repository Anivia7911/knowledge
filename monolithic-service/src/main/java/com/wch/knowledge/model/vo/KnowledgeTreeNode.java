package com.wch.knowledge.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识库树节点
 */
@Data
public class KnowledgeTreeNode {

    private Long id;

    private Long parentId;

    private String name;

    private String description;

    private Integer docCount;

    private LocalDateTime createDate;

    private List<KnowledgeTreeNode> children = new ArrayList<>();
}
