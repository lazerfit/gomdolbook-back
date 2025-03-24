package com.gomdolbook.api.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class BookUserCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOK_USER_COLLECTION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOK_ID")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_COLLECTION_ID")
    private UserCollection userCollection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    public static BookUserCollection of(User user, UserCollection collection, Book book) {
        BookUserCollection buc = new BookUserCollection();
        buc.setUser(user);
        buc.setUserCollection(collection);
        buc.setBook(book);
        return buc;
    }

    public void setUser(User user) {
        if (this.user != null) {
            user.getBookUserCollections().remove(this);
        }
        this.user = user;
        if (user != null && !user.getBookUserCollections().contains(this)) {
            user.getBookUserCollections().add(this);
        }
    }

    public void setBook(Book book) {
        if (this.book != null) {
            book.getBookUserCollections().remove(this);
        }
        this.book = book;
        if (book != null && !book.getBookUserCollections().contains(this)) {
            book.getBookUserCollections().add(this);
        }
    }

    public void setUserCollection(UserCollection userCollection) {
        if (this.userCollection != null) {
            userCollection.getBookUserCollections().remove(this);
        }
        this.userCollection = userCollection;
        if (userCollection != null && !userCollection.getBookUserCollections().contains(this)) {
            userCollection.getBookUserCollections().add(this);
        }
    }
}
