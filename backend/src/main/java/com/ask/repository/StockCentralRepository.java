package com.ask.repository;

import com.ask.entity.StockCentral;
import com.ask.enums.StockCentralStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockCentralRepository extends JpaRepository<StockCentral, Long> {
    List<StockCentral> findByProductIdAndStatus(Long productId, StockCentralStatus status);
    Optional<StockCentral> findByProductIdAndBatchNumberAndStatus(Long productId, String batchNumber, StockCentralStatus status);
    
    @Query("SELECT s FROM StockCentral s WHERE s.status = :status AND s.expiryDate <= :date")
    List<StockCentral> findExpiringStock(@Param("date") LocalDate date, @Param("status") StockCentralStatus status);
}
