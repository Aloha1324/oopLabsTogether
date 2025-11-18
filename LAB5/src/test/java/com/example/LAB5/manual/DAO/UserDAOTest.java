package com.example.LAB5.manual.DAO;

import com.example.LAB5.manual.DAO.UserDAO;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDAOTest {
    private static final Logger logger = LoggerFactory.getLogger(UserDAOTest.class);
    private UserDAO userDAO;
    private static String testPrefix;

    @BeforeAll
    static void setUpAll() {
        testPrefix = "user_test_" + UUID.randomUUID().toString().substring(0, 8) + "_";
        logger.info("Установлен префикс тестов: {}", testPrefix);
    }

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
    }

    private String uniqueUsername(String baseName) {
        return testPrefix + baseName + "_" + System.currentTimeMillis();
    }

    @Test
    @Order(1)
    void testCreateAndFindUser() {
        String username = uniqueUsername("testuser");
        String passwordHash = "hashed_password_123";

        Long userId = userDAO.createUser(username, passwordHash);

        assertNotNull(userId);
        assertTrue(userId > 0);

        Map<String, Object> foundUser = userDAO.findByUsername(username);
        assertNotNull(foundUser);
        assertEquals(username, foundUser.get("username"));
        assertEquals(passwordHash, foundUser.get("password_hash"));
    }

    @Test
    @Order(2)
    void testFindUserById() {
        String username = uniqueUsername("findbyid");
        String passwordHash = "hashed_password_456";

        Long userId = userDAO.createUser(username, passwordHash);

        Map<String, Object> foundUser = userDAO.findById(userId);
        assertNotNull(foundUser);
        assertEquals(userId, foundUser.get("id"));
        assertEquals(username, foundUser.get("username"));
    }

    @Test
    @Order(3)
    void testFindByUsernameLike() {
        String baseUsername = uniqueUsername("searchuser");
        userDAO.createUser(baseUsername + "_alpha", "pass1");
        userDAO.createUser(baseUsername + "_beta", "pass2");
        userDAO.createUser("other_user_123", "pass3");

        List<Map<String, Object>> foundUsers = userDAO.findByUsernameLike("searchuser");

        assertFalse(foundUsers.isEmpty());
        assertTrue(foundUsers.size() >= 2);
        foundUsers.forEach(user ->
                assertTrue(((String) user.get("username")).contains("searchuser"))
        );
    }

    @Test
    @Order(4)
    void testUserNotFound() {
        Map<String, Object> nonExistentUser = userDAO.findById(999999L);
        assertNull(nonExistentUser);

        Map<String, Object> nonExistentByUsername = userDAO.findByUsername("nonexistent_user_xyz");
        assertNull(nonExistentByUsername);
    }

    @Test
    @Order(5)
    void testUpdateUser() {
        String username = uniqueUsername("updateuser");
        String passwordHash = "original_hash";

        Long userId = userDAO.createUser(username, passwordHash);

        String newUsername = uniqueUsername("updateduser");
        String newPasswordHash = "updated_hash";
        boolean updated = userDAO.updateUser(userId, newUsername, newPasswordHash);

        assertTrue(updated);

        Map<String, Object> foundUser = userDAO.findById(userId);
        assertNotNull(foundUser);
        assertEquals(newUsername, foundUser.get("username"));
        assertEquals(newPasswordHash, foundUser.get("password_hash"));
    }

    @Test
    @Order(6)
    void testUpdatePassword() {
        String username = uniqueUsername("updatepass");
        String originalHash = "original_password_hash";

        Long userId = userDAO.createUser(username, originalHash);

        String newHash = "new_secure_hash";
        boolean updated = userDAO.updateUser(userId, username, newHash);

        assertTrue(updated);

        Map<String, Object> foundUser = userDAO.findByUsername(username);
        assertNotNull(foundUser);
        assertEquals(newHash, foundUser.get("password_hash"));
    }

    @Test
    @Order(7)
    void testDeleteUser() {
        String username = uniqueUsername("deleteuser");
        String passwordHash = "password_to_delete";

        Long userId = userDAO.createUser(username, passwordHash);

        boolean deleted = userDAO.deleteUser(userId);
        assertTrue(deleted);

        Map<String, Object> deletedUser = userDAO.findById(userId);
        assertNull(deletedUser);
    }

    @Test
    @Order(8)
    void testFindAllUsers() {
        int initialCount = userDAO.findAll().size();

        userDAO.createUser(uniqueUsername("allusers1"), "pass1");
        userDAO.createUser(uniqueUsername("allusers2"), "pass2");

        List<Map<String, Object>> allUsers = userDAO.findAll();

        assertFalse(allUsers.isEmpty());
        assertTrue(allUsers.size() >= initialCount + 2);
        logger.info("Найдено {} пользователей", allUsers.size());
    }

    @Test
    @Order(9)
    void testGetTotalUsersCount() {
        int initialCount = userDAO.getTotalUsersCount();

        userDAO.createUser(uniqueUsername("counttest1"), "pass1");
        userDAO.createUser(uniqueUsername("counttest2"), "pass2");

        int newCount = userDAO.getTotalUsersCount();
        assertTrue(newCount >= initialCount + 2);
    }


    @AfterEach
    void tearDown() {
        try {
            // Очистка тестовых данных
            List<Map<String, Object>> allUsers = userDAO.findAll();
            int deletedCount = 0;

            for (Map<String, Object> user : allUsers) {
                String username = (String) user.get("username");
                if (username != null && username.startsWith(testPrefix)) {
                    userDAO.deleteUser((Long) user.get("id"));
                    deletedCount++;
                }
            }

            if (deletedCount > 0) {
                logger.info("Очищено {} тестовых пользователей", deletedCount);
            }
        } catch (Exception e) {
            logger.warn("Ошибка при очистке тестовых данных: {}", e.getMessage());
        }
    }
}