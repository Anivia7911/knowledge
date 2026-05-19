package com.wch.knowledge.service;

import com.wch.file.FileClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final FileClient fileClient;

    public void uploadFile(MultipartFile file) throws IOException {
        fileClient.upload(file);
    }
}
