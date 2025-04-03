package com.gomdolbook.api.util;

import com.gomdolbook.api.application.book.dto.BookSaveRequestDTO;
import com.gomdolbook.api.domain.models.book.Book;
import com.gomdolbook.api.domain.models.bookCollection.BookCollection;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.readingLog.ReadingLog;
import com.gomdolbook.api.domain.models.readingLog.ReadingLog.Status;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.User.Role;
import com.gomdolbook.api.domain.models.book.BookRepository;
import com.gomdolbook.api.domain.models.bookCollection.BookCollectionRepository;
import com.gomdolbook.api.domain.models.readingLog.ReadingLogRepository;
import com.gomdolbook.api.domain.models.collection.CollectionRepository;
import com.gomdolbook.api.domain.models.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestDataFactory {

    @Autowired
    CollectionRepository collectionRepository;

    @Autowired
    ReadingLogRepository readingLogRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookCollectionRepository bookCollectionRepository;

    public ReadingLog createReadingLog(User user) {
        ReadingLog readingLog = ReadingLog.of(user, Status.READING);
        readingLog.setUser(user);
        return readingLogRepository.save(readingLog);
    }

    public User createUser(String email, String pic) {
        User user = new User(email, pic, Role.USER);
        return userRepository.save(user);
    }

    public Collection createUserCollection(String name, User user) {
        Collection collection = Collection.of(user, name);
        collection.setUser(user);
        return collectionRepository.save(collection);
    }

    public void createBookUserCollection(Book book, Collection collection,
        User user) {
        BookCollection bookCollection = BookCollection.of(user, collection, book);
        bookCollectionRepository.save(bookCollection);
    }

    public Book createBook(ReadingLog readingLog) {
        Book book =  Book.builder()
            .title("펠로폰네소스 전쟁사")
            .author("투퀴디데스")
            .pubDate("2011-06-30")
            .description("투퀴디세스가 집필한 전쟁사")
            .isbn13("9788991290402")
            .cover("image")
            .categoryName("서양고대사")
            .publisher("도서출판 숲")
            .build();
        book.setReadingLog(readingLog);
        return bookRepository.save(book);
    }

    public BookSaveRequestDTO getBookSaveRequestDTO(String status) {
        return new BookSaveRequestDTO(
            "펠로폰네소스 전쟁사", "투퀴디데스", "2011-06-30", "투퀴디세스가 집필한 전쟁사", "9788991290402", "image",
            "서양고대사", "도서출판 숲", status);
    }

    public Book getBook() {
        return Book.builder()
            .title("펠로폰네소스 전쟁사")
            .author("투퀴디데스")
            .pubDate("2011-06-30")
            .description("투퀴디세스가 집필한 전쟁사")
            .isbn13("9788991290402")
            .cover("image")
            .categoryName("서양고대사")
            .publisher("도서출판 숲")
            .build();
    }

}
