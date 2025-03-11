package com.gomdolbook.api.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReadingLogUpdateRequestDTO(
    @NotNull String isbn,
    @NotNull String note,
    @NotBlank String value
) {

}
