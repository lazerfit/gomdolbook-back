package com.gomdolbook.api.application.readingLog.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeSummaryRequestDTO(@NotBlank String summary) {

}
