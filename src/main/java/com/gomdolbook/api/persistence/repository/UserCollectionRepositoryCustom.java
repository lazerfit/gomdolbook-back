package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.api.dto.CollectionListResponseDTO;
import java.util.List;

public interface UserCollectionRepositoryCustom {

    List<CollectionListResponseDTO> findByEmail(String email);
}
