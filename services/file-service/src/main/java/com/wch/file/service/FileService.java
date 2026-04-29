package com.wch.file.service;

import cn.hutool.core.io.FileUtil;
import com.wch.common.trivial.enumdata.FileSchemeEnum;
import com.wch.file.FileSetting;
import com.wch.file.model.po.FileBodyPO;
import com.wch.file.model.po.FileHeaderPO;
import com.wch.file.scheme.FileSchemeContext;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void uploadChunk(HttpServletRequest request) {
        String fileName = null;
        try {
            if (!JakartaServletFileUpload.isMultipartContent(request)) return;
            DiskFileItemFactory factory = DiskFileItemFactory.builder().get();
            JakartaServletFileUpload upload = new JakartaServletFileUpload(factory);
            List<FileItem> items = upload.parseRequest(request);
            Map<String, String> paramMap = new HashMap<>();
            FileItem fileItem = null;
            for (FileItem item : items) {
                if (item.isFormField()) {
                    paramMap.put(item.getFieldName(), item.getString(String.valueOf(StandardCharsets.UTF_8)));
                } else {
                    fileItem = item;
                }
            }
            int chunk = Integer.parseInt(paramMap.getOrDefault("chunk", "0"));
            int chunks = Integer.parseInt(paramMap.getOrDefault("chucks", "1"));

            fileName = new File(paramMap.get("name")).getName();
            if (fileItem == null || fileName == null) return;

            File tempDir = new File(setting.getTempUploadPath());
            if (!tempDir.exists()) tempDir.mkdirs();

            File chunkFile = new File(tempDir, chunk + "_" + fileName);
            if (!chunkFile.exists()) {
                fileItem.write(chunkFile);
            }

            checkAndMerge(tempDir, fileName, chunks);
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

    public void uploadChunk2(HttpServletRequest request) {
        Integer chunk = null;
        Integer chunks = null;
        String name = null;
        BufferedOutputStream os = null;
        try {
            if (JakartaServletFileUpload.isMultipartContent(request)) {
                DiskFileItemFactory factory = DiskFileItemFactory.builder().get();
                JakartaServletFileUpload upload = new JakartaServletFileUpload(factory);
                upload.setFileSizeMax(setting.getFileChunkSizeMax() * 1024 * 1024 * 1024);
                upload.setSizeMax(setting.getFileSizeMax() * 1024 * 1024 * 1024);
                List<FileItem> items = upload.parseRequest(request);

                for (FileItem item : items) {
                    if (item.isFormField()) {
                        if ("chunk".equals(item.getFieldName())) {
                            chunk = Integer.parseInt(item.getString("utf-8"));
                        }
                        if ("chucks".equals(item.getFieldName())) {
                            chunks = Integer.parseInt(item.getString("utf-8"));
                        }
                        if ("name".equals(item.getFieldName())) {
                            name = item.getString("utf-8");
                        }
                    }
                }

                for (FileItem item : items) {
                    if (!item.isFormField()) {
                        String temFileName =  name;
                        if (name != null) {
                            if (chunk != null) {
                                temFileName = chunk + "_" + name;
                            }
                            File temFile = new File(setting.getTempUploadPath(), temFileName);
                            //断点续传
                            if (!temFile.exists()) {
                                item.write(temFile);
                            }
                        }
                    }
                }
                //文件合并
                if (chunk != null && chunk.intValue() == chunks.intValue() - 1) {
                    File temFile = new File(setting.getTempUploadPath(), name);
                    os = new BufferedOutputStream(new FileOutputStream(temFile));

                    for (Integer i = 0; i < chunks; i++) {
                        File file = new File(setting.getTempUploadPath(), i + "_" +name);
                        while (file.exists()) {
                            Thread.sleep(100);
                        }
                        byte[] bytes = FileUtils.readFileToByteArray(file);
                        os.write(bytes);
                        os.flush();
                        file.delete();
                    }
                    os.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public View download(String id) {
        return null;
    }
}
