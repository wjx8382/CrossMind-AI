package com.crossmind.ai.agent;

import java.util.List;
import java.util.Locale;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.crossmind.ai.model.AgentType;
import com.crossmind.ai.service.LLMClient;

@Component
@Order(40)
public class StrategyAgent implements AnalysisAgent {

    private final LLMClient llmClient;

    public StrategyAgent(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public AgentType type() {
        return AgentType.STRATEGY;
    }

    @Override
    public AnalysisReport execute(AgentContext context) {
        MarketTrendResult trend = context.result(AgentType.MARKET_TREND, MarketTrendResult.class);
        CompetitorResult competitor = context.result(AgentType.COMPETITOR, CompetitorResult.class);
        CustomerInsightResult customer = context.result(AgentType.CUSTOMER_INSIGHT, CustomerInsightResult.class);

        int opportunityScore = Math.min(100, (int) Math.round(trend.score() * 0.7 + 26.5));
        double lowerPrice = Math.max(29.99, competitor.averagePrice() - 5);
        double upperPrice = competitor.averagePrice() + 3;
        String strategy = llmClient.chat("""
                [STRATEGY]
                商品：%s；市场：%s；趋势分析：%s；竞品分析：%s；用户洞察：%s。
                请给出一句明确的市场进入结论和产品定位，不超过 60 个汉字。
                """.formatted(context.product(), context.market(), trend, competitor, customer));

        return new AnalysisReport(
                opportunityScore,
                trend.trend(),
                competitor.competition(),
                customer.painPoints(),
                List.of("升级至 5000mAh 电池", "采用可拆卸刀头与自清洁模式", "增强冰块搅打与杯盖密封结构"),
                "建议首发价 $" + String.format(Locale.US, "%.2f", lowerPrice)
                        + "–$" + String.format(Locale.US, "%.2f", upperPrice),
                "围绕旅行、健身和办公室场景投放短视频，突出续航与易清洗差异点。",
                strategy
        );
    }
}
