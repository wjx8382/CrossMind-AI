package com.crossmind.ai.agent;

import java.util.List;

public record AnalysisReport(
        int score,
        String trend,
        String competition,
        List<String> painPoints,
        List<String> productSuggestions,
        String pricingSuggestion,
        String marketingSuggestion,
        String strategy
) {
}

