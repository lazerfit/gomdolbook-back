package com.gomdolbook.api.domain.models.collection;

import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.bookcollection.BookCollection;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COLLECTION_ID")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "collection", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<BookCollection> bookCollections = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    private Collection(String name) {
        this.name = name;
    }

    public static Collection of(User user, String name) {
        Collection c = new Collection(name);
        c.setUser(user);
        return c;
    }

    public void setUser(User user) {
        if (this.user != null) {
            this.user.getCollections().remove(this);
        }
        this.user = user;
        if(user != null && !user.getCollections().contains(this)) {
            user.getCollections().add(this);
        }
    }
}
