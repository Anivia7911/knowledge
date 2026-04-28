package com.wch.preview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author: Jie Bugui
 * @create: 2025-04-22 16:28
 */
@MapperScan()
@SpringBootApplication()
public class PreviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(PreviewApplication.class, args);
    }
}
