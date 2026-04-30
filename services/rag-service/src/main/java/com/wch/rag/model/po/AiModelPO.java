package com.wch.rag.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wch.common.model.po.BasePO;
import com.wch.rag.trivial.enumdata.ProviderType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: Jie Bugui
 * @create: 2026-05-01 0:49
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_model")
public class AiModelPO extends BasePO {

    /**
     * 名称
     */
    private String name;

    /**
     * ai服务类型
     */
    private ProviderType providerType;

    /**
     * 开关
     */
    private Boolean enabled;
}
