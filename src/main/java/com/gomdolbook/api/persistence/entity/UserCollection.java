package com.gomdolbook.api.persistence.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class UserCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USERCOLLECTION_ID")
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "userCollection", fetch = FetchType.LAZY)
    private final List<BookUserCollection> bookUserCollections = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    public UserCollection(String name) {
        this.name = name;
    }

    public void setUser(User user) {
        if (this.user != null) {
            this.user.getUserCollections().remove(this);
        }
        this.user = user;
        if(!user.getUserCollections().contains(this)) {
            user.getUserCollections().add(this);
        }
    }
}
