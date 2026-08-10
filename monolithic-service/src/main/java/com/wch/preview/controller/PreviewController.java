package com.wch.preview.controller;

import com.wch.common.model.resp.RespResult;
import com.wch.common.util.FileNameUtil;
import com.wch.file.model.po.FileBodyPO;
import com.wch.file.model.po.FileHeaderPO;
import com.wch.file.service.FileBodyService;
import com.wch.file.service.FileService;
import com.wch.preview.service.PreviewConvertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/preview")
@RequiredArgsConstructor
public class PreviewController {

    private final FileService fileService;
    private final FileBodyService fileBodyService;
    private final PreviewConvertService previewConvertService;

    /**
     * 获取文件预览信息
     * 返回文件元信息和文本内容（如果是文本文件）
     */
    @GetMapping("/info")
    public RespResult<Map<String, Object>> previewInfo(@RequestParam("id") String id) {
        FileHeaderPO header = fileService.getFileInfo(id);
        if (header == null) {
            return RespResult.error();
        }

        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("id", header.getId());
        fileInfo.put("name", header.getName());
        fileInfo.put("bodyId", header.getBodyId());

        FileBodyPO body = fileBodyService.getById(header.getBodyId());
        if (body != null) {
            fileInfo.put("size", body.getSize());
            fileInfo.put("type", body.getType());
            fileInfo.put("path", body.getPath());
        }

        Map<String, Object> result = new HashMap<>(fileInfo);

        String fileType = (String) fileInfo.get("type");
        String path = (String) fileInfo.get("path");

        // 预览类型判定（参照 kkfileview 分类：文本/图片/PDF/Office）
        if (isTextFile(fileType)) {
            result.put("previewType", "text");
            try {
                String content = readFileContent(path);
                // 限制预览内容长度
                if (content.length() > 10000) {
                    content = content.substring(0, 10000) + "\n... (内容过长，仅展示前10000字符)";
                }
                result.put("content", content);
                result.put("previewable", true);
            } catch (Exception e) {
                log.error("读取文件内容失败: {}", e.getMessage());
                result.put("previewable", false);
                result.put("previewError", "文件内容读取失败");
            }
        } else if (previewConvertService.isImage(fileType)) {
            result.put("previewType", "image");
            result.put("previewable", true);
        } else if (previewConvertService.isPdf(fileType)) {
            result.put("previewType", "pdf");
            result.put("previewable", true);
        } else if (previewConvertService.isOffice(fileType)) {
            result.put("previewType", "office");
            result.put("previewable", true);
        } else {
            result.put("previewType", "unsupported");
            result.put("previewable", false);
            result.put("previewError", "该文件类型暂不支持预览");
        }

        return RespResult.success(result);
    }

    /**
     * Office 文档在线预览（LibreOffice 转 PDF，参照 kkfileview 实现思路）
     */
    @GetMapping("/office")
    public ResponseEntity<Resource> officeView(@RequestParam("id") String id) {
        FileHeaderPO header = fileService.getFileInfo(id);
        if (header == null) {
            return ResponseEntity.notFound().build();
        }
        FileBodyPO body = fileBodyService.getById(header.getBodyId());
        if (body == null || body.getPath() == null || body.getPath().isBlank()) {
            return ResponseEntity.notFound().build();
        }
        try {
            String cacheKey = body.getMd5() != null ? body.getMd5() : String.valueOf(body.getId());
            File pdf = previewConvertService.convertToPdf(body.getPath(), cacheKey);
            Resource resource = new FileSystemResource(pdf);
            String pdfName = (header.getName() == null ? "preview" : header.getName().replaceAll("\\.[^.]+$", "")) + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, FileNameUtil.contentDisposition("inline", pdfName))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (Exception e) {
            log.error("Office文档预览失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 文件在线预览（直接返回文件流，适用于图片等）
     */
    @GetMapping("/view")
    public ResponseEntity<Resource> viewFile(@RequestParam("id") String id) {
        FileHeaderPO header = fileService.getFileInfo(id);
        if (header == null) {
            return ResponseEntity.notFound().build();
        }

        FileBodyPO body = fileBodyService.getById(header.getBodyId());
        if (body == null) {
            return ResponseEntity.notFound().build();
        }

        String fileType = body.getType();
        String fileName = header.getName();

        MediaType mediaType = getMediaType(fileType);
        try {
            Resource resource = fileService.download(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, FileNameUtil.contentDisposition("inline", fileName))
                    .contentType(mediaType)
                    .body(resource);
        } catch (Exception e) {
            log.error("文件预览失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private boolean isTextFile(String fileType) {
        if (fileType == null) return false;
        String lower = fileType.toLowerCase();
        return lower.equals("txt") || lower.equals("md") || lower.equals("json")
                || lower.equals("xml") || lower.equals("csv") || lower.equals("log")
                || lower.equals("java") || lower.equals("py") || lower.equals("js")
                || lower.equals("html") || lower.equals("css") || lower.equals("sql")
                || lower.equals("yaml") || lower.equals("yml") || lower.equals("properties")
                || lower.equals("sh") || lower.equals("bat") || lower.equals("ini")
                || lower.equals("conf");
    }

    private String readFileContent(String path) throws Exception {
        if (path == null || path.isBlank()) {
            throw new RuntimeException("文件路径为空");
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new java.io.FileInputStream(path), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private MediaType getMediaType(String fileType) {
        if (fileType == null) return MediaType.APPLICATION_OCTET_STREAM;
        return switch (fileType.toLowerCase()) {
            case "png" -> MediaType.IMAGE_PNG;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "svg" -> MediaType.parseMediaType("image/svg+xml");
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "txt", "md", "json", "xml", "csv", "log", "java", "py", "js", "html", "css", "sql"
                    -> MediaType.TEXT_PLAIN;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
