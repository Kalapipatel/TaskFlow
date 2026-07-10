package com.TaskFlow.UserService.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_rt_user_id", columnList = "user_id"),
                @Index(name = "idx_rt_expires_at", columnList = "expires_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR) // Safe MySQL UUID mapping
    @Column(length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // Enforces DB-level cascade delete
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt; // Changed to Instant for timezone safety

    @Column(name = "is_revoked", nullable = false)
    private boolean isRevoked = false; // Changed to primitive

    @CreationTimestamp // Delegated to Hibernate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt; // Changed to Instant
}