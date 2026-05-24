package com.ask.repository;

import com.ask.entity.BulkMessageLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkMessageLogRepository extends JpaRepository<BulkMessageLog, Long> {
    Page<BulkMessageLog> findBySenderId(Long senderId, Pageable pageable);
}
