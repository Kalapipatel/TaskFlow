package com.TaskFlow.ProjectService.security;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record UserPrincipal(UUID userId, String email, String username) implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Project role checks (OWNER/ADMIN/MEMBER/VIEWER) are evaluated
        // in service methods via project_members, not through Spring roles[cite: 2, 3]
        return List.of();
    }

    @Override
    public String getPassword() { return null; }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}