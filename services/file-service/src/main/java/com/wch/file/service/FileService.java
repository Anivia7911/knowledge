package com.wch.file.service;

import cn.hutool.core.io.FileUtil;
import com.wch.common.trivial.enumdata.FileSchemeEnum;
import com.wch.file.FileSetting;
import com.wch.file.model.po.FileBodyPO;
import com.wch.file.model.po.FileHeaderPO;
import com.wch.file.scheme.FileSchemeContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

/**
 * @author: Jie Bugui
 * @create: 2026-04-29 4:06
 */
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

    @Transactional(rollbackFor = Throwable.class)
    public void uploadChunk(MultipartFile file, Integer chunk, Integer chunks, String name) {
        try {
            File tempDir = new File(setting.getTempUploadPath());
            if (!tempDir.exists()) tempDir.mkdirs();

            File chunkFile = new File(tempDir, chunk + "_" + name);
            if (!chunkFile.exists()) {
                file.transferTo(chunkFile);
            }

            checkAndMerge(tempDir, name, chunks);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkAndMerge(File tempDir, String fileName, int chunks) {
        // 检查是否所有分片都已就绪
        for (int i = 0; i < chunks; i++) {
            if (!new File(tempDir, i + "_" + fileName).exists()) {
                return; // 还有分片没到，退出
            }
        }

        // 开始合并
        File finalFile = new File(tempDir, fileName);
        try (FileChannel destChannel = new FileOutputStream(finalFile).getChannel()) {
            for (int i = 0; i < chunks; i++) {
                File partFile = new File(tempDir, i + "_" + fileName);
                try (FileChannel srcChannel = new FileInputStream(partFile).getChannel()) {
                    srcChannel.transferTo(0, srcChannel.size(), destChannel);
                }
                Files.delete(partFile.toPath()); // 合并完立即删除
            }

            FileBodyPO fileBodyPO = new FileBodyPO();
            fileBodyPO.setSize(finalFile.length());
            fileBodyPO.setType(FileUtil.getType(finalFile));
            fileBodyPO.setScheme(FileSchemeEnum.valueOf(setting.getScheme()));
            fileBodyService.save(fileBodyPO);

            FileHeaderPO fileHeaderPO = new FileHeaderPO();
            fileHeaderPO.setName(fileName);
            fileHeaderPO.setBodyId(fileBodyPO.getId());
            fileHeaderPO.setBody(fileBodyPO);
            fileHeaderPO.setDeleted(0);


            fileHeaderService.save(fileHeaderPO);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public View download(String id) {
        return null;
    }
}
