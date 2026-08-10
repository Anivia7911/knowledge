package com.wch.preview.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件预览配置（参照 kkfileview 思路：Office 文档通过 LibreOffice 转 PDF 后预览）
 */
@Component
@ConfigurationProperties("preview")
@Data
public class PreviewSetting {

    /**
     * LibreOffice soffice 可执行文件路径
     */
    private String sofficePath = "/Applications/LibreOffice.app/Contents/MacOS/soffice";

    /**
     * 转换结果缓存目录
     */
    private String cachePath = "/tmp/knowledge/preview-cache";

    /**
     * 转换超时时间（秒）
     */
    private Integer convertTimeoutSeconds = 120;
}
