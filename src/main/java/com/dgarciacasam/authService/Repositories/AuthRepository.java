package com.dgarciacasam.authService.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dgarciacasam.authService.Models.User;

public interface AuthRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByUsernameAndPassword(String username, String password);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
}
