package com.example.LAB5.framework.repository;

import com.example.LAB5.framework.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByUsernameContaining(String username);

    @Query("SELECT COUNT(u) FROM User u")
    int countUsers();

    @Query("SELECT u FROM User u WHERE u.username LIKE %:pattern%")
    List<User> findUsersByUsernamePattern(@Param("pattern") String pattern);
}