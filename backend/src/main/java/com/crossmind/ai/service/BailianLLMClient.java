package com.crossmind.ai.service;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.crossmind.ai.config.AiProperties;

public class BailianLLMClient implements LLMClient {

    private static final String SYSTEM_PROMPT = """
            你是一名跨境电商选品分析专家。请严格依据用户提供的数据进行分析，
            使用简洁中文回答，不虚构外部数据，不使用 Markdown 标题。
            """;

    private final RestClient restClient;
    private final AiProperties properties;

    public BailianLLMClient(RestClient restClient, AiProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public String chat(String prompt) {
        ChatRequest request = new ChatRequest(
                properties.model(),
                List.of(
                        new Message("system", SYSTEM_PROMPT),
                        new Message("user", prompt)
                ),
                0.2
        );

        try {
            ChatResponse response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ChatResponse.class);
            return extractContent(response);
        } catch (RestClientException exception) {
            throw new LLMClientException("阿里云百炼调用失败", exception);
        }
    }

    @Override
    public LLMRuntimeInfo runtimeInfo() {
        return new LLMRuntimeInfo("bailian", properties.model(), true);
    }

    private String extractContent(ChatResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()
                || response.choices().getFirst().message() == null
                || response.choices().getFirst().message().content() == null
                || response.choices().getFirst().message().content().isBlank()) {
            throw new LLMClientException("阿里云百炼返回内容为空");
        }
        return response.choices().getFirst().message().content().trim();
    }

    record ChatRequest(String model, List<Message> messages, double temperature) {
    }

    record ChatResponse(List<Choice> choices) {
    }

    record Choice(Message message) {
    }

    record Message(String role, String content) {
    }
}
