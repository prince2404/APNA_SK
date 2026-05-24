package com.ask.repository;

import com.ask.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long>, JpaSpecificationExecutor<Bill> {
    Optional<Bill> findByBillNumber(String billNumber);
    Page<Bill> findByStoreId(Long storeId, Pageable pageable);
    
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.store.id = :storeId AND b.createdAt >= :startOfDay")
    long countByStoreIdAndCreatedAtAfter(@Param("storeId") Long storeId, @Param("startOfDay") LocalDateTime startOfDay);
}
