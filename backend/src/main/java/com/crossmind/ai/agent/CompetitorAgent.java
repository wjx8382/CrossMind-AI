package com.crossmind.ai.agent;

import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.crossmind.ai.model.AgentType;
import com.crossmind.ai.service.LLMClient;

@Component
@Order(20)
public class CompetitorAgent implements AnalysisAgent {

    private final LLMClient llmClient;

    public CompetitorAgent(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public AgentType type() {
        return AgentType.COMPETITOR;
    }

    @Override
    public CompetitorResult execute(AgentContext context) {
        List<MockProductData.Competitor> competitors = context.mockData().competitors();
        double averagePrice = competitors.stream()
                .mapToDouble(MockProductData.Competitor::price)
                .average()
                .orElse(0);
        String competition = competitors.size() >= 8 ? "高" : competitors.size() >= 4 ? "中等" : "低";
        String summary = llmClient.chat("""
                [COMPETITOR]
                商品：%s；市场：%s；竞品数量：%d；平均价格：$%.2f；竞争程度：%s；竞品：%s。
                请用一句中文总结差异化机会。
                """.formatted(context.product(), context.market(), competitors.size(), averagePrice,
                competition, competitors));
        return new CompetitorResult(
                competition,
                Math.round(averagePrice * 100.0) / 100.0,
                competitors.size(),
                List.of("更长续航", "可拆卸刀头", "强化冰块搅打能力"),
                summary
        );
    }
}
