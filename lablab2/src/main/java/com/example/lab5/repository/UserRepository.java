package com.example.lab5.repository;

import com.example.lab5.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    boolean update(User user);
    boolean delete(Long id);
    boolean existsByUsername(String username);
    List<User> findByUsernameContaining(String username);
    int count();
}