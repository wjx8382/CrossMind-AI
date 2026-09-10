package com.crossmind.ai.service;

public interface LLMClient {

    String chat(String prompt);

    /** Returns non-sensitive runtime metadata for health checks and Demo display. */
    default LLMRuntimeInfo runtimeInfo() {
        return new LLMRuntimeInfo("unknown", "unknown", false);
    }
}
