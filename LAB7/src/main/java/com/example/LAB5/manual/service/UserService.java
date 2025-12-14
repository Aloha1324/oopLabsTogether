package com.example.LAB5.manual.service;

import com.example.LAB5.manual.DAO.UserDAO;
import com.example.LAB5.manual.DTO.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserDAO userDao;

    public UserService() {
        this(new UserDAO());
    }

    public UserService(UserDAO userDao) {
        this.userDao = userDao;
    }

    public Long createUser(String login, String role, String password) {
        log.info("Создание пользователя: login={}, role={}", login, role);
        UserDTO dto = new UserDTO(login, role, password);
        return userDao.createUser(dto);
    }

    public Optional<UserDTO> getUserById(Long id) {
        log.debug("Поиск пользователя по id {}", id);
        return userDao.findById(id);
    }

    public Optional<UserDTO> getUserByLogin(String login) {
        log.debug("Поиск пользователя по логину '{}'", login);
        return userDao.findByLogin(login);
    }

    public List<UserDTO> getAllUsers() {
        log.debug("Чтение всех пользователей");
        return userDao.findAll();
    }

    public List<UserDTO> getUsersByRole(String role) {
        log.debug("Поиск пользователей по роли '{}'", role);
        return userDao.findByRole(role);
    }

    public boolean updateUser(Long id, String login, String role, String password) {
        log.info("Обновление пользователя id={}", id);

        Optional<UserDTO> current = userDao.findById(id);
        if (current.isEmpty()) {
            log.warn("Пользователь id={} не найден для обновления", id);
            return false;
        }

        UserDTO dto = current.get();
        dto.setLogin(login);
        dto.setRole(role);
        dto.setPassword(password);

        return userDao.updateUser(dto);
    }

    public boolean deleteUser(Long id) {
        log.info("Удаление пользователя id={}", id);
        return userDao.deleteUser(id);
    }

    public boolean validateUserCredentials(String login, String password) {
        log.debug("Проверка логина/пароля для '{}'", login);
        Optional<UserDTO> userOpt = userDao.findByLogin(login);
        return userOpt.isPresent() && password.equals(userOpt.get().getPassword());
    }
}
