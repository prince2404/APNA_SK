package com.ask.repository;

import com.ask.entity.PermissionRequest;
import com.ask.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for PermissionRequest operations.
 */
@Repository
public interface PermissionRequestRepository extends JpaRepository<PermissionRequest, Long>, JpaSpecificationExecutor<PermissionRequest> {

    Page<PermissionRequest> findByUserId(Long userId, Pageable pageable);

    Page<PermissionRequest> findByUserIdAndStatus(Long userId, RequestStatus status, Pageable pageable);

    Page<PermissionRequest> findByStatus(RequestStatus status, Pageable pageable);

    boolean existsByUserIdAndPermissionIdAndStatus(Long userId, Long permissionId, RequestStatus status);
}
