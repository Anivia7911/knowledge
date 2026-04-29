package com.wch.file.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Jie Bugui
 * @create: 2026-04-29 15:02
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileConfig {
}
