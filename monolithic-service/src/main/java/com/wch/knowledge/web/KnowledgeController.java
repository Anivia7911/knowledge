package com.wch.knowledge.web;

import com.wch.common.model.resp.RespPageResult;
import com.wch.common.model.resp.RespResult;
import com.wch.knowledge.model.po.KnowledgeBasePO;
import com.wch.knowledge.model.po.KnowledgeDocumentPO;
import com.wch.knowledge.model.req.KnowledgeBaseDTO;
import com.wch.knowledge.model.req.KnowledgeBaseQuery;
import com.wch.knowledge.model.req.KnowledgeDocumentDTO;
import com.wch.knowledge.model.req.KnowledgeDocumentQuery;
import com.wch.knowledge.model.vo.KnowledgeTreeNode;
import com.wch.knowledge.service.KnowledgeBaseService;
import com.wch.knowledge.service.KnowledgeDocumentService;
import com.wch.knowledge.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

    private KnowledgeService knowledgeService;
    private KnowledgeBaseService knowledgeBaseService;
    private KnowledgeDocumentService knowledgeDocumentService;

    @Autowired
    void setService(
            KnowledgeService knowledgeService,
            KnowledgeBaseService knowledgeBaseService,
            KnowledgeDocumentService knowledgeDocumentService
    ) {
        this.knowledgeService = knowledgeService;
        this.knowledgeBaseService = knowledgeBaseService;
        this.knowledgeDocumentService = knowledgeDocumentService;
    }

    /**
     * 上传文件
     */
    @PostMapping("/file-upload")
    public RespResult<Void> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        knowledgeService.uploadFile(file);
        return RespResult.success();
    }

    // ==================== 知识库管理 ====================

    /**
     * 知识库树（支持任意层级）
     */
    @GetMapping("/base/tree")
    public RespResult<List<KnowledgeTreeNode>> baseTree() {
        return RespResult.success(knowledgeService.listTree());
    }

    /**
     * 创建知识库
     */
    @PostMapping("/base/add")
    public RespResult<KnowledgeBasePO> addBase(@RequestBody KnowledgeBaseDTO dto) {
        KnowledgeBasePO po = knowledgeService.createKnowledgeBase(dto);
        return RespResult.success(po);
    }

    /**
     * 编辑知识库
     */
    @PostMapping("/base/edit")
    public RespResult<Void> editBase(@RequestBody KnowledgeBaseDTO dto) {
        knowledgeService.updateKnowledgeBase(dto);
        return RespResult.success();
    }

    /**
     * 删除知识库
     */
    @PostMapping("/base/delete")
    public RespResult<Void> deleteBase(@RequestParam("id") Long id) {
        knowledgeService.deleteKnowledgeBase(id);
        return RespResult.success();
    }

    /**
     * 查看知识库详情
     */
    @GetMapping("/base/select")
    public RespResult<KnowledgeBasePO> selectBase(@RequestParam("id") Long id) {
        return RespResult.success(knowledgeBaseService.getById(id));
    }

    /**
     * 知识库分页列表
     */
    @GetMapping("/base/select-page")
    public RespPageResult<KnowledgeBasePO> selectBasePage(KnowledgeBaseQuery query) {
        return knowledgeBaseService.selectPageList(query);
    }

    // ==================== 文档管理 ====================

    /**
     * 向知识库添加文档
     */
    @PostMapping("/doc/add")
    public RespResult<Void> addDocuments(@RequestBody KnowledgeDocumentDTO dto) {
        knowledgeService.addDocuments(dto);
        return RespResult.success();
    }

    /**
     * 直接上传文件并添加到指定知识库（任意层级节点）
     */
    @PostMapping("/doc/upload")
    public RespResult<KnowledgeDocumentPO> uploadDocument(
            @RequestParam("knowledgeBaseId") Long knowledgeBaseId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return RespResult.success(knowledgeService.uploadAndAddDocument(knowledgeBaseId, file));
    }

    /**
     * 从知识库移除文档
     */
    @PostMapping("/doc/remove")
    public RespResult<Void> removeDocument(
            @RequestParam("knowledgeBaseId") Long knowledgeBaseId,
            @RequestParam("documentId") Long documentId
    ) {
        knowledgeService.removeDocument(knowledgeBaseId, documentId);
        return RespResult.success();
    }

    /**
     * 重试向量化失败的文档
     */
    @PostMapping("/doc/retry-ingest")
    public RespResult<Void> retryIngest(@RequestParam("documentId") Long documentId) {
        knowledgeService.retryIngest(documentId);
        return RespResult.success();
    }

    /**
     * 知识库文档分页列表
     */
    @GetMapping("/doc/select-page")
    public RespPageResult<KnowledgeDocumentPO> selectDocPage(KnowledgeDocumentQuery query) {
        return knowledgeDocumentService.selectPageList(query);
    }
}
