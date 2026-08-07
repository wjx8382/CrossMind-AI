package com.crossmind.ai.agent;

public record MarketTrendResult(
        String trend,
        int score,
        double monthlySearchGrowth,
        double yearlyMarketGrowth,
        String summary
) {
}

