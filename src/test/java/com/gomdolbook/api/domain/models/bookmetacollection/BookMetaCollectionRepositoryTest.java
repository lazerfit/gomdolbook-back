package com.gomdolbook.api.domain.models.bookmetacollection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gomdolbook.api.application.book.command.BookMetaSaveCommand;
import com.gomdolbook.api.application.bookmetacollection.BookMetaCollectionApplicationService;
import com.gomdolbook.api.application.collection.dto.CollectionDetailDTO;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.bookmeta.BookMetaRepository;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.collection.CollectionRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.User.Role;
import com.gomdolbook.api.domain.models.user.UserRepository;
import com.gomdolbook.api.domain.shared.CollectionNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class BookMetaCollectionRepositoryTest {

    @Autowired
    BookMetaCollectionRepository bookMetaCollectionRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CollectionRepository collectionRepository;
    @Autowired
    BookMetaRepository bookMetaRepository;
    @Autowired
    BookMetaCollectionApplicationService service;

    @Test
    void saveAndFindByUser_정상등록_조회() {
        User user = new User("user@test.com", "img1", Role.USER);
        userRepository.save(user);
        Collection collection = collectionRepository.save(Collection.of(user, "내컬렉션"));
        BookMetaSaveCommand command = new BookMetaSaveCommand(
            "삭제책", "저자", "2025-01-01", "설명",
            "6234567890123", "cover", "카테고리", "출판사"
        );
        BookMeta bookMeta = bookMetaRepository.save(BookMeta.of(command));

        BookMetaCollection metaCollection = BookMetaCollection.of(bookMeta, collection, user);
        bookMetaCollectionRepository.save(metaCollection);

        List<BookMetaCollection> found = bookMetaCollectionRepository.findByUser(user);
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getBookMeta().getIsbn()).isEqualTo("6234567890123");
    }

    @Test
    void findByEmailAndCollectionName_존재하지않는경우_Error() {
        assertThatThrownBy(() -> bookMetaCollectionRepository.getCollectionData(
            "no@email.com", Long.valueOf(1000))).isInstanceOf(CollectionNotFoundException.class);
    }

    @Test
    void 삭제_정상동작() {
        User user = userRepository.save(new User("del@email.com", "img", Role.USER));
        Collection collection = collectionRepository.save(Collection.of(user, "del컬렉션"));
        BookMetaSaveCommand command = new BookMetaSaveCommand(
            "삭제책", "저자", "2025-01-01", "설명",
            "6234567890123", "cover", "카테고리", "출판사"
        );
        BookMeta bookMeta = bookMetaRepository.save(BookMeta.of(command));

        BookMetaCollection metaCollection = BookMetaCollection.of(bookMeta, collection, user);
        BookMetaCollection saved = bookMetaCollectionRepository.saveAndFlush(
            metaCollection);

        bookMetaCollectionRepository.deleteById(saved.getId());
        bookMetaCollectionRepository.flush();
        CollectionDetailDTO found = bookMetaCollectionRepository.getCollectionData(
            "del@email.com", collection.getId());
        assertThat(found.books()).isEmpty();

        assertThat(bookMetaRepository.findById(bookMeta.getId())).isPresent();
        assertThat(collectionRepository.findById(collection.getId())).isPresent();
        assertThat(userRepository.findById(user.getId())).isPresent();
    }

    @Test
    void 여러유저_동일_BookMeta_서로다른컬렉션_정상저장() {
        User user1 = userRepository.save(new User("a@email.com", "img", Role.USER));
        User user2 = userRepository.save(new User("b@email.com", "img", Role.USER));
        Collection col1 = collectionRepository.save(Collection.of(user1, "col1"));
        Collection col2 = collectionRepository.save(Collection.of(user2, "col2"));
        BookMetaSaveCommand command = new BookMetaSaveCommand(
            "삭제책", "저자", "2025-01-01", "설명",
            "6234567890123", "cover", "카테고리", "출판사"
        );
        BookMeta bookMeta = bookMetaRepository.save(BookMeta.of(command));

        BookMetaCollection metaCol1 = BookMetaCollection.of(bookMeta, col1, user1);
        BookMetaCollection metaCol2 = BookMetaCollection.of(bookMeta, col2, user2);
        bookMetaCollectionRepository.save(metaCol1);
        bookMetaCollectionRepository.save(metaCol2);

        assertThat(bookMetaCollectionRepository.findByUser(user1)).hasSize(1);
        assertThat(bookMetaCollectionRepository.findByUser(user2)).hasSize(1);
    }

    @WithMockCustomUser
    @Test
    void isBookExistsInCollection_정상동작() {
        User user = userRepository.save(new User("redkafe@daum.net", "img", Role.USER));
        BookMetaSaveCommand command = new BookMetaSaveCommand(
            "삭제책", "저자", "2025-01-01", "설명",
            "6234567890123", "cover", "카테고리", "출판사"
        );
        BookMeta bookMeta = bookMetaRepository.save(BookMeta.of(command));
        Collection collection = collectionRepository.save(Collection.of(user, "내컬렉션"));
        bookMetaCollectionRepository.save(BookMetaCollection.of(bookMeta, collection, user));

        boolean exists = service.isBookExistsInCollection("내컬렉션", "6234567890123");
        boolean notExists = service.isBookExistsInCollection("내컬렉션", "999999999");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
