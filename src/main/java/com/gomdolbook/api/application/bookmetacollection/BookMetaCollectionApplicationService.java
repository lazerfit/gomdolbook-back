package com.gomdolbook.api.application.bookmetacollection;

import com.gomdolbook.api.application.book.command.BookMetaSaveCommand;
import com.gomdolbook.api.application.book.dto.BookCollectionCoverData;
import com.gomdolbook.api.application.book.dto.BookCollectionCoverListData;
import com.gomdolbook.api.application.bookmetacollection.dto.CollectionBookMetaData;
import com.gomdolbook.api.common.config.annotations.PreAuthorizeWithContainsUser;
import com.gomdolbook.api.common.config.annotations.UserCheckAndSave;
import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.bookmeta.BookMetaRepository;
import com.gomdolbook.api.domain.models.bookmetacollection.BookMetaCollection;
import com.gomdolbook.api.domain.models.bookmetacollection.BookMetaCollectionRepository;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.collection.CollectionRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.UserRepository;
import com.gomdolbook.api.domain.services.SecurityService;
import com.gomdolbook.api.domain.shared.BookNotFoundException;
import com.gomdolbook.api.domain.shared.BookNotInCollectionException;
import com.gomdolbook.api.domain.shared.CollectionNotFoundException;
import com.gomdolbook.api.domain.shared.UserValidationError;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@PreAuthorizeWithContainsUser
@RequiredArgsConstructor
@Service
public class BookMetaCollectionApplicationService {

    private final CollectionRepository collectionRepository;
    private final BookMetaCollectionRepository bookMetaCollectionRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final BookMetaRepository bookMetaRepository;

    @Cacheable(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()", unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<BookCollectionCoverListData> getCollectionList() {
        List<BookCollectionCoverData> results = bookMetaCollectionRepository.getAllCollection(
            securityService.getUserEmailFromSecurityContext());
        return BookCollectionCoverListData.from(results);
    }

    @Cacheable(cacheNames = "collectionCache", keyGenerator = "customKeyGenerator", unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<CollectionBookMetaData> getCollection(String name) {
        collectionRepository.find(name, securityService.getUserEmailFromSecurityContext())
            .orElseThrow(() -> new CollectionNotFoundException("존재하지 않는 컬렉션입니다."));
        return bookMetaCollectionRepository.getCollectionData(
            securityService.getUserEmailFromSecurityContext(), name);
    }

    @CacheEvict(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()")
    @UserCheckAndSave
    @Transactional
    public void createCollection(String name) {
        User user = userRepository.find(securityService.getUserEmailFromSecurityContext())
            .orElseThrow(() -> new UserValidationError("등록된 사용자를 찾을 수 없습니다."));
        collectionRepository.save(Collection.of(user, name));
        log.info(">>>>>>>>>>>>> createCollection called >>>>>>>>>>>");
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "collectionCache", key = "@securityService.getCacheKey(#name)"),
        @CacheEvict(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()")
    })
    @Transactional
    public void deleteCollection(String name) {
        Collection collection = collectionRepository.find(name,
                securityService.getUserEmailFromSecurityContext())
            .orElseThrow(() -> new CollectionNotFoundException("해당 컬렉션을 찾을 수 없습니다."));
        collectionRepository.delete(collection);
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "collectionCache", key = "@securityService.getCacheKey(#collectionName)"),
        @CacheEvict(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()")
    })
    @Transactional
    public void addBookToCollection(BookMetaSaveCommand command, String collectionName) {
        String email = securityService.getUserEmailFromSecurityContext();
        User user = getCurrentUser();
        Collection collection = getCollectionByNameAndUser(collectionName, email);
        BookMeta bookMeta = bookMetaRepository.findByIsbn(command.isbn())
            .orElseGet(() -> bookMetaRepository.save(BookMeta.of(command)));

        boolean exists = bookMetaCollectionRepository.existsByBookMetaAndCollection(user, collection,
            bookMeta);
        if (exists) {
            throw new IllegalStateException("이미 등록된 책입니다");
        }
        bookMetaCollectionRepository.save(BookMetaCollection.of(bookMeta, collection, user));
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "collectionCache", key = "@securityService.getCacheKey(#collectionName)"),
        @CacheEvict(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()")
    })
    @Transactional
    public void removeBookFromCollection(String isbn, String collectionName) {
        String email = securityService.getUserEmailFromSecurityContext();
        User user = getCurrentUser();
        BookMeta bookMeta = getBookMetaByIsbn(isbn);
        Collection collection = getCollectionByNameAndUser(collectionName, email);

        BookMetaCollection bookMetaCollection = bookMetaCollectionRepository.findByUserAndBookMetaAndCollection(
                user, bookMeta, collection)
            .orElseThrow(() -> new BookNotInCollectionException("해당 컬렉션에 등록되어있지 않은 책입니다."));
        bookMetaCollectionRepository.delete(bookMetaCollection);
        bookMeta.getBookMetaCollections().remove(bookMetaCollection);
        collection.getBookMetaCollections().remove(bookMetaCollection);
    }

    private User getCurrentUser() {
        String email = securityService.getUserEmailFromSecurityContext();
        return userRepository.find(email)
            .orElseThrow(() -> new UserValidationError("해당 유저를 찾을 수 없습니다: " + email));
    }

    private BookMeta getBookMetaByIsbn(String isbn) {
        return bookMetaRepository.findByIsbn(isbn)
            .orElseThrow(() -> new BookNotFoundException("책을 찾을 수 없습니다: " + isbn));
    }

    private Collection getCollectionByNameAndUser(String collectionName, String email) {
        return collectionRepository.find(collectionName, email)
            .orElseThrow(() -> new CollectionNotFoundException("컬렉션을 찾을 수 없습니다: " + collectionName));
    }

    @Transactional(readOnly = true)
    public boolean isBookExistsInCollection(String name, String isbn) {
        String email = securityService.getUserEmailFromSecurityContext();
        return bookMetaCollectionRepository.existsBookInCollection(email, name, isbn);
    }
}
