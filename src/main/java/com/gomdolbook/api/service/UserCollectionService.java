package com.gomdolbook.api.service;

import static com.gomdolbook.api.utils.SecurityUtil.getUserEmailFromSecurityContext;

import com.gomdolbook.api.api.dto.BookSaveRequestDTO;
import com.gomdolbook.api.api.dto.CollectionListResponseDTO;
import com.gomdolbook.api.config.annotations.PreAuthorizeWithContainsUser;
import com.gomdolbook.api.config.annotations.UserCheckAndSave;
import com.gomdolbook.api.errors.UserValidationError;
import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.BookUserCollection;
import com.gomdolbook.api.persistence.entity.User;
import com.gomdolbook.api.persistence.entity.UserCollection;
import com.gomdolbook.api.persistence.repository.BookUserCollectionRepository;
import com.gomdolbook.api.persistence.repository.UserCollectionRepository;
import com.gomdolbook.api.persistence.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@PreAuthorizeWithContainsUser
@RequiredArgsConstructor
@Service
public class UserCollectionService {

    private final UserCollectionRepository userCollectionRepository;
    private final BookUserCollectionRepository bookUserCollectionRepository;
    private final UserRepository userRepository;
    private final BookService bookService;

    @Transactional(readOnly = true)
    public List<CollectionListResponseDTO> getCollectionList() {
        return userCollectionRepository.findByEmail(getUserEmailFromSecurityContext());
    }

    @UserCheckAndSave
    @Transactional
    public void createCollection(String name) {
        UserCollection collection = userCollectionRepository.save(new UserCollection(name));
        User user = userRepository.findByEmail(getUserEmailFromSecurityContext())
            .orElseThrow(() -> new UserValidationError("등록된 사용자를 찾을 수 없습니다."));
        collection.setUser(user);
    }

    @Transactional
    public void addBook(BookSaveRequestDTO dto, String collectionName) {
        UserCollection collection = userCollectionRepository.findByName(collectionName)
            .orElseThrow(RuntimeException::new);
        User user = userRepository.findByEmail(getUserEmailFromSecurityContext())
            .orElseThrow(() -> new UserValidationError("해당 유저를 찾을 수 없습니다."));
        bookService.findByIsbn(dto.isbn13()).ifPresentOrElse(book -> {
                BookUserCollection bookUserCollection = new BookUserCollection();
                bookUserCollection.setBook(book);
                bookUserCollection.setUserCollection(collection);
                bookUserCollection.setUser(user);
                bookUserCollectionRepository.save(bookUserCollection);
            },
            () -> {
                Book book = bookService.saveBook(dto);
                BookUserCollection bookUserCollection = new BookUserCollection();
                bookUserCollection.setBook(book);
                bookUserCollection.setUserCollection(collection);
                bookUserCollection.setUser(user);
                bookUserCollectionRepository.save(bookUserCollection);
            });
    }
}
