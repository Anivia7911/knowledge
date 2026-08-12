package com.wch.preview.service;

import com.wch.preview.config.PreviewSetting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 文档转换服务（参照 kkfileview 实现思路）：
 * Office 文档通过 LibreOffice 无头模式转换为 PDF，转换结果按文件指纹缓存，避免重复转换
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreviewConvertService {

    private final PreviewSetting setting;

    /**
     * 支持转 PDF 预览的 Office 类文档类型
     */
    private static final Set<String> OFFICE_TYPES = Set.of(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "odt", "ods", "odp", "wps", "et", "dps", "csv"
    );

    private static final Set<String> IMAGE_TYPES = Set.of(
            "png", "jpg", "jpeg", "gif", "bmp", "webp", "svg"
    );

    private static final Set<String> PDF_TYPES = Set.of("pdf");

    /**
     * 每个缓存key一把锁，避免同一文件并发重复转换
     */
    private final Map<String, Object> convertLocks = new ConcurrentHashMap<>();

    public boolean isOffice(String type) {
        return type != null && OFFICE_TYPES.contains(type.toLowerCase());
    }

    public boolean isImage(String type) {
        return type != null && IMAGE_TYPES.contains(type.toLowerCase());
    }

    public boolean isPdf(String type) {
        return type != null && PDF_TYPES.contains(type.toLowerCase());
    }

    /**
     * 将 Office 文档转换为 PDF，返回缓存的 PDF 文件
     *
     * @param sourcePath 源文件路径
     * @param cacheKey   缓存键（通常为文件体MD5）
     */
    public File convertToPdf(String sourcePath, String cacheKey) throws Exception {
        Path cacheDir = Paths.get(setting.getCachePath());
        Files.createDirectories(cacheDir);
        File cached = cacheDir.resolve(cacheKey + ".pdf").toFile();
        if (cached.exists()) {
            return cached;
        }

        Object lock = convertLocks.computeIfAbsent(cacheKey, k -> new Object());
        synchronized (lock) {
            if (cached.exists()) {
                return cached;
            }
            doConvert(sourcePath, cacheDir, cached);
            return cached;
        }
    }

    private void doConvert(String sourcePath, Path cacheDir, File cached) throws Exception {
        File source = new File(sourcePath);
        if (!source.exists()) {
            throw new RuntimeException("源文件不存在");
        }
        File soffice = new File(setting.getSofficePath());
        if (!soffice.exists()) {
            throw new RuntimeException("未安装LibreOffice或路径配置错误，无法预览Office文档");
        }

        // LibreOffice 要求源文件带正确扩展名，复制到临时目录保证扩展名正确
        String ext = extOf(source.getName());
        Path workDir = Files.createTempDirectory("kk-preview-");
        Path workFile = workDir.resolve("source." + ext);
        Files.copy(source.toPath(), workFile, StandardCopyOption.REPLACE_EXISTING);

        // 为每次转换创建独立的用户配置目录，避免与已有 LibreOffice 实例冲突
        Path userInstallDir = Files.createTempDirectory("lo-profile-");
        String userInstallUrl = "file://" + userInstallDir.toAbsolutePath();

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    soffice.getAbsolutePath(),
                    "-env:UserInstallation=" + userInstallUrl,
                    "--headless", "--norestore", "--invisible",
                    "--convert-to", "pdf",
                    "--outdir", workDir.toString(),
                    workFile.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            boolean finished = process.waitFor(setting.getConvertTimeoutSeconds(), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("文档转换超时");
            }
            if (process.exitValue() != 0) {
                String output = new String(process.getInputStream().readAllBytes());
                log.error("LibreOffice转换失败: {}", output);
                throw new RuntimeException("文档转换失败");
            }
            Path converted = workDir.resolve("source.pdf");
            if (!Files.exists(converted)) {
                throw new RuntimeException("文档转换未生成PDF");
            }
            // 先写临时文件再原子移动，避免并发读到半成品
            Path tmpTarget = Paths.get(cached.getAbsolutePath() + ".tmp");
            Files.move(converted, tmpTarget, StandardCopyOption.REPLACE_EXISTING);
            Files.move(tmpTarget, cached.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.info("Office文档转PDF完成: {} -> {}", sourcePath, cached.getAbsolutePath());
        } finally {
            // 清理临时工作目录和用户配置目录
            cleanupDirectory(workDir);
            cleanupDirectory(userInstallDir);
        }
    }

    private void cleanupDirectory(Path dir) {
        try (var stream = Files.walk(dir)) {
            stream.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (Exception ignore) {
                }
            });
        } catch (Exception ignore) {
        }
    }

    private String extOf(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "bin";
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
