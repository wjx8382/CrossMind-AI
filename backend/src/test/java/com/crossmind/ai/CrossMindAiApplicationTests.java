package com.crossmind.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.crossmind.ai.service.LLMClient;
import com.crossmind.ai.service.MockLLMClient;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest
class CrossMindAiApplicationTests {

    @Autowired
    private LLMClient llmClient;

    @Test
    void contextLoads() {
        assertInstanceOf(MockLLMClient.class, llmClient);
    }
}

