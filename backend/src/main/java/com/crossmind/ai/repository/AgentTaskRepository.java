package com.crossmind.ai.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crossmind.ai.model.AgentTask;
import com.crossmind.ai.model.AgentType;

public interface AgentTaskRepository extends JpaRepository<AgentTask, UUID> {

    List<AgentTask> findByAnalysis_IdOrderByCreatedTimeAsc(UUID analysisId);

    java.util.Optional<AgentTask> findByAnalysis_IdAndAgentType(UUID analysisId, AgentType agentType);
}
