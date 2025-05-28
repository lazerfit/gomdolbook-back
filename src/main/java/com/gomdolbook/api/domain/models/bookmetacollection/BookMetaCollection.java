package com.gomdolbook.api.domain.models.bookmetacollection;

import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class BookMetaCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOK_META_COLLECTION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOK_META_ID")
    private BookMeta bookMeta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COLLECTION_ID")
    private Collection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    public static BookMetaCollection of(BookMeta bookMeta, Collection collection, User user) {
        BookMetaCollection metaCollection = new BookMetaCollection();
        metaCollection.setBookMeta(bookMeta);
        metaCollection.setCollection(collection);
        metaCollection.setUser(user);
        return metaCollection;
    }

    public void setUser(User user) {
        if (this.user != null) {
            user.getBookMetaCollections().remove(this);
        }
        this.user = user;
        if (user != null && !user.getBookMetaCollections().contains(this)) {
            user.getBookMetaCollections().add(this);
        }
    }

    public void setBookMeta(BookMeta bookMeta) {
        if (this.bookMeta != null) {
            bookMeta.getBookMetaCollections().remove(this);
        }
        this.bookMeta = bookMeta;
        if (bookMeta != null && !bookMeta.getBookMetaCollections().contains(this)) {
            bookMeta.getBookMetaCollections().add(this);
        }
    }

    public void setCollection(Collection collection) {
        if (this.collection != null) {
            collection.getBookMetaCollections().remove(this);
        }
        this.collection = collection;
        if (collection != null && !collection.getBookMetaCollections().contains(this)) {
            collection.getBookMetaCollections().add(this);
        }
    }
}
