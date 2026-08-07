package com.crossmind.ai.common;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.crossmind.ai.model.AnalysisStatus;

public record AnalysisDetailResponse(
        UUID analysisId,
        String product,
        String market,
        AnalysisStatus status,
        Integer score,
        String trend,
        String competition,
        List<String> painPoints,
        List<String> productSuggestions,
        String pricingSuggestion,
        String marketingSuggestion,
        String strategy,
        List<AgentTaskResponse> agentTasks,
        Instant createdTime
) {
}
