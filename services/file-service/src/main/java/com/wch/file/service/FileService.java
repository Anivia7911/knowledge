package com.wch.file.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.wch.file.model.po.FileBodyPO;

import com.wch.file.model.po.FileHeaderPO;
import com.wch.file.scheme.FileSchemeContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: Jie Bugui
 * @create: 2026-04-29 4:06
 */
@Service
public class FileService {
    private FileSchemeContext fileSchemeContext;
    private FileHeaderService fileHeaderService;
    private FileBodyService fileBodyService;


    @Autowired
    void setService(
            FileSchemeContext fileSchemeContext,
            FileHeaderService fileHeaderService,
            FileBodyService fileBodyService
    ) {
        this.fileSchemeContext = fileSchemeContext;
        this.fileHeaderService = fileHeaderService;
        this.fileBodyService = fileBodyService;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void upload(MultipartFile file) {
        FileBodyPO fileBodyPO =  new FileBodyPO();
        fileBodyPO.setSize(file.getSize());
        fileBodyService.save(fileBodyPO);

        FileHeaderPO fileHeaderPO = new FileHeaderPO();
        fileHeaderPO.setName(file.getOriginalFilename());
        fileHeaderPO.setBodyId(fileBodyPO.getId());
        fileHeaderPO.setBody(fileBodyPO);
        fileHeaderPO.setDeleted(0);


        fileHeaderService.save(fileHeaderPO);
    }
}
