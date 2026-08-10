package com.wch.rag.model.req;

import com.wch.rag.model.po.AiModelPO;
import com.wch.rag.trivial.enumdata.AiModelType;
import com.wch.rag.trivial.enumdata.ProviderType;
import lombok.Data;

/**
 * @author: Jie Bugui
 * @create: 2026-05-01 2:09
 */
@Data
public class AiModelDTO {

    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * ai服务类型
     */
    private ProviderType providerType;

    /**
     * apiKey
     */
    private String apiKey;

    /**
     * 模型url (说明：对接自定义模型使用)
     */
    private String apiUrl;

    /**
     * 模型类型
     */
    private AiModelType modelType;

    /**
     * 模型名
     */
    private String modelName;

    /**
     * 开关
     */
    private Boolean enabled;

    /**
     * 是否默认模型
     */
    private Boolean defaultModel;

    public static AiModelPO transfer(AiModelDTO dto) {
        AiModelPO po = new AiModelPO();
        po.setProviderType(dto.getProviderType());
        po.setApiKey(dto.getApiKey());
        po.setApiUrl(dto.getApiUrl());
        po.setModelType(dto.getModelType());
        po.setModelName(dto.getModelName());
        po.setEnabled(dto.getEnabled());
        po.setDefaultModel(dto.getDefaultModel());
        po.setId(dto.getId());
        po.setName(dto.getName());
        return po;
    }
}
