package com.gomdolbook.api.persistence.repository;

import com.gomdolbook.api.persistence.entity.ReadingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadingLogRepository extends JpaRepository<ReadingLog, Long> {
}
