package com.wch.file;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: Jie Bugui
 * @create: 2025-04-22 16:28
 */
@EnableFeignClients
@MapperScan("com.wch.file.mapper")
@SpringBootApplication()
public class FileApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }
}
