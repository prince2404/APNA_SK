package com.ask.repository;

import com.ask.entity.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for UserPermission entity operations.
 */
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

    List<UserPermission> findByUserId(Long userId);

    boolean existsByUserIdAndPermissionId(Long userId, Long permissionId);

    void deleteByUserId(Long userId);

    /** Get all permissions for a user as module:action strings */
    @Query("SELECT CONCAT(up.permission.module, ':', up.permission.action) FROM UserPermission up WHERE up.user.id = :userId")
    List<String> findPermissionStringsByUserId(@Param("userId") Long userId);
}
