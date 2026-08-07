package com.crossmind.ai.agent;

import java.util.List;

public record MockProductData(
        String product,
        String market,
        double monthlySearchGrowth,
        double yearlyMarketGrowth,
        List<Competitor> competitors,
        List<String> reviews
) {
    public record Competitor(String brand, double price, double rating) {
    }
}

