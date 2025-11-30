package com.example.LAB5.framework.repository;

import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface FunctionRepository extends JpaRepository<Function, Long> {

    // Существующие методы
    List<Function> findByUser(User user);

    List<Function> findByUserId(Long userId);

    List<Function> findByNameContaining(String name);

    // ADD THIS MISSING METHOD - Case insensitive search
    List<Function> findByNameContainingIgnoreCase(String name);

    List<Function> findByExpressionContaining(String expression);

    List<Function> findByUserIdAndNameContaining(Long userId, String name);

    // ADD THIS MISSING METHOD - Case insensitive version
    List<Function> findByUserIdAndNameContainingIgnoreCase(Long userId, String name);

    int countByUser(User user);

    // STREAMING МЕТОДЫ ДЛЯ ОПТИМИЗАЦИИ ПРОИЗВОДИТЕЛЬНОСТИ
    @Query("SELECT f FROM Function f")
    Stream<Function> streamAll();

    @Query("SELECT f FROM Function f ORDER BY f.id")
    Stream<Function> streamAllOrderedById();

    @Query("SELECT f FROM Function f WHERE f.user.id = :userId")
    Stream<Function> streamByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM Function f WHERE f.id IS NOT NULL")
    Stream<Function> streamAllBasic();

    @Query("SELECT f FROM Function f WHERE f.user = :user")
    Stream<Function> streamByUser(@Param("user") User user);

    @Query("SELECT f FROM Function f WHERE f.name LIKE %:pattern%")
    Stream<Function> streamByNameContaining(@Param("pattern") String pattern);

    // ADD CASE INSENSITIVE STREAMING METHOD
    @Query("SELECT f FROM Function f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :pattern, '%'))")
    Stream<Function> streamByNameContainingIgnoreCase(@Param("pattern") String pattern);

    @Query("SELECT f FROM Function f WHERE f.expression LIKE %:pattern%")
    Stream<Function> streamByExpressionContaining(@Param("pattern") String pattern);

    // ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ:

    // Поиск по точному совпадению имени
    @Query("SELECT f FROM Function f WHERE f.name = :name")
    Optional<Function> findByName(@Param("name") String name);

    // ADD CASE INSENSITIVE EXACT MATCH
    @Query("SELECT f FROM Function f WHERE LOWER(f.name) = LOWER(:name)")
    Optional<Function> findByNameIgnoreCase(@Param("name") String name);

    // Поиск функций по диапазону ID
    @Query("SELECT f FROM Function f WHERE f.id BETWEEN :startId AND :endId")
    List<Function> findByIdBetween(@Param("startId") Long startId, @Param("endId") Long endId);

    // Поиск функций с сортировкой по имени (возрастание)
    @Query("SELECT f FROM Function f WHERE f.user = :user ORDER BY f.name ASC")
    List<Function> findByUserOrderByNameAsc(@Param("user") User user);

    // Поиск функций с сортировкой по имени (убывание)
    @Query("SELECT f FROM Function f WHERE f.user = :user ORDER BY f.name DESC")
    List<Function> findByUserOrderByNameDesc(@Param("user") User user);

    // Поиск функций с сортировкой по дате создания (возрастание)
    @Query("SELECT f FROM Function f WHERE f.user = :user ORDER BY f.createdAt ASC")
    List<Function> findByUserOrderByCreatedAtAsc(@Param("user") User user);

    // Поиск функций с сортировкой по дате создания (убывание)
    @Query("SELECT f FROM Function f WHERE f.user = :user ORDER BY f.createdAt DESC")
    List<Function> findByUserOrderByCreatedAtDesc(@Param("user") User user);

    // Проверка существования функции по имени и пользователю
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Function f WHERE f.name = :name AND f.user = :user")
    Boolean existsByNameAndUser(@Param("name") String name, @Param("user") User user);

    // ADD CASE INSENSITIVE VERSION
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Function f WHERE LOWER(f.name) = LOWER(:name) AND f.user = :user")
    Boolean existsByNameIgnoreCaseAndUser(@Param("name") String name, @Param("user") User user);

    // Проверка существования функции по выражению и пользователю
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Function f WHERE f.expression = :expression AND f.user = :user")
    Boolean existsByExpressionAndUser(@Param("expression") String expression, @Param("user") User user);

    // Удаление функций по пользователю
    @Transactional
    @Modifying
    @Query("DELETE FROM Function f WHERE f.user = :user")
    void deleteByUser(@Param("user") User user);

    // Удаление функций по ID пользователя
    @Transactional
    @Modifying
    @Query("DELETE FROM Function f WHERE f.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    // Подсчет всех функций в системе
    @Query("SELECT COUNT(f) FROM Function f")
    long countAllFunctions();

    // Поиск функций с пустым выражением
    @Query("SELECT f FROM Function f WHERE f.expression IS NULL")
    List<Function> findByExpressionIsNull();

    // Поиск функций с непустым выражением
    @Query("SELECT f FROM Function f WHERE f.expression IS NOT NULL")
    List<Function> findByExpressionIsNotNull();

    // Поиск функций с пустым именем
    @Query("SELECT f FROM Function f WHERE f.name IS NULL")
    List<Function> findByNameIsNull();

    // Поиск функций с непустым именем
    @Query("SELECT f FROM Function f WHERE f.name IS NOT NULL")
    List<Function> findByNameIsNotNull();

    // Поиск по нескольким пользователям
    @Query("SELECT f FROM Function f WHERE f.user IN :users")
    List<Function> findByUserIn(@Param("users") List<User> users);

    // Поиск по нескольким ID пользователей
    @Query("SELECT f FROM Function f WHERE f.user.id IN :userIds")
    List<Function> findByUserIdIn(@Param("userIds") List<Long> userIds);

    // Поиск функций с именем, начинающимся с префикса
    @Query("SELECT f FROM Function f WHERE f.name LIKE :prefix%")
    List<Function> findByNameStartingWith(@Param("prefix") String prefix);

    // ADD CASE INSENSITIVE VERSION
    List<Function> findByNameStartingWithIgnoreCase(String prefix);

    // Поиск функций с именем, заканчивающимся на суффикс
    @Query("SELECT f FROM Function f WHERE f.name LIKE %:suffix")
    List<Function> findByNameEndingWith(@Param("suffix") String suffix);

    // ADD CASE INSENSITIVE VERSION
    List<Function> findByNameEndingWithIgnoreCase(String suffix);

    // Поиск функций по длине имени
    @Query("SELECT f FROM Function f WHERE LENGTH(f.name) > :minLength")
    List<Function> findByNameLengthGreaterThan(@Param("minLength") int minLength);

    // Поиск функций по длине выражения
    @Query("SELECT f FROM Function f WHERE LENGTH(f.expression) < :maxLength")
    List<Function> findByExpressionLengthLessThan(@Param("maxLength") int maxLength);

    // Поиск функций с именами в указанном списке
    @Query("SELECT f FROM Function f WHERE f.name IN :names")
    List<Function> findByNameIn(@Param("names") List<String> names);

    // Поиск функций с выражениями в указанном списке
    @Query("SELECT f FROM Function f WHERE f.expression IN :expressions")
    List<Function> findByExpressionIn(@Param("expressions") List<String> expressions);

    // Поиск функций по пользователю и части выражения
    @Query("SELECT f FROM Function f WHERE f.user = :user AND f.expression LIKE %:expressionPart%")
    List<Function> findByUserAndExpressionContaining(@Param("user") User user,
                                                     @Param("expressionPart") String expressionPart);

    // Поиск функций созданных после определенного ID
    @Query("SELECT f FROM Function f WHERE f.id > :minId")
    List<Function> findByIdGreaterThan(@Param("minId") Long minId);

    // Поиск функций созданных до определенного ID
    @Query("SELECT f FROM Function f WHERE f.id < :maxId")
    List<Function> findByIdLessThan(@Param("maxId") Long maxId);

    // Поиск первых N функций пользователя
    @Query("SELECT f FROM Function f WHERE f.user = :user ORDER BY f.id ASC")
    List<Function> findTopNByUserOrderByIdAsc(@Param("user") User user, org.springframework.data.domain.Pageable pageable);

    // Поиск последних N функций пользователя
    @Query("SELECT f FROM Function f WHERE f.user = :user ORDER BY f.id DESC")
    List<Function> findTopNByUserOrderByIdDesc(@Param("user") User user, org.springframework.data.domain.Pageable pageable);

    // Поиск уникальных имен функций для пользователя
    @Query("SELECT DISTINCT f.name FROM Function f WHERE f.user = :user")
    List<String> findDistinctNamesByUser(@Param("user") User user);

    // Поиск функций с группировкой по пользователю
    @Query("SELECT f.user, COUNT(f) FROM Function f GROUP BY f.user")
    List<Object[]> countFunctionsByUser();

    // Поиск функций с максимальным ID для каждого пользователя
    @Query("SELECT f1 FROM Function f1 WHERE f1.id = (SELECT MAX(f2.id) FROM Function f2 WHERE f2.user = f1.user)")
    List<Function> findLatestFunctionPerUser();

    // Поиск функций с минимальным ID для каждого пользователя
    @Query("SELECT f1 FROM Function f1 WHERE f1.id = (SELECT MIN(f2.id) FROM Function f2 WHERE f2.user = f1.user)")
    List<Function> findFirstFunctionPerUser();

    // Поиск функций по сложному критерию (имя и выражение)
    @Query("SELECT f FROM Function f WHERE f.name LIKE %:namePart% AND f.expression LIKE %:expressionPart%")
    List<Function> findByNameContainingAndExpressionContaining(@Param("namePart") String namePart,
                                                               @Param("expressionPart") String expressionPart);

    // ADD CASE INSENSITIVE VERSION
    @Query("SELECT f FROM Function f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :namePart, '%')) AND f.expression LIKE %:expressionPart%")
    List<Function> findByNameContainingIgnoreCaseAndExpressionContaining(@Param("namePart") String namePart,
                                                                         @Param("expressionPart") String expressionPart);

    // Поиск функций по пользователю и диапазону ID
    @Query("SELECT f FROM Function f WHERE f.user = :user AND f.id BETWEEN :startId AND :endId")
    List<Function> findByUserAndIdBetween(@Param("user") User user,
                                          @Param("startId") Long startId,
                                          @Param("endId") Long endId);

    // Подсчет функций по длине имени
    @Query("SELECT LENGTH(f.name), COUNT(f) FROM Function f GROUP BY LENGTH(f.name) ORDER BY LENGTH(f.name)")
    List<Object[]> countFunctionsByNameLength();

    // Поиск функций с самым длинным именем
    @Query("SELECT f FROM Function f WHERE LENGTH(f.name) = (SELECT MAX(LENGTH(f2.name)) FROM Function f2)")
    List<Function> findFunctionsWithLongestName();

    // Поиск функций с самым коротким именем
    @Query("SELECT f FROM Function f WHERE LENGTH(f.name) = (SELECT MIN(LENGTH(f2.name)) FROM Function f2)")
    List<Function> findFunctionsWithShortestName();

    // Обновление выражения функции по ID
    @Transactional
    @Modifying
    @Query("UPDATE Function f SET f.expression = :expression WHERE f.id = :id")
    int updateExpressionById(@Param("id") Long id, @Param("expression") String expression);

    // Обновление имени функции по ID
    @Transactional
    @Modifying
    @Query("UPDATE Function f SET f.name = :name WHERE f.id = :id")
    int updateNameById(@Param("id") Long id, @Param("name") String name);

    // Удаление функций по префиксу имени
    @Transactional
    @Modifying
    @Query("DELETE FROM Function f WHERE f.name LIKE :prefix%")
    int deleteByNameStartingWith(@Param("prefix") String prefix);

    // Удаление функций по суффиксу имени
    @Transactional
    @Modifying
    @Query("DELETE FROM Function f WHERE f.name LIKE %:suffix")
    int deleteByNameEndingWith(@Param("suffix") String suffix);

    // Проверка наличия каких-либо функций у пользователя
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Function f WHERE f.user = :user")
    Boolean existsByUser(@Param("user") User user);

    // Поиск функций с пагинацией
    @Query("SELECT f FROM Function f WHERE f.user = :user ORDER BY f.name ASC")
    List<Function> findByUserWithPagination(@Param("user") User user, org.springframework.data.domain.Pageable pageable);

    // Поиск всех функций с сортировкой по имени
    @Query("SELECT f FROM Function f ORDER BY f.name ASC")
    List<Function> findAllOrderByNameAsc();

    // Поиск всех функций с сортировкой по дате создания
    @Query("SELECT f FROM Function f ORDER BY f.createdAt DESC")
    List<Function> findAllOrderByCreatedAtDesc();

    // STREAMING МЕТОДЫ С ПАГИНАЦИЕЙ
    @Query("SELECT f FROM Function f ORDER BY f.id")
    Stream<Function> streamAllWithPagination(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT f FROM Function f WHERE f.user = :user ORDER BY f.id")
    Stream<Function> streamByUserWithPagination(@Param("user") User user, org.springframework.data.domain.Pageable pageable);

    // STREAMING ДЛЯ СТАТИСТИКИ (только основные поля)
    @Query("SELECT f.id, f.name, f.expression FROM Function f WHERE f.id IS NOT NULL")
    Stream<Object[]> streamAllForStatistics();

    // STREAMING ДЛЯ МАССОВОЙ ОБРАБОТКИ
    @Query("SELECT f FROM Function f WHERE f.id >= :startId AND f.id <= :endId")
    Stream<Function> streamByIdRange(@Param("startId") Long startId, @Param("endId") Long endId);

    // STREAMING С ФИЛЬТРАЦИЕЙ ПО ДАТЕ
    @Query("SELECT f FROM Function f WHERE f.createdAt >= :startDate")
    Stream<Function> streamByCreatedAfter(@Param("startDate") java.time.LocalDateTime startDate);

    @Query("SELECT f FROM Function f WHERE f.createdAt <= :endDate")
    Stream<Function> streamByCreatedBefore(@Param("endDate") java.time.LocalDateTime endDate);

    // STREAMING ДЛЯ ЭКСПОРТА ДАННЫХ
    @Query("SELECT f.id, f.name, f.expression, f.createdAt, f.user.username FROM Function f")
    Stream<Object[]> streamAllForExport();

    // STREAMING С АГРЕГАЦИЕЙ
    @Query("SELECT f.user.id, COUNT(f) FROM Function f GROUP BY f.user.id")
    Stream<Object[]> streamUserFunctionCounts();

    // STREAMING ДЛЯ ПОИСКА ДУБЛИКАТОВ
    @Query("SELECT f.name, COUNT(f) FROM Function f GROUP BY f.name HAVING COUNT(f) > 1")
    Stream<Object[]> streamDuplicateNames();

    // STREAMING ДЛЯ ОЧИСТКИ ДАННЫХ
    @Query("SELECT f FROM Function f WHERE f.expression IS NULL OR f.name IS NULL")
    Stream<Function> streamInvalidFunctions();
}