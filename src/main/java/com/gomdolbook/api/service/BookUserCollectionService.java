package com.gomdolbook.api.service;

import com.gomdolbook.api.api.dto.book.BookCollectionCoverDTO;
import com.gomdolbook.api.api.dto.book.BookCollectionCoverListResponseDTO;
import com.gomdolbook.api.api.dto.book.BookListResponseDTO;
import com.gomdolbook.api.api.dto.book.BookSaveRequestDTO;
import com.gomdolbook.api.config.annotations.PreAuthorizeWithContainsUser;
import com.gomdolbook.api.config.annotations.UserCheckAndSave;
import com.gomdolbook.api.errors.BookNotFoundException;
import com.gomdolbook.api.errors.UserValidationError;
import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.BookUserCollection;
import com.gomdolbook.api.persistence.entity.User;
import com.gomdolbook.api.persistence.entity.UserCollection;
import com.gomdolbook.api.persistence.repository.BookRepository;
import com.gomdolbook.api.persistence.repository.BookUserCollectionRepository;
import com.gomdolbook.api.persistence.repository.ReadingLogRepository;
import com.gomdolbook.api.persistence.repository.UserCollectionRepository;
import com.gomdolbook.api.persistence.repository.UserRepository;
import com.gomdolbook.api.service.Auth.SecurityService;
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

@Slf4j
@PreAuthorizeWithContainsUser
@RequiredArgsConstructor
@Service
public class BookUserCollectionService {

    private final UserCollectionRepository userCollectionRepository;
    private final BookUserCollectionRepository bookUserCollectionRepository;
    private final UserRepository userRepository;
    private final BookService bookService;
    private final SecurityService securityService;
    private final ReadingLogRepository readingLogRepository;
    private final BookRepository bookRepository;

    @Cacheable(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()", unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<BookCollectionCoverListResponseDTO> getCollectionList() {
        List<BookCollectionCoverDTO> results = bookUserCollectionRepository.getAllCollection(
            securityService.getUserEmailFromSecurityContext());

        return BookCollectionCoverListResponseDTO.from(results);
    }

    @Cacheable(cacheNames = "collectionCache", keyGenerator = "customKeyGenerator", unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<BookListResponseDTO> getCollection(String name) {
        return bookUserCollectionRepository.getCollection(name, securityService.getUserEmailFromSecurityContext());
    }

    @CacheEvict(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()")
    @UserCheckAndSave
    @Transactional
    public void createCollection(String name) {
        User user = userRepository.findByEmail(securityService.getUserEmailFromSecurityContext())
            .orElseThrow(() -> new UserValidationError("등록된 사용자를 찾을 수 없습니다."));
        UserCollection userCollection = UserCollection.of(user, name);
        userCollectionRepository.save(userCollection);
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "collectionCache", key = "@securityService.getCacheKey(#name)"),
        @CacheEvict(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()")
    })
    @Transactional
    public void deleteCollection(String name) {
        UserCollection userCollection = userCollectionRepository.findByNameAndEmail(name,
            securityService.getUserEmailFromSecurityContext());
        for (BookUserCollection bu : new ArrayList<>(userCollection.getBookUserCollections())) {
            bu.getBook().getBookUserCollections().remove(bu);
            bookUserCollectionRepository.delete(bu);
        }
        userCollection.getBookUserCollections().clear();
        userCollectionRepository.delete(userCollection);
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "collectionCache", key = "@securityService.getCacheKey(#collectionName)"),
        @CacheEvict(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()")
    })
    @Transactional
    public void addBook(BookSaveRequestDTO dto, String collectionName) {
        String email = securityService.getUserEmailFromSecurityContext();
        UserCollection collection = userCollectionRepository.findByName(collectionName,
                email)
            .orElseThrow(() -> new RuntimeException("컬렉션을 찾을 수 없습니다." + collectionName));
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserValidationError("해당 유저를 찾을 수 없습니다."));

        Book book = bookService.findByIsbn(dto.isbn13()).orElseGet(() -> bookService.saveBook(dto));
        log.info(">>> book.id = {}", book.getId());
        boolean isDuplicate = bookUserCollectionRepository.existsBook(user, collection, book);
        if (!isDuplicate) {
            BookUserCollection bookUserCollection = BookUserCollection.of(user, collection, book);
            log.info(">>> BUC: user = {}, collection = {}, book = {}",
                bookUserCollection.getUser().getEmail(),
                bookUserCollection.getUserCollection().getName(),
                bookUserCollection.getBook().getTitle());

            bookUserCollectionRepository.save(bookUserCollection);
        }
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "collectionCache", key = "@securityService.getCacheKey(#collectionName)"),
        @CacheEvict(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()")
    })
    @Transactional
    public void deleteBook(String isbn, String collectionName) {
        Optional<BookUserCollection> book = bookUserCollectionRepository.findByIsbnAndName(
            isbn, collectionName,
            securityService.getUserEmailFromSecurityContext());

        book.ifPresentOrElse(b -> {
            bookUserCollectionRepository.delete(b);
                bookRepository.delete(b.getBook());
                readingLogRepository.delete(b.getBook().getReadingLog());
                b.getUserCollection().getBookUserCollections().remove(b);
                b.getBook().getBookUserCollections().remove(b);
            },
            () ->  {
                throw new BookNotFoundException("Can't find book : " + isbn);});
    }

}
