package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.persistence.entity.UserCollection;

public interface UserCollectionRepositoryCustom {

    UserCollection findByNameAndEmail(String name, String email);
}
