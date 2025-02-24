package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.api.dto.BookCollectionCoverDTO;
import com.gomdolbook.api.api.dto.BookListResponseDTO;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface BookUserCollectionRepositoryCustom {

    List<BookCollectionCoverDTO> getAllCollection(String email);
    List<BookListResponseDTO> getCollection(String name, String email);
}
