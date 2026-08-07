package com.crossmind.ai.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai")
public record AiProperties(
        String provider,
        String apiKey,
        String model,
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {
    public AiProperties {
        provider = provider == null ? "bailian" : provider;
        model = model == null ? "qwen-plus" : model;
        baseUrl = baseUrl == null
                ? "https://dashscope.aliyuncs.com/compatible-mode/v1"
                : baseUrl;
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(10) : connectTimeout;
        readTimeout = readTimeout == null ? Duration.ofSeconds(60) : readTimeout;
    }
}
