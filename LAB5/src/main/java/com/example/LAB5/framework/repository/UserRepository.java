package com.example.LAB5.framework.repository;

import com.example.LAB5.framework.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByUsernameContaining(String username);

    // ДОБАВЛЕН МЕТОД ДЛЯ ПОИСКА ПО ЧАСТИЧНОМУ СОВПАДЕНИЮ ИГНОРИРУЯ РЕГИСТР
    List<User> findByUsernameContainingIgnoreCase(String username);

    @Query("SELECT COUNT(u) FROM User u")
    int countUsers();

    @Query("SELECT u FROM User u WHERE u.username LIKE %:pattern%")
    List<User> findUsersByUsernamePattern(@Param("pattern") String pattern);

    // ДОБАВЛЕННЫЕ МЕТОДЫ ДЛЯ ТЕСТОВ
    List<User> findByUsernameStartingWith(String prefix);

    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.username LIKE :prefix%")
    void deleteByUsernameStartingWith(@Param("prefix") String prefix);
}