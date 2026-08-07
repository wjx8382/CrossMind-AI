package com.crossmind.ai.common;

import java.util.UUID;

import com.crossmind.ai.model.AnalysisStatus;

public record CreateAnalysisResponse(UUID analysisId, AnalysisStatus status) {
}

