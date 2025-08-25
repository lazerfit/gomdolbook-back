package com.gomdolbook.api.domain.models.bookmetacollection;

import com.gomdolbook.api.application.book.dto.BookCollectionCoverData;
import com.gomdolbook.api.application.collection.dto.BookInfoInCollectionDTO;
import java.util.List;

public interface BookMetaCollectionRepositoryCustom {

    List<BookCollectionCoverData> getAllCollection(String email);
    List<BookInfoInCollectionDTO> getCollectionData(String email, Long id);
    boolean existsBookInCollection(String email, String collectionName, String isbn);
}
