package com.example.LAB5.framework.repository;

import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FunctionRepository extends JpaRepository<Function, Long> {

    List<Function> findByUser(User user);

    List<Function> findByUserId(Long userId);

    List<Function> findByNameContaining(String name);

    @Query("SELECT COUNT(f) FROM Function f WHERE f.user = :userr")
    int countByUser(@Param("user") User user);

    @Query("SELECT f FROM Function f WHERE f.expression LIKE %:expressionPart%")
    List<Function> findByExpressionContaining(@Param("expressionPart") String expressionPart);

    @Query("SELECT f FROM Function f WHERE f.user.id = :userId AND f.name LIKE %:name%")
    List<Function> findByUserIdAndNameContaining(@Param("userId") Long userId,
                                                 @Param("name") String name);
}