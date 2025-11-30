package com.example.LAB5.framework.repository;

import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.Point;
import com.example.LAB5.framework.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {

    // Существующие методы
    @Query("SELECT p FROM Point p WHERE p.function = :function")
    List<Point> findByFunction(@Param("function") Function function);

    @Query("SELECT p FROM Point p WHERE p.function.id = :functionId")
    List<Point> findByFunctionId(@Param("functionId") Long functionId);

    @Query("SELECT p FROM Point p WHERE p.xValue BETWEEN :minX AND :maxX")
    List<Point> findByXValueBetween(@Param("minX") Double minX, @Param("maxX") Double maxX);

    @Query("SELECT p FROM Point p WHERE p.yValue > :yValue")
    List<Point> findByYValueGreaterThan(@Param("yValue") Double yValue);

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

    // НОВЫЙ МЕТОД: Удаление точек по ID функции
    @Transactional
    @Modifying
    @Query("DELETE FROM Point p WHERE p.function.id = :functionId")
    void deleteByFunctionId(@Param("functionId") Long functionId);

    @Query("SELECT p FROM Point p WHERE p.yValue < :yValue")
    List<Point> findByYValueLessThan(@Param("yValue") Double yValue);

    @Transactional
    @Modifying
    @Query("DELETE FROM Point p WHERE p.function.id IN (SELECT f.id FROM Function f WHERE f.name LIKE :prefix%)")
    void deleteByFunctionNameStartingWith(@Param("prefix") String prefix);

    // НОВЫЕ МЕТОДЫ ДЛЯ ИЗОЛИРОВАННЫХ ТЕСТОВ:

    // Поиск точек по ID функции и Y > значения
    @Query("SELECT p FROM Point p WHERE p.function.id = :functionId AND p.yValue > :yValue")
    List<Point> findByFunctionIdAndYValueGreaterThan(@Param("functionId") Long functionId,
                                                     @Param("yValue") Double yValue);

    // Поиск точек по ID функции и Y < значения
    @Query("SELECT p FROM Point p WHERE p.function.id = :functionId AND p.yValue < :yValue")
    List<Point> findByFunctionIdAndYValueLessThan(@Param("functionId") Long functionId,
                                                  @Param("yValue") Double yValue);

    // Поиск точек по ID функции и диапазону Y
    @Query("SELECT p FROM Point p WHERE p.function.id = :functionId AND p.yValue BETWEEN :minY AND :maxY")
    List<Point> findByFunctionIdAndYValueBetween(@Param("functionId") Long functionId,
                                                 @Param("minY") Double minY,
                                                 @Param("maxY") Double maxY);

    // Поиск точек по ID пользователя и диапазону X
    @Query("SELECT p FROM Point p WHERE p.user.id = :userId AND p.xValue BETWEEN :minX AND :maxX")
    List<Point> findByUserIdAndXValueBetween(@Param("userId") Long userId,
                                             @Param("minX") Double minX,
                                             @Param("maxX") Double maxX);

    // Поиск точек по ID пользователя и Y > значения
    @Query("SELECT p FROM Point p WHERE p.user.id = :userId AND p.yValue > :yValue")
    List<Point> findByUserIdAndYValueGreaterThan(@Param("userId") Long userId,
                                                 @Param("yValue") Double yValue);

    // Поиск точек по ID пользователя и Y < значения
    @Query("SELECT p FROM Point p WHERE p.user.id = :userId AND p.yValue < :yValue")
    List<Point> findByUserIdAndYValueLessThan(@Param("userId") Long userId,
                                              @Param("yValue") Double yValue);

    // ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ:

    // Поиск точек по пользователю
    @Query("SELECT p FROM Point p WHERE p.user = :user")
    List<Point> findByUser(@Param("user") User user);

    // Поиск точек по ID пользователя
    @Query("SELECT p FROM Point p WHERE p.user.id = :userId")
    List<Point> findByUserId(@Param("userId") Long userId);

    // Поиск точек по функции и конкретному значению X
    @Query("SELECT p FROM Point p WHERE p.function = :function AND p.xValue = :xValue")
    List<Point> findByFunctionAndXValue(@Param("function") Function function,
                                        @Param("xValue") Double xValue);

    // Поиск точек по диапазону Y
    @Query("SELECT p FROM Point p WHERE p.yValue BETWEEN :minY AND :maxY")
    List<Point> findByYValueBetween(@Param("minY") Double minY, @Param("maxY") Double maxY);

    // Подсчет точек по пользователю
    @Query("SELECT COUNT(p) FROM Point p WHERE p.user = :user")
    Long countByUser(@Param("user") User user);

    // Удаление точек по пользователю
    @Transactional
    @Modifying
    @Query("DELETE FROM Point p WHERE p.user = :user")
    void deleteByUser(@Param("user") User user);

    // Поиск точек с сортировкой по X (возрастание)
    @Query("SELECT p FROM Point p WHERE p.function = :function ORDER BY p.xValue ASC")
    List<Point> findByFunctionOrderByXValueAsc(@Param("function") Function function);

    // Поиск точек с сортировкой по Y (убывание)
    @Query("SELECT p FROM Point p WHERE p.function = :function ORDER BY p.yValue DESC")
    List<Point> findByFunctionOrderByYValueDesc(@Param("function") Function function);

    // Поиск уникальных значений X для функции
    @Query("SELECT DISTINCT p.xValue FROM Point p WHERE p.function = :function")
    List<Double> findDistinctXValuesByFunction(@Param("function") Function function);

    // Статистика по точкам функции (min, max, avg X и Y) - ИЗМЕНЕНО НА НАТИВНЫЙ SQL
    @Query(value = "SELECT MIN(p.x_value), MAX(p.x_value), AVG(p.x_value), " +
            "MIN(p.y_value), MAX(p.y_value), AVG(p.y_value) " +
            "FROM points p WHERE p.function_id = :functionId", nativeQuery = true)
    Object[] getFunctionStatistics(@Param("functionId") Long functionId);

    // Поиск точек по функции и диапазону Y
    @Query("SELECT p FROM Point p WHERE p.function = :function AND p.yValue BETWEEN :minY AND :maxY")
    List<Point> findByFunctionAndYValueBetween(@Param("function") Function function,
                                               @Param("minY") Double minY,
                                               @Param("maxY") Double maxY);

    // Поиск точек с отрицательными значениями X
    @Query("SELECT p FROM Point p WHERE p.xValue < 0")
    List<Point> findPointsWithNegativeX();

    // Поиск точек с положительными значениями Y
    @Query("SELECT p FROM Point p WHERE p.yValue > 0")
    List<Point> findPointsWithPositiveY();

    // Поиск точек по нескольким значениям X
    @Query("SELECT p FROM Point p WHERE p.xValue IN :xValues")
    List<Point> findByXValueIn(@Param("xValues") List<Double> xValues);

    // Поиск точек с значением Y равным нулю
    @Query("SELECT p FROM Point p WHERE p.yValue = 0")
    List<Point> findPointsWithYZero();

    // Поиск максимального значения X для функции
    @Query("SELECT MAX(p.xValue) FROM Point p WHERE p.function = :function")
    Double findMaxXValueByFunction(@Param("function") Function function);

    // Поиск минимального значения Y для функции
    @Query("SELECT MIN(p.yValue) FROM Point p WHERE p.function = :function")
    Double findMinYValueByFunction(@Param("function") Function function);

    // Поиск точек с одинаковыми значениями X и Y
    @Query("SELECT p FROM Point p WHERE p.xValue = p.yValue")
    List<Point> findPointsWhereXEqualsY();

    // Поиск точек с сортировкой по X и Y
    @Query("SELECT p FROM Point p WHERE p.function = :function ORDER BY p.xValue ASC, p.yValue DESC")
    List<Point> findByFunctionOrderByXValueAscYValueDesc(@Param("function") Function function);

    // Подсчет уникальных значений X для пользователя
    @Query("SELECT COUNT(DISTINCT p.xValue) FROM Point p WHERE p.user = :user")
    Long countDistinctXValuesByUser(@Param("user") User user);

    // Поиск точек по пользователю и диапазону X
    @Query("SELECT p FROM Point p WHERE p.user = :user AND p.xValue BETWEEN :minX AND :maxX")
    List<Point> findByUserAndXValueBetween(@Param("user") User user,
                                           @Param("minX") Double minX,
                                           @Param("maxX") Double maxX);

    // Удаление точек по ID пользователя
    @Transactional
    @Modifying
    @Query("DELETE FROM Point p WHERE p.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    // Проверка существования точек для функции
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Point p WHERE p.function = :function")
    Boolean existsByFunction(@Param("function") Function function);

    // Поиск точек с использованием LIKE для имени функции (частичное совпадение)
    @Query("SELECT p FROM Point p WHERE p.function.name LIKE %:functionNamePart%")
    List<Point> findByFunctionNameContaining(@Param("functionNamePart") String functionNamePart);

    // Группировка точек по функции с подсчетом
    @Query("SELECT p.function, COUNT(p) FROM Point p GROUP BY p.function")
    List<Object[]> countPointsByFunction();

    @Query("SELECT p.function.id, COUNT(p) FROM Point p GROUP BY p.function.id")
    List<Object[]> getPointCountByFunction();

    // Поиск точек созданных после определенного ID
    @Query("SELECT p FROM Point p WHERE p.id > :minId")
    List<Point> findPointsWithIdGreaterThan(@Param("minId") Long minId);
}