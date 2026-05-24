package com.ask.repository;

import com.ask.entity.StockRequest;
import com.ask.enums.StockRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRequestRepository extends JpaRepository<StockRequest, Long> {
    Page<StockRequest> findByStoreId(Long storeId, Pageable pageable);
    Page<StockRequest> findByStatus(StockRequestStatus status, Pageable pageable);
    Page<StockRequest> findByStoreIdAndStatus(Long storeId, StockRequestStatus status, Pageable pageable);
}
