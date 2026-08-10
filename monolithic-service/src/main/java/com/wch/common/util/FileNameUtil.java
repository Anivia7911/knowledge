package com.wch.common.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件名工具：生成符合 RFC 5987 的 Content-Disposition 头，
 * 支持中文等非 ASCII 文件名（HTTP 头仅允许 ISO-8859-1 字符，直接写中文会被容器丢弃）
 */
public class FileNameUtil {

    private FileNameUtil() {
    }

    /**
     * @param disposition attachment 或 inline
     * @param fileName    原始文件名（可含中文）
     */
    public static String contentDisposition(String disposition, String fileName) {
        String safe = fileName == null || fileName.isBlank() ? "file" : fileName;
        // ASCII 回退名（供不支持 RFC 5987 的老浏览器使用）
        String asciiFallback = safe.replaceAll("[^\\x20-\\x7E]", "_").replace("\"", "");
        if (asciiFallback.isBlank()) {
            asciiFallback = "file";
        }
        String encoded = URLEncoder.encode(safe, StandardCharsets.UTF_8).replace("+", "%20");
        return disposition + "; filename=\"" + asciiFallback + "\"; filename*=UTF-8''" + encoded;
    }
}
