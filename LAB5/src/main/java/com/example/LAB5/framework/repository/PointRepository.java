package com.example.LAB5.framework.repository;

import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {

    List<Point> findByFunction(Function function);

    List<Point> findByFunctionId(Long functionId);

    List<Point> findByXValueBetween(Double minX, Double maxX);

    List<Point> findByYValueGreaterThan(Double yValue);

    @Query("SELECT COUNT(p) FROM Point p WHERE p.function = :function")
    int countByFunction(@Param("function") Function function);

    @Query("SELECT p FROM Point p WHERE p.function.id = :functionId AND p.xValue BETWEEN :minX AND :maxX")
    List<Point> findByFunctionIdAndXValueBetween(@Param("functionId") Long functionId,
                                                 @Param("minX") Double minX,
                                                 @Param("maxX") Double maxX);

    @Transactional
    @Modifying
    @Query("DELETE FROM Point p WHERE p.function = :function")
    void deleteByFunction(@Param("function") Function function);

    @Query("SELECT p FROM Point p WHERE p.yValue < :yValue")
    List<Point> findByYValueLessThan(@Param("yValue") Double yValue);
}