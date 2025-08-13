package com.gomdolbook.api.application.collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gomdolbook.api.application.collection.dto.BookCoverDataInCollectionDTO;
import com.gomdolbook.api.application.collection.dto.BookInfoInCollectionDTO;
import com.gomdolbook.api.application.collection.dto.CollectionDetailDTO;
import com.gomdolbook.api.application.collection.dto.CollectionsDTO;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.collection.CollectionRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.User.Role;
import com.gomdolbook.api.domain.models.user.UserRepository;
import com.gomdolbook.api.domain.services.SecurityService;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WithMockCustomUser
@SpringBootTest
class CollectionApplicationCacheIT {

    @MockitoBean
    CollectionRepository collectionRepository;

    @MockitoBean
    SecurityService securityService;

    @MockitoBean
    UserRepository userRepository;

    @Autowired
    CollectionApplicationService collectionApplicationService;

    @Autowired
    CacheManager cm;

    private final String email = "test@email.com";

    @AfterEach
    void cleanUp() {
        cm.getCacheNames().forEach(cacheName -> Objects.requireNonNull(cm.getCache(cacheName)).clear());
    }

    @Test
    void getCollection() {
        User user = new User(email, "pic", Role.USER);
        Collection c = Collection.of(user, "c");
        BookInfoInCollectionDTO bookInfoInCollectionDTO = new BookInfoInCollectionDTO("t", "c",
            "i");
        when(collectionRepository.findByIdWithUser(1L)).thenReturn(Optional.of(c));
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(collectionRepository.findCollection(securityService.getUserEmailFromSecurityContext(), 1L)).thenReturn(
            List.of(bookInfoInCollectionDTO));

        CollectionDetailDTO collection = collectionApplicationService.getCollection(1L);
        CollectionDetailDTO collection2 = collectionApplicationService.getCollection(1L);

        verify(collectionRepository, times(1)).findCollection(
            securityService.getUserEmailFromSecurityContext(), 1L);
        assertThat(collection).isSameAs(collection2);
    }

    @Test
    void getCollections() {
        BookCoverDataInCollectionDTO dto = new BookCoverDataInCollectionDTO(
            1L, "name", "cover");
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(collectionRepository.findCollections(email)).thenReturn(List.of(dto));

        List<CollectionsDTO> collections = collectionApplicationService.getCollections();
        List<CollectionsDTO> collections2 = collectionApplicationService.getCollections();

        verify(collectionRepository, times(1)).findCollections(email);
        assertThat(collections).isSameAs(collections2);
    }

    @Test
    void changeName() {
        String name = "새로운 컬렉션";
        Collection mockCollection = Collection.of(new User(email, "p", Role.USER), "컬렉션");
        BookInfoInCollectionDTO bookInfoInCollectionDTO = new BookInfoInCollectionDTO("t", "c",
            "i");
        BookCoverDataInCollectionDTO dto = new BookCoverDataInCollectionDTO(
            1L, "name", "cover");
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(collectionRepository.findByIdWithUser(1L)).thenReturn(Optional.of(mockCollection));
        when(collectionRepository.findByIdAndEmail(1L, email)).thenReturn(Optional.of(mockCollection));
        when(collectionRepository.findCollection(securityService.getUserEmailFromSecurityContext(), 1L)).thenReturn(
            List.of(bookInfoInCollectionDTO));
        when(securityService.getCacheKey(1L)).thenReturn("test@email.com:1");
        when(collectionRepository.findCollections(email)).thenReturn(List.of(dto));

        collectionApplicationService.getCollections();
        collectionApplicationService.getCollection(1L);
        collectionApplicationService.changeCollectionName(1L, name);
        collectionApplicationService.getCollection(1L);
        collectionApplicationService.getCollections();

        verify(collectionRepository, times(2)).findByIdWithUser(1L);
        verify(collectionRepository, times(2)).findCollections(email);
    }

    @Test
    void createCollection() {
        BookCoverDataInCollectionDTO dto = new BookCoverDataInCollectionDTO(
            1L, "name", "cover");
        User user = new User(email, "p", Role.USER);
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(collectionRepository.findCollections(email)).thenReturn(List.of(dto));
        when(userRepository.find(securityService.getUserEmailFromSecurityContext())).thenReturn(
            Optional.of(user));

        collectionApplicationService.getCollections();
        collectionApplicationService.createCollection("name");
        collectionApplicationService.getCollections();

        verify(collectionRepository, times(2)).findCollections(email);
    }

    @Test
    void deleteCollection() {
        BookCoverDataInCollectionDTO dto = new BookCoverDataInCollectionDTO(
            1L, "name", "cover");
        BookCoverDataInCollectionDTO dto1 = new BookCoverDataInCollectionDTO(
            2L, "name1", "cover1");
        User user = new User(email, "p", Role.USER);
        Collection mockCollection = Collection.of(user, "컬렉션");
        when(collectionRepository.findByIdAndEmail(1L, email)).thenReturn(Optional.of(mockCollection));
        when(collectionRepository.findCollections(email)).thenReturn(List.of(dto, dto1));
        when(userRepository.find(securityService.getUserEmailFromSecurityContext())).thenReturn(
            Optional.of(user));
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(securityService.getCacheKey(1L)).thenReturn("test@email.com:1");

        collectionApplicationService.getCollections();
        collectionApplicationService.deleteCollection(1L);
        collectionApplicationService.getCollections();

        verify(collectionRepository, times(2)).findCollections(email);
    }
}
