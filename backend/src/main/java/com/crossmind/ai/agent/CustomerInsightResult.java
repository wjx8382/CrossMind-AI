package com.crossmind.ai.agent;

import java.util.List;

public record CustomerInsightResult(
        List<String> painPoints,
        List<String> positiveSignals,
        String summary
) {
}

