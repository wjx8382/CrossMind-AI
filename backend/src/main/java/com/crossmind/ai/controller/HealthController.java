package com.crossmind.ai.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crossmind.ai.service.LLMClient;
import com.crossmind.ai.service.LLMRuntimeInfo;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final LLMClient llmClient;

    public HealthController(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        LLMRuntimeInfo runtime = llmClient.runtimeInfo();
        return Map.of(
                "status", "UP",
                "service", "crossmind-ai-backend",
                "ai", Map.of(
                        "provider", runtime.provider(),
                        "model", runtime.model(),
                        "live", runtime.live()
                )
        );
    }
}
