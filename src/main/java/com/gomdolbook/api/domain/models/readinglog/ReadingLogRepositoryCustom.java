package com.gomdolbook.api.domain.models.readinglog;

import com.gomdolbook.api.application.readingLog.dto.ReadingLogWithBookDTO;
import java.util.Optional;

public interface ReadingLogRepositoryCustom {

    Optional<ReadingLogWithBookDTO> findWithBookByIsbnAndEmail(String isbn, String email);
    Optional<ReadingLogWithBookDTO> findWithBookByIdAndEmail(Long id, String email);
}
