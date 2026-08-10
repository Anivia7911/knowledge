package com.wch.rag.model.req;

import com.wch.common.model.req.PageQuery;
import com.wch.rag.trivial.enumdata.ProviderType;
import lombok.Data;

/**
 * @author: Jie Bugui
 * @create: 2026-05-01 2:05
 */
@Data
public class AiModelQuery extends PageQuery {
    /**
     * ai服务类型
     */
    private ProviderType providerType;

    /**
     * 开关
     */
    private Boolean enabled;
}
