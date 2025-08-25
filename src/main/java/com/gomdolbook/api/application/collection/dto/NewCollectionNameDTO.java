package com.gomdolbook.api.application.collection.dto;

import jakarta.validation.constraints.NotBlank;

public record NewCollectionNameDTO(@NotBlank String name) {

}
