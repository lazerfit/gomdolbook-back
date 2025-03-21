package com.gomdolbook.api.service;

import com.gomdolbook.api.api.dto.BookCollectionCoverDTO;
import com.gomdolbook.api.api.dto.BookCollectionCoverListResponseDTO;
import com.gomdolbook.api.api.dto.BookListResponseDTO;
import com.gomdolbook.api.api.dto.BookSaveRequestDTO;
import com.gomdolbook.api.config.annotations.PreAuthorizeWithContainsUser;
import com.gomdolbook.api.config.annotations.UserCheckAndSave;
import com.gomdolbook.api.errors.BookNotFoundException;
import com.gomdolbook.api.errors.UserValidationError;
import com.gomdolbook.api.models.BookUserCollectionModel;
import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.BookUserCollection;
import com.gomdolbook.api.persistence.entity.User;
import com.gomdolbook.api.persistence.entity.UserCollection;
import com.gomdolbook.api.persistence.repository.BookUserCollectionRepository;
import com.gomdolbook.api.persistence.repository.UserCollectionRepository;
import com.gomdolbook.api.persistence.repository.UserRepository;
import com.gomdolbook.api.service.Auth.SecurityService;
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

    @Cacheable(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()", unless = "#result.isEmpty() ")
    @Transactional(readOnly = true)
    public List<BookCollectionCoverListResponseDTO> getCollectionList() {
        List<BookCollectionCoverDTO> results = bookUserCollectionRepository.getAllCollection(
            securityService.getUserEmailFromSecurityContext());

        return BookUserCollectionModel.toListResponseDTO(results);
    }

    @Cacheable(cacheNames = "collectionCache", keyGenerator = "customKeyGenerator", unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<BookListResponseDTO> getCollection(String name) {
        return bookUserCollectionRepository.getCollection(name, securityService.getUserEmailFromSecurityContext());
    }

    @UserCheckAndSave
    @Transactional
    public void createCollection(String name) {
        User user = userRepository.findByEmail(securityService.getUserEmailFromSecurityContext())
            .orElseThrow(() -> new UserValidationError("등록된 사용자를 찾을 수 없습니다."));
        UserCollection userCollection = new UserCollection(name);
        userCollection.setUser(user);
        userCollectionRepository.save(userCollection);
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "collectionCache", key = "@securityService.getCacheKey(#collectionName)"),
        @CacheEvict(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()")
    })
    @Transactional
    public void addBook(BookSaveRequestDTO dto, String collectionName) {
        UserCollection collection = userCollectionRepository.findByName(collectionName)
            .orElseThrow(RuntimeException::new);
        User user = userRepository.findByEmail(securityService.getUserEmailFromSecurityContext())
            .orElseThrow(() -> new UserValidationError("해당 유저를 찾을 수 없습니다."));
        bookService.findByIsbn(dto.isbn13()).ifPresentOrElse(book -> {
            boolean isBookUserCollectionExists = bookUserCollectionRepository.existsBookAndUser(
                book, user);
            if (!isBookUserCollectionExists) {
                BookUserCollection bookUserCollection = new BookUserCollection();
                bookUserCollection.setBook(book);
                bookUserCollection.setUserCollection(collection);
                bookUserCollection.setUser(user);
                bookUserCollectionRepository.save(bookUserCollection);
            }
        },
            () -> {
                Book book = bookService.saveOrUpdateBook(dto);
                BookUserCollection bookUserCollection = new BookUserCollection();
                bookUserCollection.setBook(book);
                bookUserCollection.setUserCollection(collection);
                bookUserCollection.setUser(user);
                bookUserCollectionRepository.save(bookUserCollection);
            });
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

        book.ifPresentOrElse(bookUserCollectionRepository::delete,
            () ->  {
                throw new BookNotFoundException("Can't find book : " + isbn);});
    }

}
