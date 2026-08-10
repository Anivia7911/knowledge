package com.wch.rag.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wch.common.model.resp.RespPageResult;
import com.wch.rag.mapper.AiModelMapper;
import com.wch.rag.model.po.AiModelPO;
import com.wch.rag.model.req.AiModelQuery;
import com.wch.rag.trivial.enumdata.ProviderType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: Jie Bugui
 * @create: 2026-05-01 0:55
 */
@Service
public class AiModelService extends ServiceImpl<AiModelMapper, AiModelPO> implements IService<AiModelPO> {

    /**
     * 新增模型，若设为默认则取消其他模型的默认状态
     */
    @Transactional(rollbackFor = Throwable.class)
    public void saveModel(AiModelPO po) {
        clearOtherDefault(po);
        save(po);
    }

    /**
     * 编辑模型，若设为默认则取消其他模型的默认状态
     */
    @Transactional(rollbackFor = Throwable.class)
    public void updateModel(AiModelPO po) {
        clearOtherDefault(po);
        updateById(po);
    }

    private void clearOtherDefault(AiModelPO po) {
        if (Boolean.TRUE.equals(po.getDefaultModel())) {
            lambdaUpdate()
                    .eq(AiModelPO::getDefaultModel, true)
                    .ne(po.getId() != null, AiModelPO::getId, po.getId())
                    .set(AiModelPO::getDefaultModel, false)
                    .update();
        }
    }

    public RespPageResult<AiModelPO> selectPageList(AiModelQuery query) {
        LambdaQueryWrapper<AiModelPO> queryWrapper = new LambdaQueryWrapper<>();

        Page<AiModelPO> aiModelPOPage = getBaseMapper().selectPage(new Page<>(query.getPage(), query.getRows()), queryWrapper);

        return RespPageResult.success(aiModelPOPage);
    }
}
