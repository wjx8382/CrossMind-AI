package com.crossmind.ai.agent;

import java.util.EnumMap;
import java.util.Map;

import com.crossmind.ai.model.AgentType;

public class AgentContext {

    private final String product;
    private final String market;
    private final MockProductData mockData;
    private final Map<AgentType, Object> results = new EnumMap<>(AgentType.class);

    public AgentContext(String product, String market, MockProductData mockData) {
        this.product = product;
        this.market = market;
        this.mockData = mockData;
    }

    public String product() {
        return product;
    }

    public String market() {
        return market;
    }

    public MockProductData mockData() {
        return mockData;
    }

    public void putResult(AgentType type, Object result) {
        results.put(type, result);
    }

    public <T> T result(AgentType type, Class<T> resultType) {
        Object result = results.get(type);
        if (!resultType.isInstance(result)) {
            throw new IllegalStateException("Agent result is unavailable: " + type);
        }
        return resultType.cast(result);
    }
}

