package com.gomdolbook.api.application.bookmetacollection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gomdolbook.api.application.book.command.BookMetaSaveCommand;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.bookmeta.BookMetaRepository;
import com.gomdolbook.api.domain.models.bookmetacollection.BookMetaCollectionRepository;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.collection.CollectionRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.User.Role;
import com.gomdolbook.api.domain.models.user.UserRepository;
import com.gomdolbook.api.domain.services.SecurityService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@WithMockCustomUser
@Transactional
@SpringBootTest
class BookMetaCollectionApplicationServiceTest {

    @Autowired
    BookMetaCollectionApplicationService service;
    @Autowired
    BookMetaCollectionRepository bookMetaCollectionRepository;
    @Autowired
    BookMetaRepository bookMetaRepository;
    @Autowired
    CollectionRepository collectionRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    SecurityService securityService;

    static User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("redkafe@daum.net", "img", Role.USER));
    }

    @Test
    void addBookToCollection_정상동작() {
        collectionRepository.save(Collection.of(user, "내컬렉션"));
        BookMetaSaveCommand command = new BookMetaSaveCommand("제목", "저자", "2025-01-01", "설명", "1234567890123", "cover", "카테고리", "출판사");

        service.addBookToCollection(command, "내컬렉션");
        assertThat(bookMetaCollectionRepository.findAll()).hasSize(1);
    }

    @Test
    void addBookToCollection_이미존재하는BookMeta_중복등록불가() {
        collectionRepository.save(Collection.of(user, "내컬렉션"));
        BookMetaSaveCommand command = new BookMetaSaveCommand("제목", "저자", "2025-01-01", "설명", "1234567890123", "cover", "카테고리", "출판사");

        service.addBookToCollection(command, "내컬렉션");

        assertThrows(IllegalStateException.class, () -> service.addBookToCollection(command, "내컬렉션"));
    }

    @Test
    void addBookToCollection_필수값누락_예외발생() {
        BookMetaSaveCommand command = new BookMetaSaveCommand(null, "저자", "2025-01-01", "설명", "1234567890123", "cover", "카테고리", "출판사");
        assertThrows(Exception.class, () -> service.addBookToCollection(command, "내컬렉션"));
    }

    @Test
    void removeBookFromCollection_존재하지않는책_예외() {
        collectionRepository.save(Collection.of(user, "내컬렉션"));
        assertThrows(IllegalStateException.class, () -> service.removeBookFromCollection("9999999999999", "내컬렉션"));
    }

    @Test
    void removeBookFromCollection_정상삭제_연관관계정상() {
        Collection collection = collectionRepository.save(Collection.of(user, "내컬렉션"));
        BookMetaSaveCommand command = new BookMetaSaveCommand("제목", "저자",
            "2025-01-01", "설명", "1234567890123", "cover", "카테고리", "출판사");
        service.addBookToCollection(command, "내컬렉션");

        service.removeBookFromCollection("1234567890123", "내컬렉션");

        assertThat(bookMetaCollectionRepository.findAll()).isEmpty();
        assertThat(collection.getBookMetaCollections()).isEmpty();
        BookMeta meta = bookMetaRepository.findByIsbn("1234567890123").orElse(null);
        assertThat(meta).isNotNull();
    }

    @Test
    void deleteCollection_컬렉션삭제시_BookMetaCollection_삭제_bookMeta_존재() {
        Collection collection = collectionRepository.save(Collection.of(user, "내컬렉션"));
        BookMetaSaveCommand command = new BookMetaSaveCommand("제목", "저자", "2025-01-01", "설명", "1234567890123", "cover", "카테고리", "출판사");
        service.addBookToCollection(command, "내컬렉션");

        service.deleteCollection("내컬렉션");

        assertThat(bookMetaCollectionRepository.findAll()).isEmpty();
        assertThat(collectionRepository.findById(collection.getId())).isNotPresent();
        assertThat(bookMetaRepository.findByIsbn("1234567890123")).isPresent();
    }
}
