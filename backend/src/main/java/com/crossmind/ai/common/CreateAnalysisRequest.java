package com.crossmind.ai.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAnalysisRequest(
        @NotBlank @Size(max = 200) String product,
        @NotBlank @Size(max = 100) String market
) {
}

