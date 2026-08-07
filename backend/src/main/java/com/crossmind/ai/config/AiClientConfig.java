package com.crossmind.ai.config;

import java.net.http.HttpClient;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.crossmind.ai.service.BailianLLMClient;
import com.crossmind.ai.service.LLMClient;
import com.crossmind.ai.service.MockLLMClient;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiClientConfig {

    private static final Logger log = LoggerFactory.getLogger(AiClientConfig.class);

    @Bean
    public LLMClient llmClient(RestClient.Builder restClientBuilder, AiProperties properties) {
        String provider = properties.provider().toLowerCase(Locale.ROOT);
        if ("mock".equals(provider)) {
            log.info("AI provider is set to mock; using MockLLMClient");
            return new MockLLMClient();
        }
        if (!StringUtils.hasText(properties.apiKey())) {
            log.info("AI API Key is not configured; using MockLLMClient");
            return new MockLLMClient();
        }

        if (!"bailian".equals(provider)) {
            throw new IllegalArgumentException("Unsupported AI provider: " + properties.provider());
        }

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.readTimeout());
        RestClient restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .requestFactory(requestFactory)
                .build();
        log.info("Using BailianLLMClient with model {}", properties.model());
        return new BailianLLMClient(restClient, properties);
    }
}
