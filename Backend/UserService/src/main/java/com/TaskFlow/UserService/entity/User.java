package com.TaskFlow.UserService.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR) // Forces Hibernate to use CHAR(36) in MySQL instead of BINARY
    @Column(length = 36)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "profile_picture_url", columnDefinition = "TEXT")
    private String profilePictureUrl;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true; // Primitive boolean for NOT NULL columns

    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified = false;

    @CreationTimestamp // Automatically managed by Hibernate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt; // Instant ensures UTC time universally

    @UpdateTimestamp // Automatically managed by Hibernate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }
}