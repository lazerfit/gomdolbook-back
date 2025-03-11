package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.api.dto.BookCollectionCoverDTO;
import com.gomdolbook.api.api.dto.BookListResponseDTO;
import com.gomdolbook.api.persistence.entity.BookUserCollection;
import java.util.List;
import java.util.Optional;

public interface BookUserCollectionRepositoryCustom {

    List<BookCollectionCoverDTO> getAllCollection(String email);
    List<BookListResponseDTO> getCollection(String name, String email);
    Optional<BookUserCollection> findByIsbnAndName(String isbn, String name, String email);
}
