package com.ask.repository;

import com.ask.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for RefreshToken entity operations.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.isRevoked = true, r.revokedAt = :now WHERE r.user.id = :userId")
    int revokeByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /** Revoke expired tokens without deleting audit-relevant records. */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.isRevoked = true, r.revokedAt = :now WHERE r.expiresAt < :now AND r.isRevoked = false")
    int revokeExpiredTokens(@Param("now") LocalDateTime now);
}
