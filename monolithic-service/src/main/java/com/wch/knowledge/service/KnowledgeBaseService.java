package com.wch.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wch.common.model.resp.RespPageResult;
import com.wch.knowledge.mapper.KnowledgeBaseMapper;
import com.wch.knowledge.model.po.KnowledgeBasePO;
import com.wch.knowledge.model.req.KnowledgeBaseQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class KnowledgeBaseService extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBasePO> implements IService<KnowledgeBasePO> {

    public RespPageResult<KnowledgeBasePO> selectPageList(KnowledgeBaseQuery query) {
        LambdaQueryWrapper<KnowledgeBasePO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(KnowledgeBasePO::getName, query.getName());
        }
        wrapper.orderByDesc(KnowledgeBasePO::getCreateDate);

        Page<KnowledgeBasePO> page = getBaseMapper().selectPage(
                new Page<>(query.getPage(), query.getRows()), wrapper
        );
        return RespPageResult.success(page);
    }
}
