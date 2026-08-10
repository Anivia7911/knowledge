package com.wch.rag.service;

import com.wch.knowledge.model.po.KnowledgeDocumentPO;
import com.wch.knowledge.service.KnowledgeDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * RAG 文档摄取服务：异步解析文件并向量化，更新文档处理状态
 * 状态：0待处理 1处理中 2已完成 3失败
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagIngestService {

    private final DocumentParseService documentParseService;
    private final RagVectorService ragVectorService;
    private final KnowledgeDocumentService knowledgeDocumentService;

    @Async("ragExecutor")
    public void ingestAsync(Long knowledgeBaseId, Long documentId, Long fileId, String fileName) {
        updateStatus(documentId, 1);
        try {
            String text = documentParseService.parseByFileId(fileId);
            ragVectorService.ingestText(knowledgeBaseId, documentId, fileName, text);
            updateStatus(documentId, 2);
        } catch (Exception e) {
            log.error("知识库[{}] 文档[{}] 向量化失败: {}", knowledgeBaseId, documentId, e.getMessage());
            updateStatus(documentId, 3);
        }
    }

    private void updateStatus(Long documentId, int status) {
        try {
            KnowledgeDocumentPO update = new KnowledgeDocumentPO();
            update.setId(documentId);
            update.setStatus(status);
            knowledgeDocumentService.updateById(update);
        } catch (Exception e) {
            log.warn("更新文档状态失败: {}", e.getMessage());
        }
    }
}
