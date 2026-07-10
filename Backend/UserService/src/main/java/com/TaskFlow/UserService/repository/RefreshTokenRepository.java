package com.TaskFlow.UserService.repository;

import com.TaskFlow.UserService.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    // Used during the refresh token flow to validate the incoming hashed token
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // Used to log a user out of all devices (e.g., upon password change)
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);

    // Used by a scheduled batch job to clear out naturally expired tokens
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") Instant now);
}