package com.ask.repository;

import com.ask.entity.District;
import com.ask.enums.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for District entity operations.
 */
public interface DistrictRepository extends JpaRepository<District, Long> {

    Page<District> findByStateIdAndStatus(Long stateId, EntityStatus status, Pageable pageable);

    Optional<District> findByIdAndStateId(Long id, Long stateId);

    Page<District> findByIdAndStatus(Long id, EntityStatus status, Pageable pageable);

    List<District> findByStateIdAndStatusOrderByNameAsc(Long stateId, EntityStatus status);

    boolean existsByNameIgnoreCaseAndStateId(String name, Long stateId);

    boolean existsByNameIgnoreCaseAndStateIdAndIdNot(String name, Long stateId, Long id);

    Page<District> findByStatus(EntityStatus status, Pageable pageable);
}
