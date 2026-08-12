package com.wch.file.scheme.strategy.local;

import cn.hutool.core.io.FileUtil;
import com.wch.common.trivial.enumdata.FileSchemeEnum;
import com.wch.file.FileSetting;
import com.wch.file.model.po.FileBodyPO;
import com.wch.file.model.po.FileHeaderPO;
import com.wch.file.scheme.FileSchemeStrategy;
import com.wch.file.service.FileBodyService;
import com.wch.file.service.FileHeaderService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author: Jie Bugui
 * @create: 2026-04-29 15:00
 */
@Service
public class FileSchemeLocalService implements FileSchemeStrategy {

    private static final String CHUNK_UPLOAD_KEY = "file:upload:chunks:";
    private static final String LOCK_MERGE_KEY = "file:lock:merge:";
    private static final String RELEASE_LOCK_LUA =" if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    private FileSetting setting;
    private FileHeaderService fileHeaderService;
    private FileBodyService fileBodyService;
    private StringRedisTemplate redisTemplate;


    @Autowired
    void setService(
            FileSetting setting,
            FileHeaderService fileHeaderService,
            FileBodyService fileBodyService,
            StringRedisTemplate redisTemplate
    ) {
        this.setting = setting;
        this.fileHeaderService = fileHeaderService;
        this.fileBodyService = fileBodyService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void saveChunk(MultipartFile file, String md5, Integer chunk, Integer chunks, String name) throws Exception {
        //检查当前分片是否已经上传
        if (checkChunk(md5, chunk)) {
            return;
        }

        File tempDir = new File(setting.getTempUploadPath());
        if (!tempDir.exists()) tempDir.mkdirs();

        // 使用 md5 作为隔离子目录
        File taskDir = new File(tempDir, md5);
        if (!taskDir.exists()) taskDir.mkdirs();

        File chunkFile = new File(taskDir, String.valueOf(chunk));
        if (!chunkFile.exists()) {
            file.transferTo(chunkFile);
        }


        //上传成功创建记录
        redisTemplate.opsForValue().setBit(CHUNK_UPLOAD_KEY + md5, chunk, true);
        redisTemplate.expire(CHUNK_UPLOAD_KEY + md5, 24, TimeUnit.HOURS);

        // 检查是否所有分片都已就绪
        if (isAllChunksReady(CHUNK_UPLOAD_KEY + md5, chunks)) {
            attemptMerge(tempDir, taskDir, md5, name, chunks);
        }
    }

    private boolean checkChunk(String md5, Integer chunk) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().getBit(CHUNK_UPLOAD_KEY + md5, chunk));
    }

    private void attemptMerge(File tempDir, File taskDir, String md5, String name, Integer chunks) {
        String lockKey = LOCK_MERGE_KEY + md5;
        String requestId = UUID.randomUUID().toString(); // 锁的持有者标识

        try {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, requestId, 5, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(locked)) {
                // 双重检查
                if (isAllChunksReady(CHUNK_UPLOAD_KEY + md5, chunks)) {
                    doMergeAndSave(tempDir, taskDir, md5, name, chunks);
                    redisTemplate.delete(CHUNK_UPLOAD_KEY + md5);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            releaseDistributedLock(lockKey, requestId);
        }
    }


    private void releaseDistributedLock(String lockKey, String requestId) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(RELEASE_LOCK_LUA, Long.class);
        redisTemplate.execute(redisScript, Collections.singletonList(lockKey), requestId);
    }

    private boolean isAllChunksReady(String redisKey, int chunks) {
        Long uploadedCount = redisTemplate.execute((RedisCallback<Long>) connection ->
                connection.bitCount(redisKey.getBytes()));
        return uploadedCount != null && uploadedCount.intValue() == chunks;
    }

    private void doMergeAndSave(File tempDir, File taskDir, String md5, String fileName, int chunks) throws IOException {
        // 开始合并 (最终文件名加上 md5 防止同名覆盖)
        File finalFile = new File(tempDir, md5 + "_" + fileName);
        if (finalFile.exists()) {
            return;
        }

        try (FileChannel destChannel = new FileOutputStream(finalFile).getChannel()) {
            for (int i = 0; i < chunks; i++) {
                File partFile = new File(taskDir, String.valueOf(i));
                try (FileChannel srcChannel = new FileInputStream(partFile).getChannel()) {
                    srcChannel.transferTo(0, srcChannel.size(), destChannel);
                }
            }
        }

        saveFileData(finalFile, md5, fileName);
        FileUtils.deleteQuietly(taskDir);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void saveFileData(File finalFile, String md5, String fileName) {
        FileBodyPO fileBodyPO = new FileBodyPO();
        fileBodyPO.setSize(finalFile.length());
        fileBodyPO.setMd5(md5);
        fileBodyPO.setType(FileUtil.getType(finalFile));
        fileBodyPO.setScheme(FileSchemeEnum.valueOf(setting.getScheme()));
        // 存储相对路径（仅文件名），避免环境变更导致路径失效
        fileBodyPO.setPath(finalFile.getName());
        fileBodyService.save(fileBodyPO);

        FileHeaderPO fileHeaderPO = new FileHeaderPO();
        fileHeaderPO.setName(fileName);
        fileHeaderPO.setBodyId(fileBodyPO.getId());
        fileHeaderPO.setDeleted(0);
        fileHeaderService.save(fileHeaderPO);
    }

    @Override
    public Resource getFile(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                throw new RuntimeException("文件不存在: " + path);
            }
            return new InputStreamResource(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败: " + path, e);
        }
    }
}
