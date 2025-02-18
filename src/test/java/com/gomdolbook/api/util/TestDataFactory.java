package com.gomdolbook.api.util;

import com.gomdolbook.api.persistence.entity.Book;
import com.gomdolbook.api.persistence.entity.BookUserCollection;
import com.gomdolbook.api.persistence.entity.ReadingLog;
import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import com.gomdolbook.api.persistence.entity.User;
import com.gomdolbook.api.persistence.entity.User.Role;
import com.gomdolbook.api.persistence.entity.UserCollection;
import com.gomdolbook.api.persistence.repository.BookRepository;
import com.gomdolbook.api.persistence.repository.BookUserCollectionRepository;
import com.gomdolbook.api.persistence.repository.ReadingLogRepository;
import com.gomdolbook.api.persistence.repository.UserCollectionRepository;
import com.gomdolbook.api.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestDataFactory {

    @Autowired
    UserCollectionRepository userCollectionRepository;

    @Autowired
    ReadingLogRepository readingLogRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookUserCollectionRepository bookUserCollectionRepository;

    public ReadingLog createReadingLog(User user) {
        ReadingLog readingLog = new ReadingLog(Status.READING, "1", "2", "3");
        readingLog.setUser(user);
        return readingLogRepository.save(readingLog);
    }

    public User createUser(String email, String pic) {
        User user = new User(email, pic, Role.USER);
        return userRepository.save(user);
    }

    public UserCollection createUserCollection(String name, User user) {
        UserCollection userCollection = new UserCollection(name);
        userCollection.setUser(user);
        return userCollectionRepository.save(userCollection);
    }

    public void createBookUserCollection(Book book, UserCollection userCollection,
        User user) {
        BookUserCollection bookUserCollection = new BookUserCollection();
        bookUserCollection.setBook(book);
        bookUserCollection.setUserCollection(userCollection);
        bookUserCollection.setUser(user);
        bookUserCollectionRepository.save(bookUserCollection);
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
}
