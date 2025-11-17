package com.example.lab5.repository;

import com.example.lab5.entity.Function;
import com.example.lab5.entity.User;
import java.util.List;
import java.util.Optional;

public interface FunctionRepository {
    Function save(Function function);
    Optional<Function> findById(Long id);
    List<Function> findByUser(User user);
    List<Function> findByUserId(Long userId);
    List<Function> findAll();
    boolean update(Function function);
    boolean delete(Long id);
    List<Function> findByNameContaining(String name);
    int countByUser(User user);
}