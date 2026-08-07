package com.crossmind.ai.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crossmind.ai.agent.AgentOrchestrator;
import com.crossmind.ai.agent.AnalysisReport;
import com.crossmind.ai.common.AnalysisDetailResponse;
import com.crossmind.ai.common.AgentTaskResponse;
import com.crossmind.ai.common.CreateAnalysisRequest;
import com.crossmind.ai.common.CreateAnalysisResponse;
import com.crossmind.ai.common.ResourceNotFoundException;
import com.crossmind.ai.model.AgentTask;
import com.crossmind.ai.model.AgentType;
import com.crossmind.ai.model.ProductAnalysis;
import com.crossmind.ai.repository.AgentTaskRepository;
import com.crossmind.ai.repository.ProductAnalysisRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AnalysisService {

    private final ProductAnalysisRepository analysisRepository;
    private final AgentTaskRepository taskRepository;
    private final AgentOrchestrator orchestrator;
    private final ObjectMapper objectMapper;

    public AnalysisService(
            ProductAnalysisRepository analysisRepository,
            AgentTaskRepository taskRepository,
            AgentOrchestrator orchestrator,
            ObjectMapper objectMapper) {
        this.analysisRepository = analysisRepository;
        this.taskRepository = taskRepository;
        this.orchestrator = orchestrator;
        this.objectMapper = objectMapper;
    }

    public CreateAnalysisResponse createAnalysis(CreateAnalysisRequest request) {
        ProductAnalysis analysis = new ProductAnalysis(request.product().trim(), request.market().trim());
        ProductAnalysis saved = analysisRepository.saveAndFlush(analysis);
        List<AgentTask> tasks = List.of(
                new AgentTask(saved, AgentType.MARKET_TREND),
                new AgentTask(saved, AgentType.COMPETITOR),
                new AgentTask(saved, AgentType.CUSTOMER_INSIGHT),
                new AgentTask(saved, AgentType.STRATEGY)
        );
        taskRepository.saveAllAndFlush(tasks);
        orchestrator.executeAsync(saved.getId());
        return new CreateAnalysisResponse(saved.getId(), saved.getStatus());
    }

    @Transactional(readOnly = true)
    public AnalysisDetailResponse getAnalysis(UUID analysisId) {
        ProductAnalysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("分析任务不存在: " + analysisId));

        AnalysisReport report = readReport(analysis);
        List<AgentTaskResponse> agentTasks = taskRepository.findByAnalysis_IdOrderByCreatedTimeAsc(analysisId)
                .stream()
                .map(task -> new AgentTaskResponse(task.getAgentType(), task.getStatus()))
                .toList();

        return new AnalysisDetailResponse(
                analysis.getId(),
                analysis.getProductName(),
                analysis.getMarket(),
                analysis.getStatus(),
                analysis.getScore(),
                report == null ? null : report.trend(),
                report == null ? null : report.competition(),
                report == null ? List.of() : report.painPoints(),
                report == null ? List.of() : report.productSuggestions(),
                report == null ? null : report.pricingSuggestion(),
                report == null ? null : report.marketingSuggestion(),
                report == null ? null : report.strategy(),
                agentTasks,
                analysis.getCreatedTime()
        );
    }

    private AnalysisReport readReport(ProductAnalysis analysis) {
        if (analysis.getStatus() != com.crossmind.ai.model.AnalysisStatus.COMPLETED
                || analysis.getResultJson() == null) {
            return null;
        }
        try {
            return objectMapper.readValue(analysis.getResultJson(), AnalysisReport.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("分析报告 JSON 无法解析", exception);
        }
    }
}
