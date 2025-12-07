package com.example.LAB5.manual.service;

import com.example.LAB5.manual.DAO.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO;
    private final FunctionService functionService;

    public UserService() {
        this.userDAO = new UserDAO();
        this.functionService = new FunctionService();
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.functionService = new FunctionService();
    }

    public UserService(UserDAO userDAO, FunctionService functionService) {
        this.userDAO = userDAO;
        this.functionService = functionService;
    }

    public Long createUser(String username, String password) {
        logger.info("Создание пользователя: username={}", username);
        return userDAO.createUser(username, password);
    }

    public Long createUserWithEmail(String username, String password, String email) {
        logger.info("Создание пользователя: username={}, email={}", username, email);
        return userDAO.createUserWithEmail(username, password, email);
    }

    public Map<String, Object> getUserById(Long id) {
        logger.debug("Поиск пользователя по ID: {}", id);
        return userDAO.findById(id);
    }

    public Map<String, Object> getUserByUsername(String username) {
        logger.debug("Поиск пользователя по username: {}", username);
        return userDAO.findByUsername(username);
    }

    public List<Map<String, Object>> getAllUsers() {
        logger.debug("Получение всех пользователей");
        return userDAO.findAll();
    }

    public List<Map<String, Object>> getAllUsersLimited(int limit) {
        logger.debug("Получение ограниченного списка пользователей: limit={}", limit);
        return userDAO.getAllUsersLimited(limit);
    }

    public List<Map<String, Object>> getUsersByUsernamePattern(String usernamePattern) {
        logger.debug("Поиск пользователей по шаблону username: {}", usernamePattern);
        return userDAO.findByUsernameLike(usernamePattern);
    }

    public boolean updateUser(Long id, String username, String password) {
        logger.info("Обновление пользователя с ID: {}", id);
        Map<String, Object> existingUser = userDAO.findById(id);
        if (existingUser != null) {
            return userDAO.updateUser(id, username, password);
        }
        logger.warn("Пользователь с ID {} не найден для обновления", id);
        return false;
    }

    public boolean updateUserEmail(Long id, String email) {
        logger.info("Обновление email пользователя с ID: {}", id);
        Map<String, Object> existingUser = userDAO.findById(id);
        if (existingUser != null) {
            return userDAO.updateUserEmail(id, email);
        }
        logger.warn("Пользователь с ID {} не найден для обновления email", id);
        return false;
    }

    public boolean deleteUser(Long id) {
        logger.info("Удаление пользователя с ID: {}", id);
        return userDAO.deleteUser(id);
    }

    public boolean deleteUserByUsername(String username) {
        logger.info("Удаление пользователя с username: {}", username);
        return userDAO.deleteUserByUsername(username);
    }

    public boolean safeDeleteUser(Long userId) {
        logger.info("Безопасное удаление пользователя с ID: {}", userId);

        try {
            // Сначала удаляем все функции пользователя (точки удалятся каскадно)
            functionService.deleteFunctionsByUserId(userId);

            // Затем удаляем пользователя
            return deleteUser(userId);
        } catch (Exception e) {
            logger.error("Ошибка при безопасном удалении пользователя {}: {}", userId, e.getMessage());
            return false;
        }
    }

    public boolean validateUserCredentials(String username, String password) {
        logger.debug("Проверка учетных данных для пользователя: {}", username);
        Map<String, Object> user = userDAO.findByUsername(username);
        if (user != null) {
            String storedPassword = (String) user.get("password");
            return storedPassword != null && storedPassword.equals(password);
        }
        return false;
    }

    public boolean userExists(String username) {
        logger.debug("Проверка существования пользователя: {}", username);
        return userDAO.existsByUsername(username);
    }

    public int getTotalUsersCount() {
        logger.debug("Получение общего количества пользователей");
        return userDAO.getTotalUsersCount();
    }

    // Дополнительные бизнес-методы
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        logger.info("Смена пароля для пользователя с ID: {}", userId);
        Map<String, Object> user = userDAO.findById(userId);
        if (user != null) {
            String currentPassword = (String) user.get("password");
            if (currentPassword != null && currentPassword.equals(oldPassword)) {
                return userDAO.updateUser(userId, (String) user.get("username"), newPassword);
            } else {
                logger.warn("Неверный старый пароль для пользователя с ID: {}", userId);
                return false;
            }
        }
        logger.warn("Пользователь с ID {} не найден для смены пароля", userId);
        return false;
    }

    public boolean resetPassword(Long userId, String newPassword) {
        logger.info("Сброс пароля для пользователя с ID: {}", userId);
        Map<String, Object> user = userDAO.findById(userId);
        if (user != null) {
            return userDAO.updateUser(userId, (String) user.get("username"), newPassword);
        }
        logger.warn("Пользователь с ID {} не найден для сброса пароля", userId);
        return false;
    }

    public List<Map<String, Object>> searchUsers(String searchTerm) {
        logger.debug("Поиск пользователей по термину: {}", searchTerm);
        return userDAO.findByUsernameLike(searchTerm);
    }
}