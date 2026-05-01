package com.ask.repository;

import com.ask.entity.TwoFactorConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for TwoFactorConfig entity operations.
 */
public interface TwoFactorConfigRepository extends JpaRepository<TwoFactorConfig, Long> {

    Optional<TwoFactorConfig> findByUserId(Long userId);
}
