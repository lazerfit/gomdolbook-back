package com.gomdolbook.api.application.bookmetacollection;

import com.gomdolbook.api.application.book.command.BookMetaSaveCommand;
import com.gomdolbook.api.common.config.annotations.PreAuthorizeWithContainsUser;
import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.bookmeta.BookMetaRepository;
import com.gomdolbook.api.domain.models.bookmetacollection.BookMetaCollection;
import com.gomdolbook.api.domain.models.bookmetacollection.BookMetaCollectionRepository;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.collection.CollectionRepository;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.UserRepository;
import com.gomdolbook.api.domain.services.SecurityService;
import com.gomdolbook.api.domain.shared.BookDuplicatedInCollectionException;
import com.gomdolbook.api.domain.shared.BookNotFoundException;
import com.gomdolbook.api.domain.shared.BookNotInCollectionException;
import com.gomdolbook.api.domain.shared.CollectionNotFoundException;
import com.gomdolbook.api.domain.shared.UserValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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

    @Caching(evict = {
        @CacheEvict(cacheNames = "collectionCache", key = "@securityService.getCacheKey(#id)"),
        @CacheEvict(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()")
    })
    @Transactional
    public void addBookToCollection(BookMetaSaveCommand command, Long id) {
        String email = securityService.getUserEmailFromSecurityContext();
        User user = getCurrentUser(email);
        Collection collection = collectionRepository.findByIdAndEmail(id, email)
            .orElseThrow(() -> new CollectionNotFoundException("해당 컬렉션을 찾을 수 없습니다."));
        BookMeta bookMeta = bookMetaRepository.findByIsbn(command.isbn())
            .orElseGet(() -> bookMetaRepository.save(BookMeta.of(command)));

        boolean exists = bookMetaCollectionRepository.isExistsByBookMetaAndCollection(user, collection,
            bookMeta);
        if (exists) {
            throw new BookDuplicatedInCollectionException("이미 등록된 책입니다");
        }
        bookMetaCollectionRepository.save(BookMetaCollection.of(bookMeta, collection, user));
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "collectionCache", key = "@securityService.getCacheKey(#id)"),
        @CacheEvict(cacheNames = "collectionListCache", key = "@securityService.getUserEmailFromSecurityContext()")
    })
    @Transactional
    public void removeBookFromCollection(String isbn, Long id) {
        String email = securityService.getUserEmailFromSecurityContext();
        User user = getCurrentUser(email);
        BookMeta bookMeta = getBookMetaByIsbn(isbn);
        Collection collection = collectionRepository.findByIdAndEmail(id, user.getEmail())
            .orElseThrow(() -> new CollectionNotFoundException("해당 컬렉션을 찾을 수 없습니다."));

        BookMetaCollection bookMetaCollection = bookMetaCollectionRepository.findByUserAndBookMetaAndCollection(
                user, bookMeta, collection)
            .orElseThrow(() -> new BookNotInCollectionException("해당 컬렉션에 등록되어있지 않은 책입니다."));
        bookMetaCollectionRepository.delete(bookMetaCollection);
        bookMeta.getBookMetaCollections().remove(bookMetaCollection);
        collection.getBookMetaCollections().remove(bookMetaCollection);
    }

    private User getCurrentUser(String email) {
        return userRepository.find(email)
            .orElseThrow(() -> new UserValidationException("해당 유저를 찾을 수 없습니다: " + email));
    }

    private BookMeta getBookMetaByIsbn(String isbn) {
        return bookMetaRepository.findByIsbn(isbn)
            .orElseThrow(() -> new BookNotFoundException("책을 찾을 수 없습니다: " + isbn));
    }

    @Transactional(readOnly = true)
    public boolean isBookExistsInCollection(String name, String isbn) {
        String email = securityService.getUserEmailFromSecurityContext();
        return bookMetaCollectionRepository.existsBookInCollection(email, name, isbn);
    }
}
