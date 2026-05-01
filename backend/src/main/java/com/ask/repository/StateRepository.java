package com.ask.repository;

import com.ask.entity.State;
import com.ask.enums.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for State entity operations.
 */
public interface StateRepository extends JpaRepository<State, Long> {

    Optional<State> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByNameIgnoreCase(String name);

    Page<State> findByIdAndStatus(Long id, EntityStatus status, Pageable pageable);

    Page<State> findByStatus(EntityStatus status, Pageable pageable);

    List<State> findByStatusOrderByNameAsc(EntityStatus status);
}
