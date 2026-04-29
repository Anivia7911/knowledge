package com.wch.file.service;

import cn.hutool.core.io.FileUtil;
import com.wch.common.trivial.enumdata.FileSchemeEnum;
import com.wch.file.FileSetting;
import com.wch.file.model.po.FileBodyPO;
import com.wch.file.model.po.FileHeaderPO;
import com.wch.file.scheme.FileSchemeContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;

import java.io.*;
import java.nio.channels.FileChannel;
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


    @Autowired
    void setService(
            FileSetting setting,
            FileSchemeContext fileSchemeContext,
            FileHeaderService fileHeaderService,
            FileBodyService fileBodyService
    ) {
        this.setting = setting;
        this.fileSchemeContext = fileSchemeContext;
        this.fileHeaderService = fileHeaderService;
        this.fileBodyService = fileBodyService;
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

    @Transactional(rollbackFor = Throwable.class)
    public void uploadChunk(MultipartFile file, String md5, Integer chunk, Integer chunks, String name) {
        try {
            File tempDir = new File(setting.getTempUploadPath());
            if (!tempDir.exists()) tempDir.mkdirs();

            // 使用 md5 作为隔离子目录
            File taskDir = new File(tempDir, md5);
            if (!taskDir.exists()) taskDir.mkdirs();

            File chunkFile = new File(taskDir, String.valueOf(chunk));
            if (!chunkFile.exists()) {
                file.transferTo(chunkFile);
            }


            // 检查是否所有分片都已就绪
            if (isAllChunksReady(taskDir, chunks)) {
                // 使用 md5.intern() 确保同一文件的合并操作是串行的
                synchronized (md5.intern()) {
                    // 双重检查
                    if (isAllChunksReady(taskDir, chunks)) {
                        doMergeAndSave(tempDir, taskDir, md5, name, chunks);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("分片上传并处理失败", e);
        }
    }

    private boolean isAllChunksReady(File taskDir, int chunks) {
        for (int i = 0; i < chunks; i++) {
            if (!new File(taskDir, String.valueOf(i)).exists()) {
                return false;
            }
        }
        return true;
    }

    private void doMergeAndSave(File tempDir, File taskDir, String md5, String fileName, int chunks) throws IOException {
        // 开始合并 (最终文件名加上 md5 防止同名覆盖)
        File finalFile = new File(tempDir, md5 + "_" + fileName);
        try {
            if (finalFile.exists()) return;

            try (FileChannel destChannel = new FileOutputStream(finalFile).getChannel()) {
                for (int i = 0; i < chunks; i++) {
                    File partFile = new File(taskDir, String.valueOf(i));
                    try (FileChannel srcChannel = new FileInputStream(partFile).getChannel()) {
                        srcChannel.transferTo(0, srcChannel.size(), destChannel);
                    }
                }
            }

            FileBodyPO fileBodyPO = new FileBodyPO();
            fileBodyPO.setSize(finalFile.length());
            fileBodyPO.setMd5(md5);
            fileBodyPO.setType(FileUtil.getType(finalFile));
            fileBodyPO.setScheme(FileSchemeEnum.valueOf(setting.getScheme()));
            fileBodyService.save(fileBodyPO);

            FileHeaderPO fileHeaderPO = new FileHeaderPO();
            fileHeaderPO.setName(fileName);
            fileHeaderPO.setBodyId(fileBodyPO.getId());
            fileHeaderPO.setDeleted(0);
            fileHeaderService.save(fileHeaderPO);


        } finally {
            try {
                if (taskDir.exists()) {
                    // 成功后清理隔离目录
                    FileUtils.deleteDirectory(taskDir);
                }
            } catch (IOException e) {
                log.error("清理临时分片目录失败: {}", taskDir.getAbsolutePath(), e);
            }

        }
    }

    public View download(String id) {
        return null;
    }
}
