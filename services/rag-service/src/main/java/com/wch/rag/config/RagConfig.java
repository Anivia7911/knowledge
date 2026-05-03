package com.wch.rag.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Jie Bugui
 * @create: 2026-05-02 21:51
 */
@Configuration
public class RagConfig {

    @Bean
    public ChatModel LLMChat() {
        return OpenAiChatModel.builder()
                .apiKey("")
                .modelName("")
                .baseUrl("")
                .build();
    }

}
