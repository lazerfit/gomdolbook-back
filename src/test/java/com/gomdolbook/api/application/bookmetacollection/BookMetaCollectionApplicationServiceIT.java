package com.gomdolbook.api.application.bookmetacollection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gomdolbook.api.application.book.command.BookMetaSaveCommand;
import com.gomdolbook.api.application.collection.dto.BookInfoInCollectionDTO;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.bookmeta.BookMetaRepository;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.collection.CollectionRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.User.Role;
import com.gomdolbook.api.domain.models.user.UserRepository;
import com.gomdolbook.api.domain.shared.BookNotFoundException;
import com.gomdolbook.api.domain.shared.BookNotInCollectionException;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@WithMockCustomUser
@Transactional
@SpringBootTest
class BookMetaCollectionApplicationServiceIT {

    @Autowired
    BookMetaCollectionApplicationService bookMetaCollectionApplicationService;

    @Autowired
    BookMetaRepository bookMetaRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CollectionRepository collectionRepository;

    @Autowired
    EntityManager em;

    private Long collectionId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(new User("test@email.com", "pic", Role.USER));
        BookMeta bookMeta = BookMeta.builder().title("t").author("a").pubDate("p")
            .description("d").isbn("i").cover("c").categoryName("ca").publisher("p").build();
        bookMetaRepository.save(bookMeta);
        Collection collection = collectionRepository.save(Collection.of(user, "collection"));
        collectionId = collection.getId();
    }

    @Test
    void addBookToCollection() {
        BookMetaSaveCommand bookMetaSaveCommand = new BookMetaSaveCommand("t", "a", "p", "d", "i",
            "c", "ca", "p");
        bookMetaCollectionApplicationService.addBookToCollection(bookMetaSaveCommand, collectionId);

        em.flush();
        em.clear();

        List<BookInfoInCollectionDTO> collection = collectionRepository.findCollection(
            "test@email.com", collectionId);

        assertThat(collection).hasSize(1);
    }

    @Test
    void removeBookFromCollection() {
        BookMetaSaveCommand bookMetaSaveCommand = new BookMetaSaveCommand("t", "a", "p", "d", "i",
            "c", "ca", "p");
        bookMetaCollectionApplicationService.addBookToCollection(bookMetaSaveCommand, collectionId);
        em.flush();
        em.clear();
        List<BookInfoInCollectionDTO> collection = collectionRepository.findCollection(
            "test@email.com", collectionId);
        assertThat(collection).hasSize(1);

        bookMetaCollectionApplicationService.removeBookFromCollection("i", collectionId);
        em.flush();
        em.clear();
        List<BookInfoInCollectionDTO> collectionAfterRemove = collectionRepository.findCollection(
            "test@email.com", collectionId);

        assertThat(collectionAfterRemove).isEmpty();
    }

    @Test
    void removeBookFromCollection_BookNotFound() {
        BookMetaSaveCommand bookMetaSaveCommand = new BookMetaSaveCommand("t", "a", "p", "d", "i",
            "c", "ca", "p");
        bookMetaCollectionApplicationService.addBookToCollection(bookMetaSaveCommand, collectionId);
        em.flush();
        em.clear();

        assertThatThrownBy(() -> bookMetaCollectionApplicationService.removeBookFromCollection("ii",
            collectionId)).isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void removeBookFromCollection_BookNotInCollection() {
        assertThatThrownBy(
            () -> bookMetaCollectionApplicationService.removeBookFromCollection("i", collectionId))
            .isInstanceOf(BookNotInCollectionException.class);
    }
}
