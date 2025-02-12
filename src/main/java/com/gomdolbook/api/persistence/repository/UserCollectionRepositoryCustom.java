package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.api.dto.CollectionListResponseDTO;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCollectionRepositoryCustom {

    List<CollectionListResponseDTO> findByEmail(String email);
}
