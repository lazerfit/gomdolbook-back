package com.gomdolbook.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gomdolbook.api.config.QueryDslConfig;
import com.gomdolbook.api.persistence.entity.ReadingLog;
import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import com.gomdolbook.api.persistence.entity.User;
import com.gomdolbook.api.persistence.repository.BookRepository;
import com.gomdolbook.api.persistence.repository.ReadingLogRepository;
import com.gomdolbook.api.util.TestDataFactory;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Import({QueryDslConfig.class, TestDataFactory.class})
@DataJpaTest
class ReadingLogRepositoryTest {

    static User user;

    @Autowired
    ReadingLogRepository readingLogRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        user = testDataFactory.createUser("redkafe@daum.net", "");
    }

    @AfterEach
    void tearDown() {
        readingLogRepository.deleteAll();
    }

    @Test
    void saveReadingLog() {
        ReadingLog saved = readingLogRepository.save(
            ReadingLog.of(user, Status.READING)
        );

        ReadingLog found = readingLogRepository.findById(saved.getId())
            .orElseThrow(() -> new RuntimeException("찾을 수 없습니다."));

        List<ReadingLog> all = readingLogRepository.findAll();

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(all).hasSize(1);
    }

    @Transactional
    @Test
    void saveRating() {
        ReadingLog saved = readingLogRepository.save(
            ReadingLog.of(user, Status.READING)
        );
        ReadingLog readingLog = readingLogRepository.findById(saved.getId())
            .orElseThrow(() -> new RuntimeException("찾을 수 없습니다."));
        readingLog.updateRating(5);
        assertThat(readingLog.getRating()).isEqualTo(5);
    }
}
