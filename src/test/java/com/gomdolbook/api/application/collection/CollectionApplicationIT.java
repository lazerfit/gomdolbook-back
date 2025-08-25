package com.gomdolbook.api.application.collection;

import static org.assertj.core.api.Assertions.assertThat;

import com.gomdolbook.api.application.collection.dto.CollectionDetailDTO;
import com.gomdolbook.api.application.collection.dto.CollectionsDTO;
import com.gomdolbook.api.config.WithMockCustomUser;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.User.Role;
import com.gomdolbook.api.domain.models.user.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@WithMockCustomUser
@Transactional
@SpringBootTest
class CollectionApplicationIT {

    @Autowired
    CollectionApplicationService collectionApplicationService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    private Long collectionId;

    @BeforeEach
    void setUp() {
        String email = "test@email.com";
        userRepository.save(new User(email, "pic", Role.USER));
        collectionApplicationService.createCollection("name");

        em.flush();
        em.clear();

        List<CollectionsDTO> collections = collectionApplicationService.getCollections();
        collectionId = collections.getFirst().id();
    }

    @Test
    void createCollection() {
        collectionApplicationService.createCollection("name1");
        em.flush();
        em.clear();

        List<CollectionsDTO> collections = collectionApplicationService.getCollections();

        assertThat(collections).hasSize(2);
    }

    @Test
    void changeName() {
        collectionApplicationService.changeCollectionName(collectionId, "name2");
        em.flush();
        em.clear();

        CollectionDetailDTO collection = collectionApplicationService.getCollection(collectionId);

        assertThat(collection.collectionName()).isEqualTo("name2");
    }

    @Test
    void deleteCollection() {
        collectionApplicationService.deleteCollection(collectionId);
        em.flush();
        em.clear();

        List<CollectionsDTO> collections = collectionApplicationService.getCollections();

        assertThat(collections).isEmpty();
    }
}
