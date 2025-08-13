package com.gomdolbook.api.application.collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gomdolbook.api.application.collection.dto.BookCoverDataInCollectionDTO;
import com.gomdolbook.api.application.collection.dto.BookInfoInCollectionDTO;
import com.gomdolbook.api.application.collection.dto.CollectionDetailDTO;
import com.gomdolbook.api.application.collection.dto.CollectionsDTO;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.collection.CollectionRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.User.Role;
import com.gomdolbook.api.domain.models.user.UserRepository;
import com.gomdolbook.api.domain.services.SecurityService;
import com.gomdolbook.api.domain.shared.CollectionNotFoundException;
import com.gomdolbook.api.domain.shared.UserValidationException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CollectionApplicationServiceTest {

    @InjectMocks
    private CollectionApplicationService collectionApplicationService;

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private UserRepository userRepository;

    @Test
    void changeCollectionName_정상동작() {
        Long id = 1L;
        String name = "새로운 컬렉션";
        String email = "test@email.com";

        Collection mockCollection = Collection.of(new User(email, "p", Role.USER), "컬렉션");

        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(collectionRepository.findByIdAndEmail(id, email)).thenReturn(java.util.Optional.of(mockCollection));

        collectionApplicationService.changeCollectionName(id, name);

        assertThat(mockCollection.getName()).isEqualTo(name);
        verify(collectionRepository).findByIdAndEmail(id,email);
    }

    @Test
    void changeCollectionName_withNonExistId_throwException() {
        Long id = 1L;
        String email = "test@email.com";

        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(collectionRepository.findByIdAndEmail(id, email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionApplicationService.changeCollectionName(id, "name")).isInstanceOf(
            CollectionNotFoundException.class);
    }

    @Test
    void deleteCollection_정상동작() {
        Long id = 1L;
        String email = "test@email.com";

        Collection mockCollection = Collection.of(new User(email, "p", Role.USER), "컬렉션");
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(collectionRepository.findByIdAndEmail(id, email)).thenReturn(Optional.of(mockCollection));

        collectionApplicationService.deleteCollection(id);
        verify(collectionRepository).delete(mockCollection);
    }

    @Test
    void deleteCollection_NonExistId_ThrowException() {
        Long id = 1L;
        String email = "test@email.com";

        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(collectionRepository.findByIdAndEmail(id, email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionApplicationService.deleteCollection(id)).isInstanceOf(
            CollectionNotFoundException.class);
    }

    @Test
    void createCollection_정상동작() {
        String name = "컬렉션";
        String email = "test@email.com";
        User user = new User(email, "p", Role.USER);

        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(userRepository.find(securityService.getUserEmailFromSecurityContext())).thenReturn(
            Optional.of(user));

        collectionApplicationService.createCollection(name);
        verify(collectionRepository).save(any(Collection.class));
    }

    @Test
    void createCollection_NonExistUser_ThrowException() {
        String email = "test@email.com";
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(userRepository.find(securityService.getUserEmailFromSecurityContext())).thenReturn(
            Optional.empty());

        assertThatThrownBy(() -> collectionApplicationService.createCollection("name"))
            .isInstanceOf(UserValidationException.class);
    }

    @Test
    void getCollections() {
        String email = "test@email.com";
        BookCoverDataInCollectionDTO dto = new BookCoverDataInCollectionDTO(
            1L, "name", "cover");
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(collectionRepository.findCollections(email)).thenReturn(List.of(dto));

        List<CollectionsDTO> collections = collectionApplicationService.getCollections();

        assertThat(collections.getFirst().id()).isEqualTo(1L);
        assertThat(collections.getFirst().name()).isEqualTo("name");
        assertThat(collections.getFirst().covers()).hasSize(1);
        assertThat(collections.getFirst().covers().getFirst()).isEqualTo("cover");
    }

    @Test
    void getCollection() {
        String email = "test@email.com";
        User user = new User(email, "pic", Role.USER);
        Collection c = Collection.of(user, "c");
        BookInfoInCollectionDTO bookInfoInCollectionDTO = new BookInfoInCollectionDTO("t", "c",
            "i");
        when(collectionRepository.findByIdWithUser(1L)).thenReturn(Optional.of(c));
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(collectionRepository.findCollection(securityService.getUserEmailFromSecurityContext(), 1L)).thenReturn(
            List.of(bookInfoInCollectionDTO));

        CollectionDetailDTO collection = collectionApplicationService.getCollection(1L);

        assertThat(collection.collectionName()).isEqualTo("c");
        assertThat(collection.books()).hasSize(1);
        assertThat(collection.books().getFirst().title()).isEqualTo("t");
    }

    @Test
    void getCollection_NonExistId_throwException() {
        when(collectionRepository.findByIdWithUser(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionApplicationService.getCollection(1L)).isInstanceOf(
            CollectionNotFoundException.class);
    }

}
