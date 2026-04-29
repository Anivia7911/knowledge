package com.wch.file.controller;

import com.wch.common.model.resp.RespResult;
import com.wch.file.service.FileService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Cleanup;
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
    public RespResult upload(@RequestBody MultipartFile file) throws IOException {
        fileService.upload(file);
        return RespResult.success();
    }

    /**
     * 文件分片上传
     */
    @PostMapping(value = "/upload/chunk")
    public RespResult uploadChunk(HttpServletRequest request) throws IOException {
        fileService.uploadChunk(request);
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
