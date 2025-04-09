package com.gomdolbook.api.domain.models.bookCollection;

import com.gomdolbook.api.domain.models.book.Book;
import com.gomdolbook.api.domain.models.collection.Collection;
import com.gomdolbook.api.domain.models.user.User;
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
public class BookCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOK_COLLECTION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOK_ID")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COLLECTION_ID")
    private Collection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    public static BookCollection of(User user, Collection collection, Book book) {
        BookCollection buc = new BookCollection();
        buc.setUser(user);
        buc.setCollection(collection);
        buc.setBook(book);
        return buc;
    }

    public void setUser(User user) {
        if (this.user != null) {
            user.getBookCollections().remove(this);
        }
        this.user = user;
        if (user != null && !user.getBookCollections().contains(this)) {
            user.getBookCollections().add(this);
        }
    }

    public void setBook(Book book) {
        if (this.book != null) {
            book.getBookCollections().remove(this);
        }
        this.book = book;
        if (book != null && !book.getBookCollections().contains(this)) {
            book.getBookCollections().add(this);
        }
    }

    public void setCollection(Collection collection) {
        if (this.collection != null) {
            collection.getBookCollections().remove(this);
        }
        this.collection = collection;
        if (collection != null && !collection.getBookCollections().contains(this)) {
            collection.getBookCollections().add(this);
        }
    }
}
