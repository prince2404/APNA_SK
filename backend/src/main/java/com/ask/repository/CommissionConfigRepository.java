package com.ask.repository;

import com.ask.entity.CommissionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CommissionConfigRepository extends JpaRepository<CommissionConfig, Long> {
    Optional<CommissionConfig> findByRoleId(Long roleId);
}
