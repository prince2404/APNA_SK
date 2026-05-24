package com.ask.repository;

import com.ask.entity.StockStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockStoreRepository extends JpaRepository<StockStore, Long> {
    Optional<StockStore> findByStoreIdAndProductIdAndBatchNumber(Long storeId, Long productId, String batchNumber);
    List<StockStore> findByStoreId(Long storeId);
    Page<StockStore> findByStoreId(Long storeId, Pageable pageable);
    Page<StockStore> findByStoreIdAndProductCategoryId(Long storeId, Long categoryId, Pageable pageable);
    
    Page<StockStore> findByStoreIdAndProductNameContainingIgnoreCase(Long storeId, String name, Pageable pageable);
    Page<StockStore> findByStoreIdAndProductNameContainingIgnoreCaseAndProductCategoryId(Long storeId, String name, Long categoryId, Pageable pageable);

    @Query("SELECT s FROM StockStore s WHERE s.expiryDate <= :date")
    List<StockStore> findExpiringStock(@Param("date") LocalDate date);

    @Query("SELECT s FROM StockStore s WHERE s.store.id = :storeId AND s.expiryDate <= :date")
    List<StockStore> findExpiringStockByStore(@Param("storeId") Long storeId, @Param("date") LocalDate date);

    @Query("SELECT s FROM StockStore s JOIN s.product p WHERE s.store.id = :storeId AND s.quantity < p.minStockThreshold")
    List<StockStore> findLowStockByStore(@Param("storeId") Long storeId);

    @Query("SELECT s FROM StockStore s JOIN s.product p WHERE s.quantity < p.minStockThreshold")
    List<StockStore> findAllLowStock();
}
