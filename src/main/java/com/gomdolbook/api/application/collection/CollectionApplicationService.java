package com.gomdolbook.api.application.collection;

import com.gomdolbook.api.application.collection.dto.BookCoverDataInCollectionDTO;
import com.gomdolbook.api.application.collection.dto.BookInfoInCollectionDTO;
import com.gomdolbook.api.application.collection.dto.CollectionDetailDTO;
import com.gomdolbook.api.application.collection.dto.CollectionsDTO;
import com.gomdolbook.api.common.config.annotations.PreAuthorizeWithContainsUser;
import com.gomdolbook.api.common.config.annotations.UserCheckAndSave;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.collection.CollectionRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.UserRepository;
import com.gomdolbook.api.domain.services.SecurityService;
import com.gomdolbook.api.domain.shared.CollectionNotFoundException;
import com.gomdolbook.api.domain.shared.UserValidationException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@PreAuthorizeWithContainsUser
@RequiredArgsConstructor
@Service
public class CollectionApplicationService {

    private final CollectionRepository collectionRepository;
    private final SecurityService securityService;
    private final UserRepository userRepository;

    @Caching(evict = {
        @CacheEvict(cacheNames = "collectionCache", key = "@securityService.getCacheKey(#id)"),
        @CacheEvict(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()")
    })
    @Transactional
    public void changeCollectionName(Long id, String name) {
        String email = securityService.getUserEmailFromSecurityContext();
        Collection collection = collectionRepository.findByIdAndEmail(id, email)
            .orElseThrow(() -> new CollectionNotFoundException("해당 컬렉션이 존재하지않습니다."));
        collection.changeName(name);
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "collectionCache", key = "@securityService.getCacheKey(#id)"),
        @CacheEvict(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()")
    })
    @Transactional
    public void deleteCollection(Long id) {
        Collection collection = collectionRepository.findByIdAndEmail(id,
                securityService.getUserEmailFromSecurityContext())
            .orElseThrow(() -> new CollectionNotFoundException("해당 컬렉션이 존재하지않습니다."));
        collectionRepository.delete(collection);
    }

    @CacheEvict(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()")
    @UserCheckAndSave
    @Transactional
    public void createCollection(String name) {
        User user = userRepository.find(securityService.getUserEmailFromSecurityContext())
            .orElseThrow(() -> new UserValidationException("등록된 사용자를 찾을 수 없습니다."));
        collectionRepository.save(Collection.of(user, name));
    }

    @Cacheable(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()", unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<CollectionsDTO> getCollections() {
        List<BookCoverDataInCollectionDTO> results = collectionRepository.findCollections(
            securityService.getUserEmailFromSecurityContext());
        return CollectionsDTO.from(results);
    }

    @Cacheable(cacheNames = "collectionCache", keyGenerator = "customKeyGenerator", unless = "#result.books.isEmpty()")
    @Transactional(readOnly = true)
    public CollectionDetailDTO getCollection(Long id) {
        Collection collection = collectionRepository.findByIdWithUser(id)
            .orElseThrow(() -> new CollectionNotFoundException("존재하지 않는 컬렉션입니다."));

        if (!collection.getUser().getEmail()
            .equals(securityService.getUserEmailFromSecurityContext())) {
            throw new AuthorizationDeniedException("Access Denied");
        } else {
            List<BookInfoInCollectionDTO> collectionDetails = collectionRepository.findCollection(
                securityService.getUserEmailFromSecurityContext(), id);

            return new CollectionDetailDTO(collection.getId(), collection.getName(), collectionDetails);
        }
    }
}
