package com.crossmind.ai.agent;

import java.util.List;

public record CompetitorResult(
        String competition,
        double averagePrice,
        int competitorCount,
        List<String> differentiators,
        String summary
) {
}

