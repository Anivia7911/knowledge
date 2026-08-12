package com.wch.rag.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * RAG 向量存储服务
 * 使用 pgvector 作为向量存储，qwen3-embedding 作为嵌入模型
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagVectorService {

    private final EmbeddingModel embeddingModel;
    private final PgVectorEmbeddingStore pgVectorStore;

    /**
     * 分片策略：800字符，150字符重叠
     */
    private static final int CHUNK_SIZE = 800;
    private static final int CHUNK_OVERLAP = 150;

    /**
     * 每批嵌入的分片数，避免单次请求过大导致超时
     */
    private static final int EMBEDDING_BATCH_SIZE = 25;

    /**
     * 最低相似度阈值，过滤低质量结果
     */
    private static final double MIN_SCORE = 0.5;

    /**
     * 向知识库写入文档分片向量
     */
    public void ingestText(Long knowledgeBaseId, Long documentId, String fileName, String text) {
        ingestText(knowledgeBaseId, documentId, fileName, text, null);
    }

    /**
     * 向知识库写入文档分片向量（支持进度回调）
     * @param progressCallback 进度回调，参数为 (已完成分片数, 总分片数)，可为 null
     */
    public void ingestText(Long knowledgeBaseId, Long documentId, String fileName, String text, BiConsumer<Integer, Integer> progressCallback) {
        if (text == null || text.isBlank()) {
            throw new RuntimeException("文档内容为空，无法向量化");
        }

        // 先移除该文档旧的分片（支持重复处理）
        removeDocumentChunks(knowledgeBaseId, documentId);

        Metadata docMeta = new Metadata();
        docMeta.put("documentId", String.valueOf(documentId));
        docMeta.put("fileName", fileName == null ? "" : fileName);
        docMeta.put("knowledgeBaseId", String.valueOf(knowledgeBaseId));
        Document document = Document.from(text, docMeta);

        // 递归分片：单片最大800字符，重叠150字符
        List<TextSegment> segments = DocumentSplitters.recursive(CHUNK_SIZE, CHUNK_OVERLAP).split(document);
        if (segments.isEmpty()) {
            throw new RuntimeException("文档分片结果为空");
        }

        log.info("知识库[{}] 文档[{}] 开始向量化，总分片数: {}", knowledgeBaseId, documentId, segments.size());
        long startTime = System.currentTimeMillis();

        // 分片完成后立即通知总数
        if (progressCallback != null) {
            progressCallback.accept(0, segments.size());
        }

        // 分批嵌入并写入向量库，边嵌入边存储避免一次性大批量处理
        for (int i = 0; i < segments.size(); i += EMBEDDING_BATCH_SIZE) {
            int end = Math.min(i + EMBEDDING_BATCH_SIZE, segments.size());
            List<TextSegment> batch = segments.subList(i, end);

            List<Embedding> batchEmbeddings = embeddingModel.embedAll(batch).content();

            List<String> batchIds = new ArrayList<>();
            for (int j = 0; j < batch.size(); j++) {
                batchIds.add(UUID.randomUUID().toString());
            }
            pgVectorStore.addAll(batchIds, batchEmbeddings, batch);

            if (progressCallback != null) {
                progressCallback.accept(end, segments.size());
            }
            log.info("知识库[{}] 文档[{}] 向量化进度: {}/{}", knowledgeBaseId, documentId, end, segments.size());
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("知识库[{}] 文档[{}] 向量化完成，分片数: {}，耗时: {}ms", knowledgeBaseId, documentId, segments.size(), elapsed);
    }

    /**
     * 移除知识库中某文档的全部分片
     */
    public void removeDocumentChunks(Long knowledgeBaseId, Long documentId) {
        try {
            pgVectorStore.removeAll(
                    MetadataFilterBuilder.metadataKey("documentId").isEqualTo(String.valueOf(documentId))
            );
        } catch (Exception e) {
            log.warn("移除知识库[{}]文档[{}]分片失败: {}", knowledgeBaseId, documentId, e.getMessage());
        }
    }

    /**
     * 删除整个知识库的向量数据
     */
    public void removeKnowledgeBase(Long knowledgeBaseId) {
        try {
            pgVectorStore.removeAll(
                    MetadataFilterBuilder.metadataKey("knowledgeBaseId").isEqualTo(String.valueOf(knowledgeBaseId))
            );
        } catch (Exception e) {
            log.warn("删除知识库[{}]向量数据失败: {}", knowledgeBaseId, e.getMessage());
        }
    }

    /**
     * 跨知识库检索相关分片
     */
    public List<Chunk> search(List<Long> knowledgeBaseIds, String query, int maxResults) {
        if (knowledgeBaseIds == null || knowledgeBaseIds.isEmpty() || query == null || query.isBlank()) {
            return new ArrayList<>();
        }

        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // 每个知识库多取一些结果，最后合并去重
        int perKbResults = maxResults * 2;
        List<Chunk> allChunks = new ArrayList<>();

        for (Long kbId : knowledgeBaseIds) {
            var filter = MetadataFilterBuilder.metadataKey("knowledgeBaseId").isEqualTo(String.valueOf(kbId));

            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(perKbResults)
                    .minScore(MIN_SCORE)
                    .filter(filter)
                    .build();

            EmbeddingSearchResult<TextSegment> searchResult = pgVectorStore.search(request);
            for (EmbeddingMatch<TextSegment> match : searchResult.matches()) {
                Chunk chunk = new Chunk();
                chunk.setText(match.embedded().text());
                chunk.setScore(match.score());
                chunk.setFileName(match.embedded().metadata().getString("fileName"));
                allChunks.add(chunk);
            }
        }

        // 按相关度降序，去重后取前 maxResults 条
        allChunks.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        LinkedHashSet<Chunk> deduped = new LinkedHashSet<>();
        for (Chunk c : allChunks) {
            deduped.add(c);
            if (deduped.size() >= maxResults) break;
        }
        return new ArrayList<>(deduped);
    }

    /**
     * 混合检索：原始查询 + 改写查询，合并去重
     */
    public List<Chunk> hybridSearch(List<Long> knowledgeBaseIds, String query, String rewrittenQuery, int maxResults) {
        List<Chunk> vectorResults = search(knowledgeBaseIds, query, maxResults * 2);
        List<Chunk> rewrittenResults = new ArrayList<>();

        if (rewrittenQuery != null && !rewrittenQuery.isBlank()) {
            rewrittenResults = search(knowledgeBaseIds, rewrittenQuery, maxResults * 2);
        }

        // 合并去重，保留高分
        Map<String, Chunk> merged = new LinkedHashMap<>();
        for (Chunk c : vectorResults) {
            merged.putIfAbsent(c.getText(), c);
        }
        for (Chunk c : rewrittenResults) {
            merged.putIfAbsent(c.getText(), c);
        }

        List<Chunk> all = new ArrayList<>(merged.values());
        all.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return all.size() > maxResults ? all.subList(0, maxResults) : all;
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
