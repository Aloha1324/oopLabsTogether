package com.example.lab5.repository;

import com.example.lab5.entity.Function;
import com.example.lab5.entity.Point;
import java.util.List;
import java.util.Optional;

public interface PointRepository {
    Point save(Point point);
    Optional<Point> findById(Long id);
    List<Point> findByFunction(Function function);
    List<Point> findByFunctionId(Long functionId);
    List<Point> findAll();
    boolean update(Point point);
    boolean delete(Long id);
    boolean deleteByFunction(Function function);
    List<Point> findByXValueBetween(Double minX, Double maxX);
    List<Point> findByYValueGreaterThan(Double yValue);
    int countByFunction(Function function);
}