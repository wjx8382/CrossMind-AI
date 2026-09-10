package com.crossmind.ai.service;

public class MockLLMClient implements LLMClient {

    @Override
    public String chat(String prompt) {
        if (prompt.contains("[MARKET_TREND]")) {
            return "便携健康消费需求持续提升，搜索增长与市场扩张共同形成积极机会。";
        }
        if (prompt.contains("[COMPETITOR]")) {
            return "市场存在成熟品牌，但续航、清洗和搅打能力仍提供清晰的差异化空间。";
        }
        if (prompt.contains("[CUSTOMER_INSIGHT]")) {
            return "消费者认可便携性，同时集中关注电池、清洗、动力和密封体验。";
        }
        if (prompt.contains("[STRATEGY]")) {
            return "值得差异化进入：开发面向旅行与健身场景的长续航、易清洗便携榨汁杯。";
        }
        return "Mock AI 已完成分析。";
    }

    @Override
    public LLMRuntimeInfo runtimeInfo() {
        return new LLMRuntimeInfo("mock", "deterministic-demo", false);
    }
}
