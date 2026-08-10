package com.wch.rag.controller;

import com.wch.common.model.resp.RespPageResult;
import com.wch.common.model.resp.RespResult;
import com.wch.rag.model.po.AiModelPO;
import com.wch.rag.model.req.AiModelDTO;
import com.wch.rag.model.req.AiModelQuery;
import com.wch.rag.service.AiModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author: Jie Bugui
 * @create: 2026-05-01 0:56
 */
@RestController
@RequestMapping("/ai-model")
public class AiModelController {

    private AiModelService aiModelService;

    @Autowired
    void setService(
            AiModelService aiModelService
    ) {
        this.aiModelService = aiModelService;
    }

    /**
     * 模型新增
     */
    @PostMapping("/add")
    public RespResult<Void> add(@RequestBody AiModelDTO dto) {
        aiModelService.saveModel(AiModelDTO.transfer(dto));
        return RespResult.success();
    }

    /**
     * 模型删除
     */
    @PostMapping("/delete")
    public RespResult<Void> delete(@RequestParam("id") String id) {
        aiModelService.removeById(id);
        return RespResult.success();
    }

    /**
     * 模型编辑
     */
    @PostMapping("/edit")
    public RespResult<Void> edit(@RequestBody AiModelDTO dto) {
        aiModelService.updateModel(AiModelDTO.transfer(dto));
        return RespResult.success();
    }

    /**
     * 模型查看
     */
    @GetMapping("/select")
    public RespResult<AiModelPO> select(@RequestParam("id") String id) {
        return RespResult.success(aiModelService.getById(id));
    }

    /**
     * 模型列表
     */
    @GetMapping("/select-page")
    public RespPageResult<AiModelPO> selectPage(AiModelQuery query) {
        return aiModelService.selectPageList(query);
    }

}
