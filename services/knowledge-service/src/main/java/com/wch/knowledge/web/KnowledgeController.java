package com.wch.knowledge.web;


import com.wch.knowledge.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {


    private KnowledgeService knowledgeService;

    @Autowired
    void setService(
            KnowledgeService knowledgeService
    ) {
        this.knowledgeService = knowledgeService;
    }

    @PostMapping("/file-upload")
    public void uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        knowledgeService.uploadFile(file);
    }
}
