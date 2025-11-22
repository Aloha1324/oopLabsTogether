package com.example.LAB5.framework.repository;

import com.example.LAB5.framework.entity.User;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
        return testPrefix + baseName + "_" + System.currentTimeMillis();
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
    void testUserNotFound() {
        logger.info("=== Тест поиска несуществующего пользователя ===");

        Optional<User> nonExistentUser = userRepository.findById(999999L);
        assertFalse(nonExistentUser.isPresent());

        logger.info("Несуществующий пользователь не найден (ожидаемо)");
    }

    @Test
    @Order(10)
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

    @AfterEach
    void tearDown() {
        try {
            cleanTestData();
        } catch (Exception e) {
            logger.warn("Ошибка при очистке тестовых данных: {}", e.getMessage());
        }
    }

    private void cleanTestData() {
        logger.info("Очистка тестовых данных...");

        List<User> allUsers = userRepository.findAll();
        int deletedCount = 0;

        for (User user : allUsers) {
            if (user.getUsername().startsWith(testPrefix)) {
                userRepository.delete(user);
                deletedCount++;
            }
        }

        if (deletedCount > 0) {
            logger.info("Очищено {} тестовых пользователей", deletedCount);
        }
    }
}