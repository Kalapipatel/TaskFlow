package com.TaskFlow.UserService.repository;

import com.TaskFlow.UserService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Used during registration to check for duplicates
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    // Used during login to load the user
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
}
