package com.wch.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wch.common.model.resp.RespPageResult;
import com.wch.knowledge.mapper.KnowledgeDocumentMapper;
import com.wch.knowledge.model.po.KnowledgeDocumentPO;
import com.wch.knowledge.model.req.KnowledgeDocumentQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class KnowledgeDocumentService extends ServiceImpl<KnowledgeDocumentMapper, KnowledgeDocumentPO> implements IService<KnowledgeDocumentPO> {

    public RespPageResult<KnowledgeDocumentPO> selectPageList(KnowledgeDocumentQuery query) {
        LambdaQueryWrapper<KnowledgeDocumentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDocumentPO::getKnowledgeBaseId, query.getKnowledgeBaseId());
        if (StringUtils.hasText(query.getFileName())) {
            wrapper.like(KnowledgeDocumentPO::getFileName, query.getFileName());
        }
        wrapper.orderByDesc(KnowledgeDocumentPO::getCreateDate);

        Page<KnowledgeDocumentPO> page = getBaseMapper().selectPage(
                new Page<>(query.getPage(), query.getRows()), wrapper
        );
        return RespPageResult.success(page);
    }

    public long countByKnowledgeBaseId(Long knowledgeBaseId) {
        return lambdaQuery()
                .eq(KnowledgeDocumentPO::getKnowledgeBaseId, knowledgeBaseId)
                .count();
    }
}
