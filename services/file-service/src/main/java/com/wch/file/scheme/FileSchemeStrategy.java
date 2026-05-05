package com.wch.file.scheme;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author: Jie Bugui
 * @create: 2026-04-29 14:53
 */
public interface FileSchemeStrategy {
    void saveChunk(MultipartFile file, String md5, Integer chunk, Integer chunks, String name) throws Exception;
}
