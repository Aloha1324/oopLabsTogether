package com.example.lab5;

import com.example.lab5.dao.UserDao;
import com.example.lab5.entity.User;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDaoTest {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoTest.class);
    private UserDao userDao;

    @BeforeAll
    void setup() {
        userDao = new UserDao();
        logger.info("Starting UserDao tests");
    }

    @Test
    void testSaveAndFindUser() {
        // Create diverse test data
        User user = new User("test_user_" + System.currentTimeMillis(), "hashed_password_123");

        // Save user
        User savedUser = userDao.save(user);
        assertNotNull(savedUser.getId());
        logger.info("Saved user with ID: {}", savedUser.getId());

        // Find by ID
        Optional<User> foundUser = userDao.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getUsername(), foundUser.get().getUsername());

        // Find by username
        Optional<User> foundByUsername = userDao.findByUsername(savedUser.getUsername());
        assertTrue(foundByUsername.isPresent());
    }

    @Test
    void testUpdateUser() {
        User user = new User("update_test_" + System.currentTimeMillis(), "original_hash");
        User savedUser = userDao.save(user);

        // Update user
        savedUser.setPasswordHash("updated_hash_456");
        boolean updated = userDao.update(savedUser);
        assertTrue(updated);

        // Verify update
        Optional<User> foundUser = userDao.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("updated_hash_456", foundUser.get().getPasswordHash());
    }

    @Test
    void testDeleteUser() {
        User user = new User("delete_test_" + System.currentTimeMillis(), "hash_to_delete");
        User savedUser = userDao.save(user);

        // Delete user
        boolean deleted = userDao.delete(savedUser.getId());
        assertTrue(deleted);

        // Verify deletion
        Optional<User> foundUser = userDao.findById(savedUser.getId());
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindAllUsers() {
        // Create multiple users
        User user1 = new User("user1_" + System.currentTimeMillis(), "hash1");
        User user2 = new User("user2_" + System.currentTimeMillis(), "hash2");

        userDao.save(user1);
        userDao.save(user2);

        List<User> users = userDao.findAll();
        assertTrue(users.size() >= 2);
        logger.info("Found {} total users", users.size());
    }

    @Test
    void testFindByUsernameContaining() {
        String uniquePrefix = "search_test_" + System.currentTimeMillis();
        User user1 = new User(uniquePrefix + "_alice", "hash1");
        User user2 = new User(uniquePrefix + "_bob", "hash2");

        userDao.save(user1);
        userDao.save(user2);

        List<User> foundUsers = userDao.findByUsernameContaining(uniquePrefix);
        assertEquals(2, foundUsers.size());
    }

    @Test
    void testUserCount() {
        int initialCount = userDao.count();

        User user = new User("count_test_" + System.currentTimeMillis(), "hash_count");
        userDao.save(user);

        int newCount = userDao.count();
        assertTrue(newCount > initialCount);
    }

    @AfterEach
    void cleanup() {
        logger.info("Test completed");
    }
}