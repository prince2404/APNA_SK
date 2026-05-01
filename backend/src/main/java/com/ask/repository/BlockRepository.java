package com.ask.repository;

import com.ask.entity.Block;
import com.ask.enums.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Block entity operations.
 */
public interface BlockRepository extends JpaRepository<Block, Long> {

    Page<Block> findByDistrictIdAndStatus(Long districtId, EntityStatus status, Pageable pageable);

    Optional<Block> findByIdAndDistrictId(Long id, Long districtId);

    Page<Block> findByIdAndStatus(Long id, EntityStatus status, Pageable pageable);

    @Query("SELECT b FROM Block b JOIN b.district d WHERE b.id = :id AND d.state.id = :stateId")
    Optional<Block> findByIdAndStateId(@Param("id") Long id, @Param("stateId") Long stateId);

    List<Block> findByDistrictIdAndStatusOrderByNameAsc(Long districtId, EntityStatus status);

    boolean existsByNameIgnoreCaseAndDistrictId(String name, Long districtId);

    Page<Block> findByStatus(EntityStatus status, Pageable pageable);
}
