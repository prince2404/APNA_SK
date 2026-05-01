package com.ask.repository;

import com.ask.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Permission entity operations.
 */
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByModuleAndAction(String module, String action);

    List<Permission> findByModule(String module);
}
