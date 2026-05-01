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

    boolean existsByCodeAndIdNot(String code, Long id);

    Optional<Store> findByIdAndBlockId(Long id, Long blockId);

    Page<Store> findByIdAndStatus(Long id, EntityStatus status, Pageable pageable);

    Page<Store> findByBlockIdAndStatus(Long blockId, EntityStatus status, Pageable pageable);

    List<Store> findByBlockIdAndStatusOrderByNameAsc(Long blockId, EntityStatus status);

    Page<Store> findByStatus(EntityStatus status, Pageable pageable);

    /** Find stores within a specific district (through block relationship) */
    @Query("SELECT s FROM Store s JOIN s.block b WHERE b.district.id = :districtId AND s.status = :status")
    Page<Store> findByDistrictIdAndStatus(@Param("districtId") Long districtId, @Param("status") EntityStatus status, Pageable pageable);

    @Query("SELECT s FROM Store s JOIN s.block b WHERE s.id = :id AND b.district.id = :districtId")
    Optional<Store> findByIdAndDistrictId(@Param("id") Long id, @Param("districtId") Long districtId);

    /** Find stores within a specific state (through block→district→state) */
    @Query("SELECT s FROM Store s JOIN s.block b JOIN b.district d WHERE d.state.id = :stateId AND s.status = :status")
    Page<Store> findByStateIdAndStatus(@Param("stateId") Long stateId, @Param("status") EntityStatus status, Pageable pageable);

    @Query("SELECT s FROM Store s JOIN s.block b JOIN b.district d WHERE s.id = :id AND d.state.id = :stateId")
    Optional<Store> findByIdAndStateId(@Param("id") Long id, @Param("stateId") Long stateId);
}
