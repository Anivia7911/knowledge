package com.wch.rag.controller;

import com.wch.common.model.resp.RespResult;
import com.wch.rag.dto.FileInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 文件向量化处理
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class FileVectorController {

    /**
     * 文件向量化 - 接收文件信息，进行解析和向量化处理
     */
    @PostMapping("/file")
    public RespResult<Void> fileVector(@RequestBody FileInfoDTO dto) {
        log.info("收到文件向量化请求: fileId={}, filename={}, type={}", dto.getId(), dto.getFilename(), dto.getType());
        // TODO: 实现文件解析和向量化处理流程
        // 1. 根据文件路径读取文件内容
        // 2. 解析文件文本（PDF、Word、TXT等）
        // 3. 文本分块
        // 4. 调用 Embedding 模型生成向量
        // 5. 存储到向量数据库
        return RespResult.success();
    }
}
