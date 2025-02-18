package com.gomdolbook.api.api.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BookCollectionCoverListResponseDTO {

    private final String name;
    private final BookCoverDTO books;
}
