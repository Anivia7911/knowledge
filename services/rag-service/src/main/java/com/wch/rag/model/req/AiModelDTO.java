package com.wch.rag.model.req;

import com.wch.rag.model.po.AiModelPO;
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
     * 开关
     */
    private Boolean enabled;

    public static AiModelPO transfer(AiModelDTO dto) {
        AiModelPO po = new AiModelPO();
        po.setProviderType(dto.getProviderType());
        po.setEnabled(dto.getEnabled());
        po.setId(dto.getId());
        po.setName(dto.getName());
        return po;
    }
}
