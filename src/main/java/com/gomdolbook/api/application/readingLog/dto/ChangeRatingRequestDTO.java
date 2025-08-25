package com.gomdolbook.api.application.readingLog.dto;

import jakarta.validation.constraints.NotNull;

public record ChangeRatingRequestDTO(
    @NotNull int star
) {

}
