package com.crossmind.ai.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.crossmind.ai.model.AgentType;
import com.crossmind.ai.service.LLMClient;

@Component
@Order(30)
public class CustomerInsightAgent implements AnalysisAgent {

    private final LLMClient llmClient;

    public CustomerInsightAgent(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public AgentType type() {
        return AgentType.CUSTOMER_INSIGHT;
    }

    @Override
    public CustomerInsightResult execute(AgentContext context) {
        String reviewText = String.join(" ", context.mockData().reviews()).toLowerCase(Locale.ROOT);
        List<String> painPoints = new ArrayList<>();
        if (reviewText.contains("battery") || reviewText.contains("charging")) {
            painPoints.add("电池续航不足、充电时间较长");
        }
        if (reviewText.contains("clean")) {
            painPoints.add("刀头区域清洗困难");
        }
        if (reviewText.contains("powerful") || reviewText.contains("frozen")) {
            painPoints.add("处理冰块和冷冻水果的动力不足");
        }
        if (reviewText.contains("leak")) {
            painPoints.add("杯盖密封不严可能导致渗漏");
        }

        String summary = llmClient.chat("""
                [CUSTOMER_INSIGHT]
                商品：%s；市场：%s；评论：%s；已识别痛点：%s。
                请用一句中文总结用户洞察。
                """.formatted(context.product(), context.market(), context.mockData().reviews(), painPoints));

        return new CustomerInsightResult(
                List.copyOf(painPoints),
                List.of("便于旅行携带", "设计紧凑", "操作简单"),
                summary
        );
    }
}
