package com.crossmind.ai.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.crossmind.ai.common.AnalysisDetailResponse;
import com.crossmind.ai.common.CreateAnalysisRequest;
import com.crossmind.ai.common.CreateAnalysisResponse;
import com.crossmind.ai.service.AnalysisService;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CreateAnalysisResponse create(@Valid @RequestBody CreateAnalysisRequest request) {
        return analysisService.createAnalysis(request);
    }

    @GetMapping("/{id}")
    public AnalysisDetailResponse get(@PathVariable UUID id) {
        return analysisService.getAnalysis(id);
    }
}

