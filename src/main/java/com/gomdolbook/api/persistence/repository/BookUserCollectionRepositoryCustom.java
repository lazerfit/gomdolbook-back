package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.api.dto.BookUserCollectionCoverResponseDTO;
import org.springframework.stereotype.Repository;

@Repository
public interface BookUserCollectionRepositoryCustom {

    BookUserCollectionCoverResponseDTO getBookCovers(String collectionName, String email);
}
