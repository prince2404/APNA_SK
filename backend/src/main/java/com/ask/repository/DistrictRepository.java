package com.ask.repository;

import com.ask.entity.District;
import com.ask.enums.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for District entity operations.
 */
public interface DistrictRepository extends JpaRepository<District, Long> {

    Page<District> findByStateIdAndStatus(Long stateId, EntityStatus status, Pageable pageable);

    List<District> findByStateIdAndStatusOrderByNameAsc(Long stateId, EntityStatus status);

    boolean existsByNameIgnoreCaseAndStateId(String name, Long stateId);

    Page<District> findByStatus(EntityStatus status, Pageable pageable);
}
