package com.wch.file.service;

import com.wch.file.FileSetting;
import com.wch.file.model.po.FileBodyPO;
import com.wch.file.model.po.FileHeaderPO;
import com.wch.file.scheme.FileSchemeContext;
import com.wch.file.scheme.FileSchemeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;

import java.io.*;

/**
 * @author: Jie Bugui
 * @create: 2026-04-29 4:06
 */
@Slf4j
@Service
public class FileService {
    private FileSetting setting;
    private FileSchemeContext fileSchemeContext;
    private FileHeaderService fileHeaderService;
    private FileBodyService fileBodyService;
    private StringRedisTemplate redisTemplate;


    @Autowired
    void setService(
            FileSetting setting,
            FileSchemeContext fileSchemeContext,
            FileHeaderService fileHeaderService,
            FileBodyService fileBodyService,
            StringRedisTemplate redisTemplate
    ) {
        this.setting = setting;
        this.fileSchemeContext = fileSchemeContext;
        this.fileHeaderService = fileHeaderService;
        this.fileBodyService = fileBodyService;
        this.redisTemplate = redisTemplate;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void upload(MultipartFile file) {
        FileBodyPO fileBodyPO = new FileBodyPO();
        fileBodyPO.setSize(file.getSize());
        fileBodyService.save(fileBodyPO);

        FileHeaderPO fileHeaderPO = new FileHeaderPO();
        fileHeaderPO.setName(file.getOriginalFilename());
        fileHeaderPO.setBodyId(fileBodyPO.getId());
        fileHeaderPO.setBody(fileBodyPO);
        fileHeaderPO.setDeleted(0);


        fileHeaderService.save(fileHeaderPO);
    }

    public boolean checkMD5(String md5) {
        // 检查数据库中是否存在该 MD5 的记录
        return fileBodyService.lambdaQuery().eq(FileBodyPO::getMd5, md5).count() > 0;
    }

    public void uploadChunk(MultipartFile file, String md5, Integer chunk, Integer chunks, String name) {
        try {
            FileSchemeStrategy fileSchemeService = fileSchemeContext.getFileSchemeStrategy(setting.getScheme());
            fileSchemeService.saveChunk(file, md5, chunk, chunks, name);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("分片上传并处理失败", e);
        }
    }

    public View download(String id) {
        return null;
    }
}
