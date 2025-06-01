package com.gomdolbook.api.application.bookCollection;

import com.gomdolbook.api.application.book.BookApplicationService;
import com.gomdolbook.api.application.book.command.BookSaveCommand;
import com.gomdolbook.api.application.book.dto.BookCollectionCoverData;
import com.gomdolbook.api.application.book.dto.BookCollectionCoverListData;
import com.gomdolbook.api.application.book.dto.BookListData;
import com.gomdolbook.api.common.config.annotations.DomainService;
import com.gomdolbook.api.common.config.annotations.PreAuthorizeWithContainsUser;
import com.gomdolbook.api.common.config.annotations.UserCheckAndSave;
import com.gomdolbook.api.domain.models.book.Book;
import com.gomdolbook.api.domain.models.book.BookRepository;
import com.gomdolbook.api.domain.models.bookcollection.BookCollection;
import com.gomdolbook.api.domain.models.bookcollection.BookCollectionRepository;
import com.gomdolbook.api.domain.models.bookmeta.BookMetaRepository;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.collection.CollectionRepository;
import com.gomdolbook.api.domain.models.readinglog.ReadingLogRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.UserRepository;
import com.gomdolbook.api.domain.services.SecurityService;
import com.gomdolbook.api.domain.shared.BookNotFoundException;
import com.gomdolbook.api.domain.shared.CollectionNotFoundException;
import com.gomdolbook.api.domain.shared.UserValidationError;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@DomainService
@Slf4j
@PreAuthorizeWithContainsUser
@RequiredArgsConstructor
@Service
public class BookCollectionApplicationService {

    private final CollectionRepository collectionRepository;
    private final BookCollectionRepository bookCollectionRepository;
    private final UserRepository userRepository;
    private final BookApplicationService bookApplicationService;
    private final SecurityService securityService;
    private final ReadingLogRepository readingLogRepository;
    private final BookRepository bookRepository;
    private final BookMetaRepository bookMetaRepository;

    @Cacheable(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()", unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<BookCollectionCoverListData> getCollectionList() {
        List<BookCollectionCoverData> results = bookCollectionRepository.getAllCollection(
            securityService.getUserEmailFromSecurityContext());

        return BookCollectionCoverListData.from(results);
    }

    @Cacheable(cacheNames = "collectionCache", keyGenerator = "customKeyGenerator", unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<BookListData> getCollection(String name) {
        return bookCollectionRepository.getCollection(name, securityService.getUserEmailFromSecurityContext());
    }

    @CacheEvict(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()")
    @UserCheckAndSave
    @Transactional
    public void createCollection(String name) {
        User user = userRepository.find(securityService.getUserEmailFromSecurityContext())
            .orElseThrow(() -> new UserValidationError("등록된 사용자를 찾을 수 없습니다."));
        Collection collection = Collection.of(user, name);
        collectionRepository.save(collection);
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "collectionCache", key = "@securityService.getCacheKey(#name)"),
        @CacheEvict(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()")
    })
    @Transactional
    public void deleteCollection(String name) {
        Collection collection = collectionRepository.find(name,
            securityService.getUserEmailFromSecurityContext()).orElseThrow(() -> new CollectionNotFoundException(name));
        for (BookCollection bu : new ArrayList<>(collection.getBookCollections())) {
            bu.getBook().getBookCollections().remove(bu);
            bookCollectionRepository.delete(bu);
        }
        collection.getBookCollections().clear();
        collectionRepository.delete(collection);
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "collectionCache", key = "@securityService.getCacheKey(#collectionName)"),
        @CacheEvict(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()")
    })
    @Transactional
    public void addBook(BookSaveCommand command, String collectionName) {
        String email = securityService.getUserEmailFromSecurityContext();
        User user = userRepository.find(email)
            .orElseThrow(() -> new UserValidationError("해당 유저를 찾을 수 없습니다."));
        Collection collection = collectionRepository.find(collectionName,
                email)
            .orElseThrow(() -> new RuntimeException("컬렉션을 찾을 수 없습니다." + collectionName));

        Book book = bookApplicationService.find(command.isbn()).orElseGet(() -> bookApplicationService.registerBookWithMeta(command));
        boolean isDuplicate = bookCollectionRepository.existsBook(user, collection, book);
        if (!isDuplicate) {
            BookCollection bookCollection = BookCollection.of(user, collection, book);
            bookCollectionRepository.save(bookCollection);
        }
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "collectionCache", key = "@securityService.getCacheKey(#collectionName)"),
        @CacheEvict(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()")
    })
    @Transactional
    public void removeBook(String isbn, String collectionName) {
        Optional<BookCollection> book = bookCollectionRepository.find(
            isbn, collectionName,
            securityService.getUserEmailFromSecurityContext());

        book.ifPresentOrElse(b -> {
            bookCollectionRepository.delete(b);
                bookRepository.delete(b.getBook());
                readingLogRepository.delete(b.getBook().getReadingLog());
                b.getCollection().getBookCollections().remove(b);
                b.getBook().getBookCollections().remove(b);
            },
            () ->  {
                throw new BookNotFoundException("Can't find book : " + isbn);});
    }

}
