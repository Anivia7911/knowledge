package com.wch.rag.config;

import com.wch.rag.model.po.AiModelPO;
import com.wch.rag.service.AiModelService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.time.Duration;

/**
 * RAG 配置 - 动态从数据库加载启用的默认模型
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@DependsOn("sqlInitializer")
public class RagConfig {

    private final AiModelService aiModelService;

    /**
     * 默认 ChatModel，从数据库加载启用的默认模型
     * 如果数据库中没有配置，则返回 null
     */
    @Bean
    public ChatModel defaultChatModel() {
        AiModelPO model;
        try {
            model = aiModelService.lambdaQuery()
                    .eq(AiModelPO::getEnabled, true)
                    .eq(AiModelPO::getDefaultModel, true)
                    .eq(AiModelPO::getDeleted, 0)
                    .one();
        } catch (Exception e) {
            log.warn("加载默认AI模型配置失败: {}", e.getMessage());
            return null;
        }
        if (model == null) {
            log.warn("未找到默认启用的AI模型配置，请先在模型管理中添加并启用默认模型");
            return null;
        }
        return buildChatModel(model);
    }

    /**
     * 根据模型配置构建 ChatModel
     */
    public static ChatModel buildChatModel(AiModelPO model) {
        String baseUrl = model.getApiUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = model.getProviderType().getBaseUrl();
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new RuntimeException("自定义模型必须填写API地址，请在模型管理中编辑补充");
        }
        return OpenAiChatModel.builder()
                .apiKey(model.getApiKey())
                .modelName(model.getModelName())
                .baseUrl(baseUrl)
                .timeout(Duration.ofSeconds(60))
                .build();
    }
}
