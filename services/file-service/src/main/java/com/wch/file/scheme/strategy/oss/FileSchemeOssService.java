package com.wch.file.scheme.strategy.oss;

import com.wch.file.scheme.FileSchemeStrategy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: Jie Bugui
 * @create: 2026-04-29 15:00
 */
@Service
public class FileSchemeOssService implements FileSchemeStrategy {
    @Override
    public void saveChunk(MultipartFile file, String md5, Integer chunk, Integer chunks, String name) throws Exception {

    }
}
