package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.api.dto.book.BookCollectionCoverDTO;
import com.gomdolbook.api.api.dto.book.BookListResponseDTO;
import com.gomdolbook.api.persistence.entity.BookUserCollection;
import java.util.List;
import java.util.Optional;

public interface BookUserCollectionRepositoryCustom {

    List<BookCollectionCoverDTO> getAllCollection(String email);
    List<BookListResponseDTO> getCollection(String name, String email);
    Optional<BookUserCollection> findByIsbnAndName(String isbn, String name, String email);
}
