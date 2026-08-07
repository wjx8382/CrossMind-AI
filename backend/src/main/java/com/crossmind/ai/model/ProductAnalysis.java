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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_analysis")
public class ProductAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(nullable = false, length = 100)
    private String market;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnalysisStatus status;

    private Integer score;

    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;

    @Column(name = "created_time", nullable = false, updatable = false)
    private Instant createdTime;

    protected ProductAnalysis() {
    }

    public ProductAnalysis(String productName, String market) {
        this.productName = productName;
        this.market = market;
        this.status = AnalysisStatus.RUNNING;
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

    public String getProductName() {
        return productName;
    }

    public String getMarket() {
        return market;
    }

    public AnalysisStatus getStatus() {
        return status;
    }

    public Integer getScore() {
        return score;
    }

    public String getResultJson() {
        return resultJson;
    }

    public Instant getCreatedTime() {
        return createdTime;
    }

    public void complete(int score, String resultJson) {
        this.score = score;
        this.resultJson = resultJson;
        this.status = AnalysisStatus.COMPLETED;
    }

    public void fail(String errorJson) {
        this.resultJson = errorJson;
        this.status = AnalysisStatus.FAILED;
    }
}
