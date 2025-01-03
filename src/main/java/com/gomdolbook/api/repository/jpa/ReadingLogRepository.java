package com.gomdolbook.api.repository.jpa;

import com.gomdolbook.api.models.ReadingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadingLogRepository extends JpaRepository<ReadingLog, Long> {
}
