package com.ppks.fernet.backend.dao;

import com.ppks.fernet.backend.domain.User;
import com.ppks.fernet.backend.domain.enumeration.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    List<User> findAllByRole(Role role);
}
