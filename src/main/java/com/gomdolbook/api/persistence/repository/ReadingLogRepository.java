package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.persistence.entity.ReadingLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingLogRepository extends JpaRepository<ReadingLog, Long> {
}
