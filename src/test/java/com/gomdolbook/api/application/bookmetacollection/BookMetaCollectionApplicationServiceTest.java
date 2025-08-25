package com.gomdolbook.api.application.bookmetacollection;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gomdolbook.api.application.book.command.BookMetaSaveCommand;
import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.bookmeta.BookMetaRepository;
import com.gomdolbook.api.domain.models.bookmetacollection.BookMetaCollection;
import com.gomdolbook.api.domain.models.bookmetacollection.BookMetaCollectionRepository;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.collection.CollectionRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.User.Role;
import com.gomdolbook.api.domain.models.user.UserRepository;
import com.gomdolbook.api.domain.services.SecurityService;
import com.gomdolbook.api.domain.shared.BookDuplicatedInCollectionException;
import com.gomdolbook.api.domain.shared.BookNotFoundException;
import com.gomdolbook.api.domain.shared.BookNotInCollectionException;
import com.gomdolbook.api.domain.shared.CollectionNotFoundException;
import com.gomdolbook.api.domain.shared.UserValidationException;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ExtendWith(MockitoExtension.class)
class BookMetaCollectionApplicationServiceTest {

    @Mock
    BookMetaCollectionRepository bookMetaCollectionRepository;

    @InjectMocks
    BookMetaCollectionApplicationService bookMetaCollectionApplicationService;

    @Mock
    CollectionRepository collectionRepository;

    @Mock
    BookMetaRepository bookMetaRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    SecurityService securityService;

    private final String email = "test@email.com";
    private final BookMeta bookMeta = BookMeta.builder().title("t").author("a").pubDate("p")
        .description("d").isbn("i").cover("c").categoryName("ca").publisher("p").build();

    @Test
    void addBookToCollection() {
        User user = new User(email, "pic", Role.USER);
        Collection mockCollection = Collection.of(user, "name");
        BookMetaSaveCommand bookMetaSaveCommand = new BookMetaSaveCommand("t", "a", "p", "d", "i",
            "c", "ca", "p");
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(userRepository.find(email)).thenReturn(Optional.of(user));
        when(collectionRepository.findByIdAndEmail(1L, email)).thenReturn(
            Optional.of(mockCollection));
        when(bookMetaRepository.findByIsbn("i")).thenReturn(Optional.of(bookMeta));
        when(bookMetaCollectionRepository.isExistsByBookMetaAndCollection(user, mockCollection,
            bookMeta)).thenReturn(false);

        Assertions.assertDoesNotThrow(() -> bookMetaCollectionApplicationService.addBookToCollection(bookMetaSaveCommand, 1L));

        verify(bookMetaCollectionRepository, times(1)).save(any(BookMetaCollection.class));
        verify(collectionRepository, times(1)).findByIdAndEmail(1L, email);
    }

    @Test
    void addBookToCollection_AlreadyExistBookInCollection() {
        User user = new User(email, "pic", Role.USER);
        Collection mockCollection = Collection.of(user, "name");
        BookMetaSaveCommand bookMetaSaveCommand = new BookMetaSaveCommand("t", "a", "p", "d", "i",
            "c", "ca", "p");
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(userRepository.find(email)).thenReturn(Optional.of(user));
        when(collectionRepository.findByIdAndEmail(1L, email)).thenReturn(
            Optional.of(mockCollection));
        when(bookMetaRepository.findByIsbn("i")).thenReturn(Optional.of(bookMeta));
        when(bookMetaCollectionRepository.isExistsByBookMetaAndCollection(user, mockCollection,
            bookMeta)).thenReturn(true);

        assertThatThrownBy(
                () -> bookMetaCollectionApplicationService.addBookToCollection(bookMetaSaveCommand, 1L))
            .isInstanceOf(BookDuplicatedInCollectionException.class);
    }

    @Test
    void removeBookFromCollection() {
        User user = new User(email, "pic", Role.USER);
        Collection mockCollection = Collection.of(user, "name");
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        BookMetaCollection bookMetaCollection = BookMetaCollection.of(bookMeta, mockCollection,
            user);
        when(userRepository.find(email)).thenReturn(Optional.of(user));
        when(collectionRepository.findByIdAndEmail(1L, email)).thenReturn(
            Optional.of(mockCollection));
        when(bookMetaRepository.findByIsbn("i")).thenReturn(Optional.of(bookMeta));
        when(bookMetaCollectionRepository.findByUserAndBookMetaAndCollection(user, bookMeta,
            mockCollection))
            .thenReturn(Optional.of(bookMetaCollection));

        bookMetaCollectionApplicationService.removeBookFromCollection("i", 1L);

        verify(bookMetaCollectionRepository,times(1)).delete(bookMetaCollection);
    }

    @Test
    void removeBookFromCollection_BookNotInCollection() {
        User user = new User(email, "pic", Role.USER);
        Collection mockCollection = Collection.of(user, "name");
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(userRepository.find(email)).thenReturn(Optional.of(user));
        when(collectionRepository.findByIdAndEmail(1L, email)).thenReturn(
            Optional.of(mockCollection));
        when(bookMetaRepository.findByIsbn("i")).thenReturn(Optional.of(bookMeta));
        when(bookMetaCollectionRepository.findByUserAndBookMetaAndCollection(user, bookMeta,
            mockCollection))
            .thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> bookMetaCollectionApplicationService.removeBookFromCollection("i", 1L))
            .isInstanceOf(BookNotInCollectionException.class);
    }

    @Test
    void removeBookFromCollection_UserNotFound() {
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(userRepository.find(email)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> bookMetaCollectionApplicationService.removeBookFromCollection("i", 1L))
            .isInstanceOf(UserValidationException.class);
    }

    @Test
    void removeBookFromCollection_BookNotFound() {
        User user = new User(email, "pic", Role.USER);
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(userRepository.find(email)).thenReturn(Optional.of(user));
        when(bookMetaRepository.findByIsbn("i")).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> bookMetaCollectionApplicationService.removeBookFromCollection("i", 1L))
            .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void removeBookFromCollection_CollectionNotFound() {
        User user = new User(email, "pic", Role.USER);
        when(securityService.getUserEmailFromSecurityContext()).thenReturn(email);
        when(userRepository.find(email)).thenReturn(Optional.of(user));
        when(bookMetaRepository.findByIsbn("i")).thenReturn(Optional.of(bookMeta));
        when(collectionRepository.findByIdAndEmail(1L, email)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> bookMetaCollectionApplicationService.removeBookFromCollection("i", 1L))
            .isInstanceOf(CollectionNotFoundException.class);
    }
}
