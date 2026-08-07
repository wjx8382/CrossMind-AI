package com.crossmind.ai.agent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crossmind.ai.model.AgentTask;
import com.crossmind.ai.model.AgentType;
import com.crossmind.ai.model.ProductAnalysis;
import com.crossmind.ai.repository.AgentTaskRepository;
import com.crossmind.ai.repository.ProductAnalysisRepository;
import com.crossmind.ai.service.MockProductDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AgentOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AgentOrchestrator.class);

    private final List<AnalysisAgent> agents;
    private final ProductAnalysisRepository analysisRepository;
    private final AgentTaskRepository taskRepository;
    private final MockProductDataService mockProductDataService;
    private final ObjectMapper objectMapper;
    private final long executionDelayMs;

    public AgentOrchestrator(
            List<AnalysisAgent> agents,
            ProductAnalysisRepository analysisRepository,
            AgentTaskRepository taskRepository,
            MockProductDataService mockProductDataService,
            ObjectMapper objectMapper,
            @Value("${agent.execution-delay-ms:650}") long executionDelayMs) {
        this.agents = agents;
        this.analysisRepository = analysisRepository;
        this.taskRepository = taskRepository;
        this.mockProductDataService = mockProductDataService;
        this.objectMapper = objectMapper;
        this.executionDelayMs = executionDelayMs;
    }

    @Async("agentTaskExecutor")
    public void executeAsync(UUID analysisId) {
        ProductAnalysis analysis = analysisRepository.findById(analysisId).orElseThrow();
        Map<AgentType, AgentTask> tasks = taskRepository.findByAnalysis_IdOrderByCreatedTimeAsc(analysisId)
                .stream()
                .collect(Collectors.toMap(AgentTask::getAgentType, Function.identity()));

        AgentTask activeTask = null;
        try {
            MockProductData mockData = mockProductDataService.find(
                    analysis.getProductName(), analysis.getMarket());
            AgentContext context = new AgentContext(
                    analysis.getProductName(), analysis.getMarket(), mockData);

            for (AnalysisAgent agent : agents) {
                activeTask = tasks.get(agent.type());
                activeTask.markRunning();
                taskRepository.save(activeTask);
                pauseForDemo();

                Object result = agent.execute(context);
                context.putResult(agent.type(), result);
                activeTask.markCompleted(toJson(result));
                taskRepository.save(activeTask);
            }

            AnalysisReport report = context.result(AgentType.STRATEGY, AnalysisReport.class);
            analysis.complete(report.score(), toJson(report));
            analysisRepository.save(analysis);
        } catch (Exception exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            if (activeTask != null) {
                activeTask.markFailed(errorJson(exception));
                taskRepository.save(activeTask);
            }
            analysis.fail(errorJson(exception));
            analysisRepository.save(analysis);
            log.error("Agent workflow failed for analysis {}", analysisId, exception);
        }
    }

    private void pauseForDemo() throws InterruptedException {
        if (executionDelayMs > 0) {
            Thread.sleep(executionDelayMs);
        }
    }

    private String toJson(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }

    private String errorJson(Exception exception) {
        try {
            return objectMapper.writeValueAsString(Map.of("error", String.valueOf(exception.getMessage())));
        } catch (JsonProcessingException serializationException) {
            return "{\"error\":\"Agent execution failed\"}";
        }
    }
}
