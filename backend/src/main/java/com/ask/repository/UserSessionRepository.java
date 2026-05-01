package com.ask.repository;

import com.ask.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserSession entity operations.
 */
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    List<UserSession> findByUserIdAndIsRevokedFalseOrderByLastActiveAtDesc(Long userId);

    Optional<UserSession> findByTokenFingerprint(String tokenFingerprint);

    void deleteByUserId(Long userId);
}
