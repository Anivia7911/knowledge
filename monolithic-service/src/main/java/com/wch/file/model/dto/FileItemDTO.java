package com.wch.file.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件列表展示项（file_header + file_body 组合）
 */
@Data
public class FileItemDTO {

    private Long id;

    /**
     * 文件名
     */
    private String name;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 文件类型
     */
    private String type;

    /**
     * 上传时间
     */
    private LocalDateTime createDate;
}
