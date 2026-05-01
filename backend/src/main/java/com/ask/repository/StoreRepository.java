package com.ask.repository;

import com.ask.entity.Store;
import com.ask.enums.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Store entity operations.
 */
public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByCode(String code);

    boolean existsByCode(String code);

    Page<Store> findByBlockIdAndStatus(Long blockId, EntityStatus status, Pageable pageable);

    List<Store> findByBlockIdAndStatusOrderByNameAsc(Long blockId, EntityStatus status);

    Page<Store> findByStatus(EntityStatus status, Pageable pageable);

    /** Find stores within a specific district (through block relationship) */
    @Query("SELECT s FROM Store s JOIN s.block b WHERE b.district.id = :districtId AND s.status = :status")
    Page<Store> findByDistrictIdAndStatus(@Param("districtId") Long districtId, @Param("status") EntityStatus status, Pageable pageable);

    /** Find stores within a specific state (through block→district→state) */
    @Query("SELECT s FROM Store s JOIN s.block b JOIN b.district d WHERE d.state.id = :stateId AND s.status = :status")
    Page<Store> findByStateIdAndStatus(@Param("stateId") Long stateId, @Param("status") EntityStatus status, Pageable pageable);
}
