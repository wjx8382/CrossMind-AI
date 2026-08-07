package com.crossmind.ai.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crossmind.ai.model.ProductAnalysis;

public interface ProductAnalysisRepository extends JpaRepository<ProductAnalysis, UUID> {
}

