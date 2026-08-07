package com.crossmind.ai.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MockLLMClientTests {

    private final MockLLMClient client = new MockLLMClient();

    @Test
    void returnsAgentSpecificDemoContent() {
        assertTrue(client.chat("[MARKET_TREND]").contains("市场"));
        assertTrue(client.chat("[COMPETITOR]").contains("差异化"));
        assertTrue(client.chat("[CUSTOMER_INSIGHT]").contains("消费者"));
        assertTrue(client.chat("[STRATEGY]").contains("进入"));
    }
}

