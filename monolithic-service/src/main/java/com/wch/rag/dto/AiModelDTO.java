package com.wch.rag.dto;

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
}
