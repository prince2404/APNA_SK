package com.ask.repository;

import com.ask.entity.Block;
import com.ask.enums.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Block entity operations.
 */
public interface BlockRepository extends JpaRepository<Block, Long> {

    Page<Block> findByDistrictIdAndStatus(Long districtId, EntityStatus status, Pageable pageable);

    List<Block> findByDistrictIdAndStatusOrderByNameAsc(Long districtId, EntityStatus status);

    boolean existsByNameIgnoreCaseAndDistrictId(String name, Long districtId);

    Page<Block> findByStatus(EntityStatus status, Pageable pageable);
}
