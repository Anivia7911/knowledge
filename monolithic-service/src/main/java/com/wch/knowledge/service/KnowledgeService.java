package com.wch.knowledge.service;

import com.wch.file.model.po.FileBodyPO;
import com.wch.file.model.po.FileHeaderPO;
import com.wch.file.service.FileBodyService;
import com.wch.file.service.FileService;
import com.wch.knowledge.model.po.KnowledgeBasePO;
import com.wch.knowledge.model.po.KnowledgeDocumentPO;
import com.wch.knowledge.model.req.KnowledgeBaseDTO;
import com.wch.knowledge.model.req.KnowledgeDocumentDTO;
import com.wch.knowledge.model.vo.KnowledgeTreeNode;
import com.wch.rag.service.RagIngestService;
import com.wch.rag.service.RagVectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final FileService fileService;
    private final FileBodyService fileBodyService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final KnowledgeDocumentService knowledgeDocumentService;
    private final RagIngestService ragIngestService;
    private final RagVectorService ragVectorService;

    /**
     * 上传文件到文件服务
     */
    public void uploadFile(MultipartFile file) throws IOException {
        fileService.upload(file);
    }

    /**
     * 创建知识库（支持指定父节点，实现树形结构）
     */
    public KnowledgeBasePO createKnowledgeBase(KnowledgeBaseDTO dto) {
        Long parentId = dto.getParentId() == null ? 0L : dto.getParentId();
        if (parentId != 0L) {
            KnowledgeBasePO parent = knowledgeBaseService.getById(parentId);
            if (parent == null) {
                throw new RuntimeException("父知识库不存在");
            }
        }
        KnowledgeBasePO po = KnowledgeBaseDTO.transfer(dto);
        knowledgeBaseService.save(po);
        return po;
    }

    /**
     * 知识库树（全量返回，后端组装树形结构）
     */
    public List<KnowledgeTreeNode> listTree() {
        List<KnowledgeBasePO> all = knowledgeBaseService.lambdaQuery()
                .orderByAsc(KnowledgeBasePO::getId)
                .list();
        Map<Long, KnowledgeTreeNode> nodeMap = new HashMap<>();
        for (KnowledgeBasePO po : all) {
            KnowledgeTreeNode node = new KnowledgeTreeNode();
            node.setId(po.getId());
            node.setParentId(po.getParentId() == null ? 0L : po.getParentId());
            node.setName(po.getName());
            node.setDescription(po.getDescription());
            node.setDocCount(po.getDocCount());
            node.setCreateDate(po.getCreateDate());
            nodeMap.put(po.getId(), node);
        }
        List<KnowledgeTreeNode> roots = new ArrayList<>();
        for (KnowledgeTreeNode node : nodeMap.values()) {
            KnowledgeTreeNode parent = node.getParentId() == 0L ? null : nodeMap.get(node.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        // 聚合文档数：每个节点的 docCount = 自身直接文档数 + 所有子孙知识库的文档数总和
        for (KnowledgeTreeNode root : roots) {
            aggregateDocCount(root);
        }
        return roots;
    }

    /**
     * 递归聚合：节点文档数 = 自身 + 全部子孙节点
     */
    private int aggregateDocCount(KnowledgeTreeNode node) {
        int total = node.getDocCount() == null ? 0 : node.getDocCount();
        for (KnowledgeTreeNode child : node.getChildren()) {
            total += aggregateDocCount(child);
        }
        node.setDocCount(total);
        return total;
    }

    /**
     * 编辑知识库
     */
    public void updateKnowledgeBase(KnowledgeBaseDTO dto) {
        KnowledgeBasePO po = new KnowledgeBasePO();
        po.setId(dto.getId());
        po.setName(dto.getName());
        po.setDescription(dto.getDescription());
        knowledgeBaseService.updateById(po);
    }

    /**
     * 删除知识库（同时删除关联文档与向量数据；存在子知识库时禁止删除）
     */
    @Transactional(rollbackFor = Throwable.class)
    public void deleteKnowledgeBase(Long id) {
        boolean hasChildren = knowledgeBaseService.lambdaQuery()
                .eq(KnowledgeBasePO::getParentId, id)
                .exists();
        if (hasChildren) {
            throw new RuntimeException("该知识库下存在子知识库，请先删除子知识库");
        }
        knowledgeBaseService.removeById(id);
        knowledgeDocumentService.lambdaUpdate()
                .eq(KnowledgeDocumentPO::getKnowledgeBaseId, id)
                .remove();
        // 清理向量存储
        ragVectorService.removeKnowledgeBase(id);
    }

    /**
     * 向知识库添加文档
     */
    @Transactional(rollbackFor = Throwable.class)
    public void addDocuments(KnowledgeDocumentDTO dto) {
        Long kbId = dto.getKnowledgeBaseId();
        KnowledgeBasePO kb = knowledgeBaseService.getById(kbId);
        if (kb == null) {
            throw new RuntimeException("知识库不存在");
        }

        List<Long> fileIds = dto.getFileIds();
        for (Long fileId : fileIds) {
            // 检查是否已存在
            boolean exists = knowledgeDocumentService.lambdaQuery()
                    .eq(KnowledgeDocumentPO::getKnowledgeBaseId, kbId)
                    .eq(KnowledgeDocumentPO::getFileId, fileId)
                    .exists();
            if (exists) continue;

            KnowledgeDocumentPO doc = new KnowledgeDocumentPO();
            doc.setKnowledgeBaseId(kbId);
            doc.setFileId(fileId);
            // 冗余存储文件名与类型，便于列表展示
            FileHeaderPO header = fileService.getFileInfo(String.valueOf(fileId));
            if (header != null) {
                doc.setFileName(header.getName());
                FileBodyPO body = header.getBodyId() != null ? fileBodyService.getById(header.getBodyId()) : null;
                if (body != null) {
                    doc.setFileType(body.getType());
                }
            }
            doc.setStatus(0); // 待处理
            knowledgeDocumentService.save(doc);
            // 事务提交后异步向量化
            scheduleIngest(kbId, doc.getId(), fileId, doc.getFileName());
        }

        // 更新知识库文档数量
        long count = knowledgeDocumentService.countByKnowledgeBaseId(kbId);
        kb.setDocCount((int) count);
        knowledgeBaseService.updateById(kb);
    }

    /**
     * 直接上传文件并添加到知识库（任意层级节点均可）
     */
    @Transactional(rollbackFor = Throwable.class)
    public KnowledgeDocumentPO uploadAndAddDocument(Long knowledgeBaseId, MultipartFile file) throws IOException {
        KnowledgeBasePO kb = knowledgeBaseService.getById(knowledgeBaseId);
        if (kb == null) {
            throw new RuntimeException("知识库不存在");
        }
        Long fileId = fileService.upload(file);

        KnowledgeDocumentPO doc = new KnowledgeDocumentPO();
        doc.setKnowledgeBaseId(knowledgeBaseId);
        doc.setFileId(fileId);
        FileHeaderPO header = fileService.getFileInfo(String.valueOf(fileId));
        if (header != null) {
            doc.setFileName(header.getName());
            FileBodyPO body = header.getBodyId() != null ? fileBodyService.getById(header.getBodyId()) : null;
            if (body != null) {
                doc.setFileType(body.getType());
            }
        }
        doc.setStatus(0);
        knowledgeDocumentService.save(doc);

        long count = knowledgeDocumentService.countByKnowledgeBaseId(knowledgeBaseId);
        kb.setDocCount((int) count);
        knowledgeBaseService.updateById(kb);

        scheduleIngest(knowledgeBaseId, doc.getId(), fileId, doc.getFileName());
        return doc;
    }

    /**
     * 重试向量化失败的文档（重置状态为待处理并重新触发异步向量化）
     */
    @Transactional(rollbackFor = Throwable.class)
    public void retryIngest(Long documentId) {
        KnowledgeDocumentPO doc = knowledgeDocumentService.getById(documentId);
        if (doc == null) {
            throw new RuntimeException("文档不存在");
        }
        doc.setStatus(0);
        knowledgeDocumentService.updateById(doc);
        scheduleIngest(doc.getKnowledgeBaseId(), doc.getId(), doc.getFileId(), doc.getFileName());
    }

    /**
     * 事务提交后再触发异步向量化，避免异步线程读不到未提交的数据
     */
    private void scheduleIngest(Long kbId, Long docId, Long fileId, String fileName) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    ragIngestService.ingestAsync(kbId, docId, fileId, fileName);
                }
            });
        } else {
            ragIngestService.ingestAsync(kbId, docId, fileId, fileName);
        }
    }

    /**
     * 从知识库移除文档（同时清理向量分片）
     */
    @Transactional(rollbackFor = Throwable.class)
    public void removeDocument(Long knowledgeBaseId, Long documentId) {
        knowledgeDocumentService.removeById(documentId);
        long count = knowledgeDocumentService.countByKnowledgeBaseId(knowledgeBaseId);
        KnowledgeBasePO kb = knowledgeBaseService.getById(knowledgeBaseId);
        if (kb != null) {
            kb.setDocCount((int) count);
            knowledgeBaseService.updateById(kb);
        }
        // 清理该文档的向量分片
        ragVectorService.removeDocumentChunks(knowledgeBaseId, documentId);
    }
}
