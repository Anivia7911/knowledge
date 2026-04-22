package com.wch.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author: Jie Bugui
 * @create: 2025-04-22 16:28
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class FileApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }
}
