package com.wch.file.conf;

import lombok.Data;
import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.client.factory.AuthenticationMethod;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Jie Bugui
 * @create: 2025-04-22 16:09
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ceph")
public class ContainerConfig {

    private String username;
    private String password;
    private String authUrl;
    private String defaultContainerName;

    /**
     * 创建账号信息
     */
    public Account account() {
        AccountConfig config = new AccountConfig();
        config.setUsername(username);
        config.setPassword(password);
        config.setAuthUrl(authUrl);
        config.setAuthenticationMethod(AuthenticationMethod.BASIC);
        return new AccountFactory(config).createAccount();
    }

    /**
     * 创建日期对象
     */
    public Container container() {
        Container container = account().getContainer(defaultContainerName);
        if (!container.exists()) {
            return container.create();
        }
        return container;
    }
}
