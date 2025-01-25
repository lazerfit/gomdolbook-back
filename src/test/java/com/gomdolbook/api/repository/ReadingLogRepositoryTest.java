package com.gomdolbook.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gomdolbook.api.persistence.entity.ReadingLog;
import com.gomdolbook.api.persistence.entity.ReadingLog.Status;
import com.gomdolbook.api.persistence.entity.User;
import com.gomdolbook.api.persistence.entity.User.Role;
import com.gomdolbook.api.persistence.repository.BookRepository;
import com.gomdolbook.api.persistence.repository.ReadingLogRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ReadingLogRepositoryTest {

    @Autowired
    ReadingLogRepository readingLogRepository;

    @Autowired
    BookRepository bookRepository;

    @AfterEach
    void tearDown() {
        readingLogRepository.deleteAll();
    }

    @Test
    void saveReadingLog() {
        User user = new User("user", "img", Role.USER);
        ReadingLog saved = readingLogRepository.save(
            new ReadingLog(Status.READING,"1","2","3")
        );

        ReadingLog found = readingLogRepository.findById(saved.getId())
            .orElseThrow(() -> new RuntimeException("찾을 수 없습니다."));

        List<ReadingLog> all = readingLogRepository.findAll();

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(all).hasSize(1);
    }
}
