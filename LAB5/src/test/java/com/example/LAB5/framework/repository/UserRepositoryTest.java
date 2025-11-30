package com.example.LAB5.framework.repository;

import com.example.LAB5.framework.entity.User;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryTest.class);

    @Autowired
    private UserRepository userRepository;

    private static String testPrefix;

    @BeforeAll
    static void setUpAll() {
        testPrefix = "user_test_" + UUID.randomUUID().toString().substring(0, 8) + "_";
        logger.info("Установлен префикс тестов: {}", testPrefix);
    }

    private String uniqueUsername(String baseName) {
        return testPrefix + baseName + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4);
    }

    private User createTestUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("test_password_hash");
        return userRepository.save(user);
    }

    @Test
    @Order(1)
    void testSaveAndFindById() {
        logger.info("=== Тест сохранения и поиска по ID ===");

        String username = uniqueUsername("test_user");
        User user = createTestUser(username);

        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);

        Optional<User> foundUser = userRepository.findById(user.getId());
        assertTrue(foundUser.isPresent());
        assertEquals(username, foundUser.get().getUsername());
        assertEquals("test_password_hash", foundUser.get().getPasswordHash());

        logger.info("Создан пользователь: ID={}, Username={}", user.getId(), user.getUsername());
    }

    @Test
    @Order(2)
    void testFindByUsername() {
        logger.info("=== Тест поиска по имени пользователя ===");

        String username = uniqueUsername("find_by_username");
        createTestUser(username);

        Optional<User> foundUser = userRepository.findByUsername(username);
        assertTrue(foundUser.isPresent());
        assertEquals(username, foundUser.get().getUsername());

        logger.info("Найден пользователь по username: {}", username);
    }

    @Test
    @Order(3)
    void testFindAll() {
        logger.info("=== Тест поиска всех пользователей ===");

        int initialCount = userRepository.findAll().size();

        createTestUser(uniqueUsername("all_test_1"));
        createTestUser(uniqueUsername("all_test_2"));
        createTestUser(uniqueUsername("all_test_3"));

        List<User> allUsers = userRepository.findAll();
        assertFalse(allUsers.isEmpty());
        assertTrue(allUsers.size() >= initialCount + 3);

        logger.info("Найдено {} пользователей (было: {})", allUsers.size(), initialCount);
    }

    @Test
    @Order(4)
    void testExistsByUsername() {
        logger.info("=== Тест проверки существования пользователя ===");

        String username = uniqueUsername("exists_test");

        boolean existsBefore = userRepository.existsByUsername(username);
        assertFalse(existsBefore);

        createTestUser(username);

        boolean existsAfter = userRepository.existsByUsername(username);
        assertTrue(existsAfter);

        logger.info("Пользователь {} существует: {}", username, existsAfter);
    }

    @Test
    @Order(5)
    void testFindByUsernameContaining() {
        logger.info("=== Тест поиска по части имени пользователя ===");

        String searchTerm = "search_test";
        createTestUser(uniqueUsername(searchTerm + "_1"));
        createTestUser(uniqueUsername(searchTerm + "_2"));
        createTestUser(uniqueUsername("other_user"));

        List<User> foundUsers = userRepository.findByUsernameContaining(searchTerm);
        assertFalse(foundUsers.isEmpty());
        assertTrue(foundUsers.size() >= 2);

        foundUsers.forEach(user ->
                assertTrue(user.getUsername().contains(searchTerm))
        );

        logger.info("Найдено {} пользователей содержащих '{}'", foundUsers.size(), searchTerm);
    }

    @Test
    @Order(6)
    void testCountUsers() {
        logger.info("=== Тест подсчета пользователей ===");

        int initialCount = userRepository.countUsers();

        createTestUser(uniqueUsername("count_test_1"));
        createTestUser(uniqueUsername("count_test_2"));

        int finalCount = userRepository.countUsers();
        assertTrue(finalCount >= initialCount + 2);

        logger.info("Количество пользователей: было {}, стало {}", initialCount, finalCount);
    }

    @Test
    @Order(7)
    void testUpdateUser() {
        logger.info("=== Тест обновления пользователя ===");

        String originalUsername = uniqueUsername("update_test");
        User user = createTestUser(originalUsername);

        String updatedUsername = uniqueUsername("updated_user");
        user.setUsername(updatedUsername);
        user.setPasswordHash("updated_hash");

        User updatedUser = userRepository.save(user);
        assertNotNull(updatedUser);

        Optional<User> foundUser = userRepository.findById(user.getId());
        assertTrue(foundUser.isPresent());
        assertEquals(updatedUsername, foundUser.get().getUsername());
        assertEquals("updated_hash", foundUser.get().getPasswordHash());

        logger.info("Пользователь обновлен: {}", updatedUsername);
    }

    @Test
    @Order(8)
    void testDeleteUser() {
        logger.info("=== Тест удаления пользователя ===");

        String username = uniqueUsername("delete_test");
        User user = createTestUser(username);

        userRepository.delete(user);

        Optional<User> foundUser = userRepository.findById(user.getId());
        assertFalse(foundUser.isPresent());

        logger.info("Пользователь удален: ID={}", user.getId());
    }

    @Test
    @Order(9)
    void testDeleteById() {
        logger.info("=== Тест удаления пользователя по ID ===");

        String username = uniqueUsername("delete_by_id_test");
        User user = createTestUser(username);
        Long userId = user.getId();

        userRepository.deleteById(userId);

        Optional<User> foundUser = userRepository.findById(userId);
        assertFalse(foundUser.isPresent());

        logger.info("Пользователь удален по ID: {}", userId);
    }

    @Test
    @Order(10)
    void testUserNotFound() {
        logger.info("=== Тест поиска несуществующего пользователя ===");

        Optional<User> nonExistentUser = userRepository.findById(999999L);
        assertFalse(nonExistentUser.isPresent());

        Optional<User> nonExistentByUsername = userRepository.findByUsername("non_existent_user_12345");
        assertFalse(nonExistentByUsername.isPresent());

        logger.info("Несуществующий пользователь не найден (ожидаемо)");
    }

    @Test
    @Order(11)
    void testFindUsersByUsernamePattern() {
        logger.info("=== Тест поиска пользователей по шаблону ===");

        String pattern = "pattern";
        createTestUser(uniqueUsername(pattern + "_user1"));
        createTestUser(uniqueUsername(pattern + "_user2"));
        createTestUser(uniqueUsername("different_user"));

        List<User> foundUsers = userRepository.findUsersByUsernamePattern(pattern);
        assertFalse(foundUsers.isEmpty());
        assertTrue(foundUsers.size() >= 2);

        foundUsers.forEach(user ->
                assertTrue(user.getUsername().contains(pattern))
        );

        logger.info("Найдено {} пользователей по шаблону '{}'", foundUsers.size(), pattern);
    }

    @Test
    @Order(12)
    void testBatchOperations() {
        logger.info("=== Тест пакетных операций ===");

        // Создание пользователей с уникальными именами
        String batchPrefix = testPrefix + "batch_" + System.currentTimeMillis() + "_";

        List<User> users = Arrays.asList(
                createTestUser(batchPrefix + "1"),
                createTestUser(batchPrefix + "2"),
                createTestUser(batchPrefix + "3")
        );

        // Сохранение всех
        List<User> savedUsers = userRepository.saveAll(users);
        assertEquals(3, savedUsers.size());

        // Проверка сохранения - ищем ТОЛЬКО по нашему уникальному префиксу
        List<User> foundBatchUsers = userRepository.findAll().stream()
                .filter(u -> u.getUsername().startsWith(batchPrefix))
                .collect(Collectors.toList());
        assertEquals(3, foundBatchUsers.size(),
                "Должны найтись только что созданные 3 пользователя с префиксом: " + batchPrefix);

        // Удаление всех
        userRepository.deleteAll(foundBatchUsers);

        // Проверка что удалены - снова ищем по нашему префиксу
        List<User> remainingBatchUsers = userRepository.findAll().stream()
                .filter(u -> u.getUsername().startsWith(batchPrefix))
                .collect(Collectors.toList());
        assertEquals(0, remainingBatchUsers.size(),
                "После удаления не должно остаться пользователей с префиксом: " + batchPrefix);

        logger.info("Пакетные операции завершены успешно");
    }

    @Test
    @Order(13)
    void testTransactionalBehavior() {
        logger.info("=== Тест транзакционного поведения ===");

        String username = uniqueUsername("transaction_test");

        // Должно работать в транзакции
        User user = userRepository.save(new User(username, "hash"));
        assertNotNull(user.getId());

        // Должно найтись в той же транзакции
        Optional<User> found = userRepository.findByUsername(username);
        assertTrue(found.isPresent());
        assertEquals(username, found.get().getUsername());

        logger.info("Транзакционное поведение проверено успешно");
    }

    @Test
    @Order(14)
    void testUniqueUsernameConstraint() {
        logger.info("=== Тест ограничения уникальности имени ===");

        String username = uniqueUsername("unique_test");
        createTestUser(username);

        // Попытка создать пользователя с тем же именем
        User duplicateUser = new User(username, "another_hash");

        // Должно выбросить исключение при сохранении
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(duplicateUser);
            userRepository.flush(); // Принудительно выполняем SQL чтобы поймать исключение
        });

        logger.info("Ограничение уникальности имени проверено успешно");
    }

    @Test
    @Order(15)
    void testDeleteAllUsers() {
        logger.info("=== Тест удаления всех пользователей ===");

        // Создаем тестовых пользователей
        createTestUser(uniqueUsername("delete_all_1"));
        createTestUser(uniqueUsername("delete_all_2"));

        // Получаем всех пользователей с нашим префиксом
        List<User> testUsers = userRepository.findAll().stream()
                .filter(user -> user.getUsername().startsWith(testPrefix))
                .collect(Collectors.toList());

        assertFalse(testUsers.isEmpty());

        // Удаляем всех
        userRepository.deleteAll(testUsers);

        // Проверяем что удалились
        List<User> remainingTestUsers = userRepository.findAll().stream()
                .filter(user -> user.getUsername().startsWith(testPrefix))
                .collect(Collectors.toList());

        assertEquals(0, remainingTestUsers.size());
        logger.info("Удалено всех тестовых пользователей: {}", testUsers.size());
    }

    @Test
    @Order(16)
    void testEmptyResultScenarios() {
        logger.info("=== Тест сценариев с пустыми результатами ===");

        // Поиск по несуществующему шаблону
        List<User> emptyPatternResult = userRepository.findUsersByUsernamePattern("non_existent_pattern_12345");
        assertTrue(emptyPatternResult.isEmpty());

        // Поиск по несуществующей части имени
        List<User> emptyContainingResult = userRepository.findByUsernameContaining("non_existent_part_12345");
        assertTrue(emptyContainingResult.isEmpty());

        logger.info("Сценарии с пустыми результатами отработали корректно");
    }

    @Test
    @Order(17)
    void testUserEntityProperties() {
        logger.info("=== Тест свойств сущности User ===");

        String username = uniqueUsername("entity_test");
        User user = new User(username, "test_hash");

        // Проверка до установки ID
        assertNull(user.getId());

        // Сохранение и проверка
        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getId());
        assertEquals(username, savedUser.getUsername());
        assertEquals("test_hash", savedUser.getPasswordHash());

        // Проверка toString
        String toString = savedUser.toString();
        assertTrue(toString.contains("id=" + savedUser.getId()));
        assertTrue(toString.contains("username='" + username + "'"));

        logger.info("Свойства сущности User проверены: {}", savedUser);
    }

    @AfterEach
    void tearDown() {
        cleanTestData();
    }

    private void cleanTestData() {
        try {
            // Эффективная очистка через кастомный запрос
            List<User> testUsers = userRepository.findAll().stream()
                    .filter(user -> user.getUsername().startsWith(testPrefix))
                    .collect(Collectors.toList());

            if (!testUsers.isEmpty()) {
                userRepository.deleteAll(testUsers);
                logger.info("Очищено {} тестовых пользователей", testUsers.size());
            }
        } catch (Exception e) {
            logger.error("Ошибка при очистке тестовых данных: {}", e.getMessage());
        }
    }

    @AfterAll
    static void tearDownAll() {
        logger.info("Все тесты завершены. Префикс тестов: {}", testPrefix);
    }
}