package com.wch.file.service;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wch.common.model.req.PageQuery;
import com.wch.common.model.resp.RespPageResult;
import com.wch.common.trivial.enumdata.FileSchemeEnum;
import com.wch.file.FileSetting;
import com.wch.file.model.dto.FileItemDTO;
import com.wch.file.model.po.FileBodyPO;
import com.wch.file.model.po.FileHeaderPO;
import com.wch.file.scheme.FileSchemeContext;
import com.wch.file.scheme.FileSchemeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public Long upload(MultipartFile file) {
        String fileName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();

        // 计算 MD5，用于秒传判断
        String md5;
        try (InputStream in = file.getInputStream()) {
            md5 = DigestUtils.md5DigestAsHex(in);
        } catch (IOException e) {
            throw new RuntimeException("读取上传文件失败", e);
        }

        // 文件体已存在则直接复用（秒传）
        FileBodyPO fileBodyPO = fileBodyService.lambdaQuery().eq(FileBodyPO::getMd5, md5).one();
        if (fileBodyPO == null) {
            String storedName = md5 + "_" + fileName;
            File dir = new File(setting.getTempUploadPath());
            if (!dir.exists() && !dir.mkdirs()) {
                throw new RuntimeException("创建上传目录失败: " + dir.getAbsolutePath());
            }
            File dest = new File(dir, storedName);
            try {
                file.transferTo(dest);
            } catch (IOException e) {
                throw new RuntimeException("保存上传文件失败", e);
            }

            fileBodyPO = new FileBodyPO();
            fileBodyPO.setSize(file.getSize());
            fileBodyPO.setMd5(md5);
            fileBodyPO.setType(FileUtil.getType(dest));
            fileBodyPO.setScheme(FileSchemeEnum.valueOf(setting.getScheme()));
            // 存储相对路径（仅文件名），避免环境变更导致路径失效
            fileBodyPO.setPath(storedName);
            fileBodyService.save(fileBodyPO);
        } else {
            // 秒传命中但物理文件不存在（如旧数据来自其他环境），重新保存文件
            String resolvedPath = resolveFilePath(fileBodyPO.getPath());
            if (!new File(resolvedPath).exists()) {
                String storedName = md5 + "_" + fileName;
                File dir = new File(setting.getTempUploadPath());
                if (!dir.exists() && !dir.mkdirs()) {
                    throw new RuntimeException("创建上传目录失败: " + dir.getAbsolutePath());
                }
                File dest = new File(dir, storedName);
                try {
                    file.transferTo(dest);
                } catch (IOException e) {
                    throw new RuntimeException("保存上传文件失败", e);
                }
                // 更新路径（文件名可能因环境不同而变化）
                fileBodyPO.setPath(storedName);
                fileBodyService.updateById(fileBodyPO);
                log.info("秒传物理文件缺失，已重新保存: {}", storedName);
            }
        }

        FileHeaderPO fileHeaderPO = new FileHeaderPO();
        fileHeaderPO.setName(fileName);
        fileHeaderPO.setBodyId(fileBodyPO.getId());
        fileHeaderPO.setDeleted(0);
        fileHeaderService.save(fileHeaderPO);
        return fileHeaderPO.getId();
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
            log.error(e.getMessage());
            throw new RuntimeException("分片上传并处理失败", e);
        }
    }

    public Resource download(String id) {
        FileHeaderPO header = fileHeaderService.getById(id);
        if (header == null) {
            throw new RuntimeException("文件不存在");
        }
        FileBodyPO body = fileBodyService.getById(header.getBodyId());
        if (body == null || body.getPath() == null) {
            throw new RuntimeException("文件体不存在");
        }
        FileSchemeStrategy strategy = fileSchemeContext.getFileSchemeStrategy(setting.getScheme());
        return strategy.getFile(resolveFilePath(body.getPath()));
    }

    public FileHeaderPO getFileInfo(String id) {
        return fileHeaderService.getById(id);
    }

    /**
     * 解析文件存储路径：将数据库中存储的路径解析为绝对路径
     * <p>
     * 兼容旧数据：如果存储的是绝对路径且文件存在，直接使用；
     * 否则基于 tempUploadPath 解析。
     * </p>
     */
    public String resolveFilePath(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return storedPath;
        }
        File storedFile = new File(storedPath);
        // 1. 绝对路径且文件存在（兼容旧数据），直接返回
        if (storedFile.isAbsolute() && storedFile.exists()) {
            return storedPath;
        }
        // 2. 提取文件名，基于 tempUploadPath 解析
        //    兼容旧绝对路径（如 /tmp/knowledge/upload/xxx.docx → 取 xxx.docx）
        //    以及新数据（直接存的文件名）
        String fileName = storedFile.getName();
        Path basePath = Paths.get(setting.getTempUploadPath());
        return basePath.resolve(fileName).toString();
    }

    /**
     * 删除文件：逻辑删除文件头；若文件体不再被其他文件头引用，则同时删除文件体与磁盘文件
     */
    @Transactional(rollbackFor = Throwable.class)
    public void delete(String id) {
        FileHeaderPO header = fileHeaderService.getById(id);
        if (header == null) {
            throw new RuntimeException("文件不存在");
        }
        fileHeaderService.removeById(id);

        Long bodyId = header.getBodyId();
        if (bodyId == null) {
            return;
        }
        // 检查文件体是否还被其他文件头引用
        long refCount = fileHeaderService.lambdaQuery()
                .eq(FileHeaderPO::getBodyId, bodyId)
                .count();
        if (refCount > 0) {
            return;
        }

        FileBodyPO body = fileBodyService.getById(bodyId);
        fileBodyService.removeById(bodyId);
        // 删除磁盘文件
        if (body != null && body.getPath() != null && !body.getPath().isBlank()) {
            File file = new File(resolveFilePath(body.getPath()));
            if (file.exists() && !file.delete()) {
                log.warn("磁盘文件删除失败: {}", body.getPath());
            }
        }
    }

    /**
     * 文件分页列表（file_header + file_body 组合展示信息）
     */
    public RespPageResult<FileItemDTO> selectPageList(PageQuery query) {
        Page<FileHeaderPO> headerPage = fileHeaderService.lambdaQuery()
                .orderByDesc(FileHeaderPO::getCreateDate)
                .page(new Page<>(query.getPage(), query.getRows()));

        List<FileHeaderPO> headers = headerPage.getRecords();
        if (headers == null || headers.isEmpty()) {
            return RespPageResult.success(null, headerPage.getCurrent(), headerPage.getSize(), headerPage.getTotal());
        }

        // 批量查询文件体，补充大小与类型信息
        List<Long> bodyIds = headers.stream()
                .map(FileHeaderPO::getBodyId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, FileBodyPO> bodyMap = bodyIds.isEmpty()
                ? Map.of()
                : fileBodyService.listByIds(bodyIds).stream()
                        .collect(Collectors.toMap(FileBodyPO::getId, Function.identity()));

        List<FileItemDTO> items = headers.stream().map(header -> {
            FileItemDTO item = new FileItemDTO();
            item.setId(header.getId());
            item.setName(header.getName());
            item.setCreateDate(header.getCreateDate());
            FileBodyPO body = bodyMap.get(header.getBodyId());
            if (body != null) {
                item.setSize(body.getSize());
                item.setType(body.getType());
            }
            return item;
        }).collect(Collectors.toList());

        return RespPageResult.success(items, headerPage.getCurrent(), headerPage.getSize(), headerPage.getTotal());
    }
}
