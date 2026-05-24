package com.ask.repository;

import com.ask.entity.TransferOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferOrderItemRepository extends JpaRepository<TransferOrderItem, Long> {
    List<TransferOrderItem> findByTransferOrderId(Long transferOrderId);
}
