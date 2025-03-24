package com.gomdolbook.api.persistence.entity;

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
public class UserCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_COLLECTION_ID")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "userCollection", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<BookUserCollection> bookUserCollections = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    private UserCollection(String name) {
        this.name = name;
    }

    public static UserCollection of(User user, String name) {
        UserCollection c = new UserCollection(name);
        c.setUser(user);
        return c;
    }

    public void setUser(User user) {
        if (this.user != null) {
            this.user.getUserCollections().remove(this);
        }
        this.user = user;
        if(user != null && !user.getUserCollections().contains(this)) {
            user.getUserCollections().add(this);
        }
    }
}
