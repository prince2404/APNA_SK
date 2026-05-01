package com.ask.repository;

import com.ask.entity.PermissionRequest;
import com.ask.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for PermissionRequest entity operations.
 */
public interface PermissionRequestRepository extends JpaRepository<PermissionRequest, Long> {

    Page<PermissionRequest> findByUserId(Long userId, Pageable pageable);

    Page<PermissionRequest> findByStatus(RequestStatus status, Pageable pageable);

    boolean existsByUserIdAndPermissionIdAndStatus(Long userId, Long permissionId, RequestStatus status);
}
