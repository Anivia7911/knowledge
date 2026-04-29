package com.wch.file.controller;

import com.wch.common.model.resp.RespResult;
import com.wch.file.service.FileService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
     * @param file
     * @return
     */
    @PostMapping(value = "/upload")
    public RespResult upload(@RequestBody MultipartFile file) throws IOException {
        fileService.upload(file);
        return RespResult.success();
    }

    /**
     * 文件下载
     * @param name
     * @return
     */
//    @GetMapping(value = "download")
//    public void download(@RequestParam("name") String name, HttpServletResponse response) throws IOException {
//        byte[] download = fileService.download(name);
//        ServletOutputStream os = response.getOutputStream();
//        os.write(download);
//        os.flush();
//        os.close();
//    }
}
