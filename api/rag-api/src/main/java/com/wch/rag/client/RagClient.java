package com.wch.rag.client;

import com.wch.common.model.resp.RespResult;
import com.wch.rag.dto.AiModelDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("rag-service")
public interface RagClient {
    /**
     * 模型新增
     */
    @PostMapping("/ai-model/add")
    RespResult<Void> add(@RequestBody AiModelDTO dto);
}
