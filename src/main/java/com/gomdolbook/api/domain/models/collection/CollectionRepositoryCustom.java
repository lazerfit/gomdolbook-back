package com.gomdolbook.api.domain.models.collection;

import com.gomdolbook.api.application.collection.dto.BookCoverDataInCollectionDTO;
import com.gomdolbook.api.application.collection.dto.BookInfoInCollectionDTO;
import java.util.List;

public interface CollectionRepositoryCustom {

    List<BookCoverDataInCollectionDTO> findCollections(String email);
    List<BookInfoInCollectionDTO> findCollection(String email, Long id);
}
