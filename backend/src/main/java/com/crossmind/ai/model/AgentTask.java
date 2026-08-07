package com.crossmind.ai.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "agent_task", indexes = @Index(name = "idx_agent_task_analysis_id", columnList = "analysis_id"))
public class AgentTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private ProductAnalysis analysis;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_type", nullable = false, length = 30)
    private AgentType agentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AgentTaskStatus status;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(name = "created_time", nullable = false, updatable = false)
    private Instant createdTime;

    protected AgentTask() {
    }

    public AgentTask(ProductAnalysis analysis, AgentType agentType) {
        this.analysis = analysis;
        this.agentType = agentType;
        this.status = AgentTaskStatus.PENDING;
    }

    @PrePersist
    void initializeCreatedTime() {
        if (createdTime == null) {
            createdTime = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getAnalysisId() {
        return analysis.getId();
    }

    public AgentType getAgentType() {
        return agentType;
    }

    public AgentTaskStatus getStatus() {
        return status;
    }

    public String getResult() {
        return result;
    }

    public Instant getCreatedTime() {
        return createdTime;
    }

    public void markRunning() {
        this.status = AgentTaskStatus.RUNNING;
    }

    public void markCompleted(String result) {
        this.result = result;
        this.status = AgentTaskStatus.COMPLETED;
    }

    public void markFailed(String error) {
        this.result = error;
        this.status = AgentTaskStatus.FAILED;
    }
}
