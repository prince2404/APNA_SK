package com.ask.repository;

import com.ask.entity.TransferOrder;
import com.ask.enums.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransferOrderRepository extends JpaRepository<TransferOrder, Long> {
    Page<TransferOrder> findByStoreId(Long storeId, Pageable pageable);
    Page<TransferOrder> findByStatus(TransferStatus status, Pageable pageable);
    Optional<TransferOrder> findByTransferNumber(String transferNumber);
}
