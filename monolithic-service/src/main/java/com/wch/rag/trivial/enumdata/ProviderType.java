package com.wch.rag.trivial.enumdata;

import lombok.Getter;

/**
 * @author: Jie Bugui
 * @create: 2026-05-01 0:51
 */
@Getter
public enum ProviderType {
    DEEP_SEEK("https://api.deepseek.com/chat/completions", "DeepSeek"),
    QIAN_WEN("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions", "通义千问"),
    OLLAMA("http://localhost:11434/v1/chat/completions", "Ollama"),
    DOUBAO("https://ark.cn-beijing.volces.com/api/v3/chat/completions", "豆包"),
    KIMI("https://api.moonshot.cn/v1/chat/completions", "Kimi"),
    BAICHUAN("https://api.baichuan-ai.com/v1/chat/completions", "百川智能"),
    ZHIPU("https://open.bigmodel.cn/api/paas/v4/chat/completions", "智谱AI"),
    MINMAX("https://api.minimaxi.com/v1/chat/completions", "MiniMax"),
    OPENAI("https://api.openai.com/v1/chat/completions", "OpenAI"),
    CUSTOM(null, "自定义"),

    ;

    private final String baseUrl;

    private final String name;

    ProviderType(String baseUrl, String name) {
        this.baseUrl = baseUrl;
        this.name = name;
    }
}
