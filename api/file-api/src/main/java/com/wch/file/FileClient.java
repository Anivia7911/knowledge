package com.wch.file;

import com.wch.common.model.resp.RespResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@FeignClient("file-service")
public interface FileClient {
    @PostMapping(value = "/upload")
    RespResult<Void> upload(@RequestParam("file") MultipartFile file) throws IOException;

    @GetMapping(value = "/upload/check")
    RespResult<Boolean> checkMD5(@RequestParam("md5") String md5);

    @PostMapping(value = "/upload/chunk")
    RespResult<Void> uploadChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("md5") String md5,
            @RequestParam("chunk") Integer chunk,
            @RequestParam("chucks") Integer chucks,
            @RequestParam("name") String name
    ) throws IOException;
}
