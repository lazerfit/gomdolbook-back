package com.gomdolbook.api.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
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

    public void setUser(User user) {
        if (this.user != null) {
            user.getBookUserCollections().remove(this);
        }
        this.user = user;
        user.getBookUserCollections().add(this);
    }

    public void setBook(Book book) {
        if (this.book != null) {
            book.getBookUserCollections().remove(this);
        }
        this.book = book;
        book.getBookUserCollections().add(this);
    }

    public void setUserCollection(UserCollection userCollection) {
        if (this.userCollection != null) {
            userCollection.getBookUserCollections().remove(this);
        }
        this.userCollection = userCollection;
        userCollection.getBookUserCollections().add(this);
    }
}
