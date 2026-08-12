package com.wch.file.controller;

import com.wch.common.model.req.PageQuery;
import com.wch.common.model.resp.RespPageResult;
import com.wch.common.model.resp.RespResult;
import com.wch.common.util.FileNameUtil;
import com.wch.file.model.dto.FileItemDTO;
import com.wch.file.model.po.FileBodyPO;
import com.wch.file.model.po.FileHeaderPO;
import com.wch.file.service.FileBodyService;
import com.wch.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Jie Bugui
 * @create: 2025-04-22 16:21
 */
@RestController
@RequestMapping("/file")
public class FileController {

    private FileService fileService;
    private FileBodyService fileBodyService;

    @Autowired
    void setBean(
            FileService fileService,
            FileBodyService fileBodyService
    ) {
        this.fileService = fileService;
        this.fileBodyService = fileBodyService;
    }

    /**
     * 文件上传
     */
    @PostMapping(value = "/upload")
    public RespResult<Long> upload(@RequestParam("file") MultipartFile file) throws IOException {
        Long fileId = fileService.upload(file);
        return RespResult.success(fileId);
    }

    /**
     * 校验文件是否已存在（秒传）
     */
    @GetMapping(value = "/upload/check")
    public RespResult<Boolean> checkMD5(@RequestParam("md5") String md5) {
        boolean exists = fileService.checkMD5(md5);
        return RespResult.success(exists);
    }

    /**
     * 文件分片上传
     */
    @PostMapping(value = "/upload/chunk")
    public RespResult<Void> uploadChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("md5") String md5,
            @RequestParam("chunk") Integer chunk,
            @RequestParam("chucks") Integer chucks,
            @RequestParam("name") String name
    ) throws IOException {
        fileService.uploadChunk(file, md5, chunk, chucks, name);
        return RespResult.success();
    }

    /**
     * 文件下载
     */
    @GetMapping(value = "download")
    public ResponseEntity<Resource> download(@RequestParam("id") String id) {
        FileHeaderPO header = fileService.getFileInfo(id);
        Resource resource = fileService.download(id);
        String fileName = header != null ? header.getName() : "file";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, FileNameUtil.contentDisposition("attachment", fileName))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * 文件删除
     */
    @PostMapping(value = "/delete")
    public RespResult<Void> delete(@RequestParam("id") String id) {
        fileService.delete(id);
        return RespResult.success();
    }

    /**
     * 文件分页列表
     */
    @GetMapping(value = "/select-page")
    public RespPageResult<FileItemDTO> selectPage(PageQuery query) {
        return fileService.selectPageList(query);
    }

    /**
     * 获取文件信息
     */
    @GetMapping(value = "/info")
    public RespResult<Map<String, Object>> getFileInfo(@RequestParam("id") String id) {
        FileHeaderPO header = fileService.getFileInfo(id);
        if (header == null) {
            return RespResult.error();
        }
        Map<String, Object> info = new HashMap<>();
        info.put("id", header.getId());
        info.put("name", header.getName());
        info.put("bodyId", header.getBodyId());

        FileBodyPO body = fileBodyService.getById(header.getBodyId());
        if (body != null) {
            info.put("size", body.getSize());
            info.put("type", body.getType());
            info.put("path", fileService.resolveFilePath(body.getPath()));
        }
        return RespResult.success(info);
    }
}
