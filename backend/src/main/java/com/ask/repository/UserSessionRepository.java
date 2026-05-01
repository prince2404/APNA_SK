package com.ask.repository;

import com.ask.entity.UserSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserSession entity operations.
 */
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    List<UserSession> findByUserIdAndIsRevokedFalseOrderByLastActiveAtDesc(Long userId);

    Page<UserSession> findByUserIdAndIsRevokedFalse(Long userId, Pageable pageable);

    Optional<UserSession> findByIdAndUserId(Long id, Long userId);

    Optional<UserSession> findByTokenFingerprint(String tokenFingerprint);

    @Modifying
    @Query("UPDATE UserSession s SET s.isRevoked = true WHERE s.user.id = :userId AND s.isRevoked = false")
    int revokeByUserId(@Param("userId") Long userId);
}
