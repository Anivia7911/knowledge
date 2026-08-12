package com.wch.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: Jie Bugui
 * @create: 2026-04-29 19:18
 */
@Component
@ConfigurationProperties("file-service")
@Data
public class FileSetting {
    private String scheme = "LOCAL";

    /**
     * 上传文件大小，M
     */
    private Integer fileSizeMax = 10;

    /**
     * 分片上传文件大小限制，M
     */
    private Integer fileChunkSizeMax = 5;

    /**
     * 文件临时上传目录
     */
    private String tempUploadPath = "/tmp/knowledge/upload";
}
