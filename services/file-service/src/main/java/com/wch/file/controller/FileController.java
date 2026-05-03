package com.wch.file.controller;

import com.wch.common.model.resp.RespResult;
import com.wch.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;

import java.io.IOException;

/**
 * @author: Jie Bugui
 * @create: 2025-04-22 16:21
 */
@RestController
@RequestMapping("/file")
public class FileController {

    private FileService fileService;

    @Autowired
    void setBean(
            FileService fileService
    ) {
        this.fileService = fileService;
    }
    /**
     * 文件上传
     */
    @PostMapping(value = "/upload")
    public RespResult<Void> upload(@RequestParam("file") MultipartFile file) throws IOException {
        fileService.upload(file);
        return RespResult.success();
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
    public View download(@RequestParam("id") String id) throws IOException {
        return fileService.download(id);
    }
}
