package com.wch.rag.service;

import com.wch.file.FileSetting;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzhv15.BgeSmallZhV15EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

/**
 * RAG 向量存储服务
 * 每个知识库一个向量存储文件（JSON持久化），使用本地 bge-small-zh 嵌入模型
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagVectorService {

    private final FileSetting fileSetting;

    /**
     * 本地中文嵌入模型（单例，懒加载）
     */
    private static volatile EmbeddingModel embeddingModel;

    private static EmbeddingModel embeddingModel() {
        if (embeddingModel == null) {
            synchronized (RagVectorService.class) {
                if (embeddingModel == null) {
                    log.info("初始化本地嵌入模型 bge-small-zh-v15 ...");
                    embeddingModel = new BgeSmallZhV15EmbeddingModel();
                }
            }
        }
        return embeddingModel;
    }

    /**
     * 向知识库写入文档分片向量
     */
    public synchronized void ingestText(Long knowledgeBaseId, Long documentId, String fileName, String text) {
        if (text == null || text.isBlank()) {
            throw new RuntimeException("文档内容为空，无法向量化");
        }
        // 先移除该文档旧的分片（支持重复处理）
        removeDocumentChunks(knowledgeBaseId, documentId);

        Metadata docMeta = new Metadata();
        docMeta.put("documentId", String.valueOf(documentId));
        docMeta.put("fileName", fileName == null ? "" : fileName);
        Document document = Document.from(text, docMeta);

        // 递归分片：单片最大400字符，重叠60字符
        List<TextSegment> segments = DocumentSplitters.recursive(400, 60).split(document);
        if (segments.isEmpty()) {
            throw new RuntimeException("文档分片结果为空");
        }

        List<Embedding> embeddings = embeddingModel().embedAll(segments).content();

        InMemoryEmbeddingStore<TextSegment> store = loadStore(knowledgeBaseId);
        store.addAll(embeddings, segments);
        saveStore(knowledgeBaseId, store);
        log.info("知识库[{}] 文档[{}] 向量化完成，分片数: {}", knowledgeBaseId, documentId, segments.size());
    }

    /**
     * 移除知识库中某文档的全部分片
     */
    public synchronized void removeDocumentChunks(Long knowledgeBaseId, Long documentId) {
        Path file = storeFile(knowledgeBaseId);
        if (!Files.exists(file)) {
            return;
        }
        InMemoryEmbeddingStore<TextSegment> store = loadStore(knowledgeBaseId);
        store.removeAll(metadataKey("documentId").isEqualTo(String.valueOf(documentId)));
        saveStore(knowledgeBaseId, store);
    }

    /**
     * 删除整个知识库的向量存储
     */
    public synchronized void removeKnowledgeBase(Long knowledgeBaseId) {
        try {
            Files.deleteIfExists(storeFile(knowledgeBaseId));
        } catch (Exception e) {
            log.warn("删除知识库[{}]向量文件失败: {}", knowledgeBaseId, e.getMessage());
        }
    }

    /**
     * 跨知识库检索相关分片
     */
    public synchronized List<Chunk> search(List<Long> knowledgeBaseIds, String query, int maxResults) {
        List<Chunk> result = new ArrayList<>();
        if (knowledgeBaseIds == null || knowledgeBaseIds.isEmpty() || query == null || query.isBlank()) {
            return result;
        }
        Embedding queryEmbedding = embeddingModel().embed(query).content();
        for (Long kbId : knowledgeBaseIds) {
            if (!Files.exists(storeFile(kbId))) {
                continue;
            }
            InMemoryEmbeddingStore<TextSegment> store = loadStore(kbId);
            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(maxResults)
                    .minScore(0.3)
                    .build();
            EmbeddingSearchResult<TextSegment> searchResult = store.search(request);
            for (EmbeddingMatch<TextSegment> match : searchResult.matches()) {
                Chunk chunk = new Chunk();
                chunk.setText(match.embedded().text());
                chunk.setScore(match.score());
                chunk.setFileName(match.embedded().metadata().getString("fileName"));
                result.add(chunk);
            }
        }
        // 按相关度降序，取前 maxResults 条
        result.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return result.size() > maxResults ? result.subList(0, maxResults) : result;
    }

    private InMemoryEmbeddingStore<TextSegment> loadStore(Long knowledgeBaseId) {
        Path file = storeFile(knowledgeBaseId);
        try {
            if (Files.exists(file)) {
                return InMemoryEmbeddingStore.fromJson(Files.readString(file));
            }
        } catch (Exception e) {
            log.error("加载知识库[{}]向量文件失败，将重建: {}", knowledgeBaseId, e.getMessage());
        }
        return new InMemoryEmbeddingStore<>();
    }

    private void saveStore(Long knowledgeBaseId, InMemoryEmbeddingStore<TextSegment> store) {
        try {
            Path file = storeFile(knowledgeBaseId);
            Files.createDirectories(file.getParent());
            Files.writeString(file, store.serializeToJson());
        } catch (Exception e) {
            throw new RuntimeException("保存向量文件失败: " + e.getMessage(), e);
        }
    }

    private Path storeFile(Long knowledgeBaseId) {
        Path baseDir = Paths.get(fileSetting.getTempUploadPath()).getParent();
        if (baseDir == null) {
            baseDir = Paths.get(System.getProperty("java.io.tmpdir"));
        }
        return baseDir.resolve("vector").resolve("kb_" + knowledgeBaseId + ".json");
    }

    /**
     * 检索结果分片
     */
    @Data
    public static class Chunk {
        private String text;
        private double score;
        private String fileName;
    }
}
