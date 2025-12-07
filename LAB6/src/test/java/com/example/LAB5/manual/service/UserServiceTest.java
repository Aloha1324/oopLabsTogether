package com.example.LAB5.manual.service;

import com.example.LAB5.manual.DAO.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceTest.class);
    private UserDAO userDAO;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
        userService = new UserService(userDAO);
        logger.info("Настройка тестовой среды для UserService");
    }

    @AfterEach
    void tearDown() {
        // Очистка тестовых данных если нужно
    }

    @Test
    void testCreateUser() {
        String uniqueLogin = "service_test_user_" + UUID.randomUUID().toString().substring(0, 8);
        Long userId = userService.createUser(uniqueLogin, "service_pass");

        assertNotNull(userId);
        assertTrue(userId > 0);

        Map<String, Object> createdUser = userService.getUserById(userId);
        assertNotNull(createdUser);
        assertEquals(uniqueLogin, createdUser.get("username"));
        assertEquals("service_pass", createdUser.get("password"));
    }

    @Test
    void testCreateUserWithEmail() {
        String uniqueLogin = "email_user_" + UUID.randomUUID().toString().substring(0, 8);
        String email = uniqueLogin + "@test.com";

        Long userId = userService.createUserWithEmail(uniqueLogin, "password123", email);

        assertNotNull(userId);
        assertTrue(userId > 0);

        Map<String, Object> createdUser = userService.getUserById(userId);
        assertNotNull(createdUser);
        assertEquals(uniqueLogin, createdUser.get("username"));
        assertEquals(email, createdUser.get("email"));
    }

    @Test
    void testGetUserById() {
        String uniqueLogin = "get_by_id_user_" + UUID.randomUUID().toString().substring(0, 8);
        Long userId = userService.createUser(uniqueLogin, "password123");

        Map<String, Object> foundUser = userService.getUserById(userId);

        assertNotNull(foundUser);
        assertEquals(userId, foundUser.get("id"));
        assertEquals(uniqueLogin, foundUser.get("username"));
    }

    @Test
    void testGetUserById_NotFound() {
        Map<String, Object> foundUser = userService.getUserById(999999L);

        assertNull(foundUser);
    }

    @Test
    void testGetUserByUsername() {
        String uniqueLogin = "username_search_user_" + UUID.randomUUID().toString().substring(0, 8);
        userService.createUser(uniqueLogin, "mod_pass");

        Map<String, Object> foundUser = userService.getUserByUsername(uniqueLogin);

        assertNotNull(foundUser);
        assertEquals(uniqueLogin, foundUser.get("username"));
    }

    @Test
    void testGetUserByUsername_NotFound() {
        Map<String, Object> foundUser = userService.getUserByUsername("non_existent_login_" + UUID.randomUUID());

        assertNull(foundUser);
    }

    @Test
    void testGetAllUsers() {
        int initialCount = userService.getAllUsers().size();

        String uniqueLogin1 = "all_users_1_" + UUID.randomUUID().toString().substring(0, 8);
        String uniqueLogin2 = "all_users_2_" + UUID.randomUUID().toString().substring(0, 8);
        String uniqueLogin3 = "all_users_3_" + UUID.randomUUID().toString().substring(0, 8);

        userService.createUser(uniqueLogin1, "pass1");
        userService.createUser(uniqueLogin2, "pass2");
        userService.createUser(uniqueLogin3, "pass3");

        List<Map<String, Object>> allUsers = userService.getAllUsers();

        assertFalse(allUsers.isEmpty());
        assertTrue(allUsers.size() >= initialCount + 3);
        logger.info("Сервис вернул {} пользователей", allUsers.size());
    }

    @Test
    void testGetUsersByUsernamePattern() {
        String baseLogin = "pattern_test_" + UUID.randomUUID().toString().substring(0, 8);
        String uniqueLogin1 = baseLogin + "_user1";
        String uniqueLogin2 = baseLogin + "_user2";
        String uniqueLogin3 = "other_user_" + UUID.randomUUID().toString().substring(0, 8);

        userService.createUser(uniqueLogin1, "pass1");
        userService.createUser(uniqueLogin2, "pass2");
        userService.createUser(uniqueLogin3, "pass3");

        List<Map<String, Object>> patternUsers = userService.getUsersByUsernamePattern(baseLogin);

        assertFalse(patternUsers.isEmpty());
        assertTrue(patternUsers.size() >= 2);
        patternUsers.forEach(user -> {
            String username = (String) user.get("username");
            assertTrue(username.contains(baseLogin));
        });
    }

    @Test
    void testUpdateUser() {
        // Given
        String originalLogin = "to_update_user_" + UUID.randomUUID().toString().substring(0, 8);
        Long userId = userService.createUser(originalLogin, "old_password");

        String updatedLogin = "updated_user_" + UUID.randomUUID().toString().substring(0, 8);
        boolean updated = userService.updateUser(userId, updatedLogin, "new_password");

        assertTrue(updated);

        Map<String, Object> updatedUser = userService.getUserById(userId);
        assertNotNull(updatedUser);
        assertEquals(updatedLogin, updatedUser.get("username"));
        assertEquals("new_password", updatedUser.get("password"));
    }

    @Test
    void testUpdateUserEmail() {
        String originalLogin = "email_update_user_" + UUID.randomUUID().toString().substring(0, 8);
        Long userId = userService.createUser(originalLogin, "password");

        String newEmail = "updated_" + originalLogin + "@test.com";
        boolean updated = userService.updateUserEmail(userId, newEmail);

        assertTrue(updated);

        Map<String, Object> updatedUser = userService.getUserById(userId);
        assertNotNull(updatedUser);
        assertEquals(newEmail, updatedUser.get("email"));
    }

    @Test
    void testUpdateUser_NotFound() {
        boolean updated = userService.updateUser(999999L, "new_login", "new_pass");

        assertFalse(updated);
    }

    @Test
    void testDeleteUser() {
        String uniqueLogin = "to_delete_service_" + UUID.randomUUID().toString().substring(0, 8);
        Long userId = userService.createUser(uniqueLogin, "password");

        boolean deleted = userService.deleteUser(userId);

        assertTrue(deleted);

        Map<String, Object> deletedUser = userService.getUserById(userId);
        assertNull(deletedUser);
    }

    @Test
    void testDeleteUserByUsername() {
        String uniqueLogin = "to_delete_by_username_" + UUID.randomUUID().toString().substring(0, 8);
        userService.createUser(uniqueLogin, "password");

        boolean deleted = userService.deleteUserByUsername(uniqueLogin);

        assertTrue(deleted);

        Map<String, Object> deletedUser = userService.getUserByUsername(uniqueLogin);
        assertNull(deletedUser);
    }

    @Test
    void testDeleteUser_NotFound() {
        boolean deleted = userService.deleteUser(999999L);

        assertFalse(deleted);
    }

    @Test
    void testValidateUserCredentials_Valid() {
        String uniqueLogin = "valid_user_" + UUID.randomUUID().toString().substring(0, 8);
        userService.createUser(uniqueLogin, "correct_password");

        boolean isValid = userService.validateUserCredentials(uniqueLogin, "correct_password");

        assertTrue(isValid);
    }

    @Test
    void testValidateUserCredentials_InvalidPassword() {
        String uniqueLogin = "invalid_pass_user_" + UUID.randomUUID().toString().substring(0, 8);
        userService.createUser(uniqueLogin, "correct_password");

        boolean isValid = userService.validateUserCredentials(uniqueLogin, "wrong_password");

        assertFalse(isValid);
    }

    @Test
    void testValidateUserCredentials_UserNotFound() {
        boolean isValid = userService.validateUserCredentials("non_existent_user_" + UUID.randomUUID(), "any_password");

        assertFalse(isValid);
    }

    @Test
    void testUserExists() {
        String uniqueLogin = "exists_test_user_" + UUID.randomUUID().toString().substring(0, 8);

        boolean existsBefore = userService.userExists(uniqueLogin);
        assertFalse(existsBefore);

        userService.createUser(uniqueLogin, "password");

        boolean existsAfter = userService.userExists(uniqueLogin);
        assertTrue(existsAfter);
    }

    @Test
    void testChangePassword_Success() {
        String uniqueLogin = "change_pass_user_" + UUID.randomUUID().toString().substring(0, 8);
        Long userId = userService.createUser(uniqueLogin, "old_password");

        boolean changed = userService.changePassword(userId, "old_password", "new_password");
        assertTrue(changed);

        boolean validWithNew = userService.validateUserCredentials(uniqueLogin, "new_password");
        assertTrue(validWithNew);

        boolean validWithOld = userService.validateUserCredentials(uniqueLogin, "old_password");
        assertFalse(validWithOld);
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        String uniqueLogin = "wrong_old_pass_" + UUID.randomUUID().toString().substring(0, 8);
        Long userId = userService.createUser(uniqueLogin, "correct_old_password");

        boolean changed = userService.changePassword(userId, "wrong_old_password", "new_password");
        assertFalse(changed);

        boolean stillValid = userService.validateUserCredentials(uniqueLogin, "correct_old_password");
        assertTrue(stillValid);
    }

    @Test
    void testResetPassword() {
        String uniqueLogin = "reset_pass_user_" + UUID.randomUUID().toString().substring(0, 8);
        Long userId = userService.createUser(uniqueLogin, "old_password");

        boolean reset = userService.resetPassword(userId, "new_password");
        assertTrue(reset);

        boolean validWithNew = userService.validateUserCredentials(uniqueLogin, "new_password");
        assertTrue(validWithNew);
    }

    @Test
    void testGetTotalUsersCount() {
        int initialCount = userService.getTotalUsersCount();

        String uniqueLogin1 = "count_test_1_" + UUID.randomUUID().toString().substring(0, 8);
        String uniqueLogin2 = "count_test_2_" + UUID.randomUUID().toString().substring(0, 8);

        userService.createUser(uniqueLogin1, "pass1");
        userService.createUser(uniqueLogin2, "pass2");

        int newCount = userService.getTotalUsersCount();
        assertTrue(newCount >= initialCount + 2);
    }

    @Test
    void testSearchUsers() {
        String baseTerm = "search_test_" + UUID.randomUUID().toString().substring(0, 8);
        String login1 = baseTerm + "_user1";
        String login2 = baseTerm + "_user2";
        String login3 = "other_user_" + UUID.randomUUID().toString().substring(0, 8);

        userService.createUser(login1, "pass1");
        userService.createUser(login2, "pass2");
        userService.createUser(login3, "pass3");

        List<Map<String, Object>> searchResults = userService.searchUsers(baseTerm);

        assertFalse(searchResults.isEmpty());
        assertEquals(2, searchResults.size());
        searchResults.forEach(user -> {
            String username = (String) user.get("username");
            assertTrue(username.contains(baseTerm));
        });
    }

    @Test
    void testMultipleServiceOperations() {
        logger.info("Запуск комплексного теста сервиса");

        String login1 = "workflow_1_" + UUID.randomUUID().toString().substring(0, 8);
        String login2 = "workflow_2_" + UUID.randomUUID().toString().substring(0, 8);

        Long user1 = userService.createUser(login1, "pass1");
        Long user2 = userService.createUser(login2, "pass2");

        assertNotNull(user1);
        assertNotNull(user2);

        Map<String, Object> foundUser1 = userService.getUserById(user1);
        Map<String, Object> foundUser2 = userService.getUserById(user2);

        assertNotNull(foundUser1);
        assertNotNull(foundUser2);

        String updatedLogin = "updated_workflow_1_" + UUID.randomUUID().toString().substring(0, 8);
        boolean updated = userService.updateUser(user1, updatedLogin, "new_pass1");
        assertTrue(updated);

        Map<String, Object> updatedUser = userService.getUserById(user1);
        assertNotNull(updatedUser);
        assertEquals(updatedLogin, updatedUser.get("username"));

        boolean validOld = userService.validateUserCredentials(login1, "pass1");
        boolean validNew = userService.validateUserCredentials(updatedLogin, "new_pass1");

        assertFalse(validOld);
        assertTrue(validNew);

        boolean deleted = userService.deleteUser(user2);
        assertTrue(deleted);

        Map<String, Object> deletedUser = userService.getUserById(user2);
        assertNull(deletedUser);

        logger.info("Комплексный тест сервиса завершен успешно");
    }

    @Test
    void testServiceErrorHandling() {
        // Тест с пустым логином
        assertThrows(Exception.class, () -> {
            userService.createUser("", "pass");
        });

        logger.info("Обработка ошибок в сервисе проверена");
    }
}