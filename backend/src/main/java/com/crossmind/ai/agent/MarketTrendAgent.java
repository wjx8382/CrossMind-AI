package com.crossmind.ai.agent;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.crossmind.ai.model.AgentType;
import com.crossmind.ai.service.LLMClient;

@Component
@Order(10)
public class MarketTrendAgent implements AnalysisAgent {

    private final LLMClient llmClient;

    public MarketTrendAgent(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public AgentType type() {
        return AgentType.MARKET_TREND;
    }

    @Override
    public MarketTrendResult execute(AgentContext context) {
        MockProductData data = context.mockData();
        double growthSignal = data.monthlySearchGrowth() * 0.55 + data.yearlyMarketGrowth() * 0.45;
        int score = Math.min(95, (int) Math.round(65 + growthSignal));
        String trend = growthSignal >= 15 ? "增长" : growthSignal >= 5 ? "稳定" : "下降";
        String summary = llmClient.chat("""
                [MARKET_TREND]
                商品：%s；市场：%s；月搜索增长：%.1f%%；年市场增长：%.1f%%；趋势：%s。
                请用一句中文总结市场机会。
                """.formatted(context.product(), context.market(), data.monthlySearchGrowth(),
                data.yearlyMarketGrowth(), trend));
        return new MarketTrendResult(
                trend,
                score,
                data.monthlySearchGrowth(),
                data.yearlyMarketGrowth(),
                summary
        );
    }
}
