package com.gomdolbook.api.domain.models.bookmetacollection;

import com.gomdolbook.api.application.book.dto.BookCollectionCoverData;
import com.gomdolbook.api.application.bookmetacollection.dto.CollectionBookMetaData;
import java.util.List;

public interface BookMetaCollectionRepositoryCustom {

    List<BookCollectionCoverData> getAllCollection(String email);
    List<CollectionBookMetaData> getCollectionData(String email, String collectionName);
    boolean existsBookInCollection(String email, String collectionName, String isbn);
}
