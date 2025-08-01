package com.gomdolbook.api.application.collection;

import com.gomdolbook.api.common.config.annotations.PreAuthorizeWithContainsUser;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.collection.CollectionRepository;
import com.gomdolbook.api.domain.services.SecurityService;
import com.gomdolbook.api.domain.shared.CollectionNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@PreAuthorizeWithContainsUser
@RequiredArgsConstructor
@Service
public class CollectionApplicationService {

    private final CollectionRepository collectionRepository;
    private final SecurityService securityService;

    @Transactional
    public void changeCollectionName(String oldName, String newName) {
        String email = securityService.getUserEmailFromSecurityContext();
        Collection collection = collectionRepository.find(oldName, email)
            .orElseThrow(() -> new CollectionNotFoundException("해당 컬렉션이 존재하지않습니다."));
        collection.changeName(newName);
    }
}
