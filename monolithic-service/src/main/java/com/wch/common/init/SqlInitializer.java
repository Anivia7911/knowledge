package com.wch.common.init;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * 应用启动时自动根据当前数据库类型执行对应的建表SQL脚本
 * 脚本位置: classpath:sqlupgrade/builtin/{MySQL|PostgreSQL|Dameng}.sql
 */
@Slf4j
@Configuration
public class SqlInitializer {

    private final DataSource dataSource;

    public SqlInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Bean 初始化时立即执行建表，确保早于任何业务 Bean 的数据库访问
     */
    @PostConstruct
    public void init() {
        String dbType = detectDbType();
        String sqlFileName = mapDbTypeToFileName(dbType);
        String classpathLocation = "sqlupgrade/builtin/" + sqlFileName;

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource resource = resolver.getResource("classpath:" + classpathLocation);
            if (!resource.exists()) {
                log.warn("未找到SQL脚本文件: {}, 跳过初始化", classpathLocation);
                return;
            }

            String sql = readSql(resource);
            if (sql.isBlank()) {
                log.warn("SQL脚本文件为空: {}, 跳过初始化", classpathLocation);
                return;
            }

            executeSql(sql);
            log.info("SQL脚本执行完成: {}", classpathLocation);
        } catch (Exception e) {
            log.error("执行SQL脚本失败: {}, 原因: {}", classpathLocation, e.getMessage());
        }
    }

    /**
     * 根据JDBC URL检测数据库类型
     */
    private String detectDbType() {
        try (Connection conn = dataSource.getConnection()) {
            String url = conn.getMetaData().getURL().toLowerCase();
            if (url.contains("mysql")) {
                return "MySQL";
            } else if (url.contains("postgresql")) {
                return "PostgreSQL";
            } else if (url.contains("dm")) {
                return "Dameng";
            } else {
                log.warn("无法识别数据库类型: {}, 默认使用MySQL", url);
                return "MySQL";
            }
        } catch (Exception e) {
            log.error("检测数据库类型失败: {}", e.getMessage());
            return "MySQL";
        }
    }

    /**
     * 数据库类型 -> SQL文件名
     */
    private String mapDbTypeToFileName(String dbType) {
        return dbType + ".sql";
    }

    /**
     * 读取SQL文件内容
     */
    private String readSql(Resource resource) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * 按分号分割并逐条执行SQL语句，忽略已存在的表等错误
     */
    private void executeSql(String sql) {
        // 移除注释
        sql = removeComments(sql);

        String[] statements = sql.split(";");
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String s : statements) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        stmt.execute(trimmed);
                    } catch (Exception e) {
                        // 忽略表已存在等错误，继续执行后续语句
                        log.debug("SQL执行提示(可忽略): {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("数据库连接失败: {}", e.getMessage());
        }
    }

    /**
     * 移除SQL注释（单行--注释和/**\/块注释）
     */
    private String removeComments(String sql) {
        // 移除单行注释 --
        sql = sql.replaceAll("--[^\\n]*", "");
        // 移除块注释 /* */（使用非贪婪匹配）
        sql = sql.replaceAll("/\\*.*?\\*/", "");
        return sql;
    }
}
