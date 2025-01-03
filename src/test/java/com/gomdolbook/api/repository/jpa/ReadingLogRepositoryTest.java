package com.gomdolbook.api.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.gomdolbook.api.models.ReadingLog;
import com.gomdolbook.api.models.ReadingLog.Status;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest(excludeAutoConfiguration = {R2dbcAutoConfiguration.class})
@AutoConfigureDataJpa
@Sql(scripts = {"/schema-jpa.sql"})
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
        ReadingLog saved = readingLogRepository.save(
            ReadingLog.builder()
                .note1("1번 입니다.")
                .note2("2번 입니다.")
                .note3("3번 입니다.")
                .status(Status.READING)
                .build()
        );

        ReadingLog found = readingLogRepository.findById(saved.getId())
            .orElseThrow(() -> new RuntimeException("찾을 수 없습니다."));

        List<ReadingLog> all = readingLogRepository.findAll();

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(all).hasSize(1);
    }
}
