package com.gomdolbook.api.domain.models.bookcollection;

import com.gomdolbook.api.application.book.dto.BookCollectionCoverData;
import com.gomdolbook.api.application.book.dto.BookListData;
import java.util.List;
import java.util.Optional;

public interface BookCollectionRepositoryCustom {

    List<BookCollectionCoverData> getAllCollection(String email);
    List<BookListData> getCollection(String name, String email);
    Optional<BookCollection> find(String isbn, String name, String email);
}
