package com.wch.common.mybatisplus;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Jie Bugui
 * @create: 2026-05-01 1:37
 */
@Configuration
@ConfigurationProperties("mybatis-plus")
@Data
public class MybatisPlusConfig {

    @Value("${page.db-type}")
    private DbType dbType = DbType.POSTGRE_SQL;

    @Value("${page.max-limit}")
    private Long maxLimit = 1000L;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        paginationInnerInterceptor.setDbType(dbType);
        paginationInnerInterceptor.setMaxLimit(maxLimit);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }
}
