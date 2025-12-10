package com.example.LAB5.manual.DAO;

import com.example.LAB5.manual.config.DatabaseConfig;
import com.example.LAB5.manual.DTO.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    private static final Logger log = LoggerFactory.getLogger(UserDAO.class);

    public Long createUser(UserDTO user) {
        final String insertSql =
                "INSERT INTO users (login, role, password) VALUES (?, ?, ?) RETURNING id";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(insertSql)) {

            ps.setString(1, user.getLogin());
            ps.setString(2, user.getRole());
            ps.setString(3, user.getPassword());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long generatedId = rs.getLong(1);
                    log.info("Пользователь создан: id={}, login={}", generatedId, user.getLogin());
                    return generatedId;
                }
            }

            throw new SQLException("Не удалось получить идентификатор созданного пользователя");
        } catch (SQLException ex) {
            log.error("Ошибка при создании пользователя с логином {}", user.getLogin(), ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public Optional<UserDTO> findById(Long id) {
        final String query =
                "SELECT id, login, role, password FROM users WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserDTO user = toUser(rs);
                    log.debug("Получен пользователь по id {}: {}", id, user.getLogin());
                    return Optional.of(user);
                }
            }

            log.debug("Пользователь с id {} отсутствует в базе", id);
            return Optional.empty();
        } catch (SQLException ex) {
            log.error("Ошибка при поиске пользователя по id {}", id, ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public Optional<UserDTO> findByLogin(String login) {
        final String query =
                "SELECT id, login, role, password FROM users WHERE login = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, login);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserDTO user = toUser(rs);
                    log.debug("Найден пользователь с логином {}", login);
                    return Optional.of(user);
                }
            }

            log.debug("Пользователь с логином {} не найден", login);
            return Optional.empty();
        } catch (SQLException ex) {
            log.error("Ошибка при поиске пользователя по логину {}", login, ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public List<UserDTO> findAll() {
        final String query =
                "SELECT id, login, role, password FROM users ORDER BY id";

        List<UserDTO> result = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(toUser(rs));
            }

            log.debug("Из базы загружено {} пользователей", result.size());
            return result;
        } catch (SQLException ex) {
            log.error("Ошибка при получении списка всех пользователей", ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public List<UserDTO> findByRole(String role) {
        final String query =
                "SELECT id, login, role, password FROM users WHERE role = ? ORDER BY login";

        List<UserDTO> result = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, role);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(toUser(rs));
                }
            }

            log.debug("Найдено {} пользователей с ролью {}", result.size(), role);
            return result;
        } catch (SQLException ex) {
            log.error("Ошибка при поиске пользователей по роли {}", role, ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public boolean updateUser(UserDTO user) {
        final String updateSql =
                "UPDATE users SET login = ?, role = ?, password = ? WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(updateSql)) {

            ps.setString(1, user.getLogin());
            ps.setString(2, user.getRole());
            ps.setString(3, user.getPassword());
            ps.setLong(4, user.getId());

            int rows = ps.executeUpdate();
            boolean updated = rows > 0;

            if (updated) {
                log.info("Данные пользователя с id {} обновлены", user.getId());
            } else {
                log.warn("Пользователь с id {} не найден для обновления", user.getId());
            }

            return updated;
        } catch (SQLException ex) {
            log.error("Ошибка при обновлении пользователя с id {}", user.getId(), ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public boolean deleteUser(Long id) {
        final String deleteSql = "DELETE FROM users WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(deleteSql)) {

            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            boolean removed = rows > 0;

            if (removed) {
                log.info("Пользователь с id {} удален", id);
            } else {
                log.warn("Пользователь с id {} не найден для удаления", id);
            }

            return removed;
        } catch (SQLException ex) {
            log.error("Ошибка при удалении пользователя с id {}", id, ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    private UserDTO toUser(ResultSet rs) throws SQLException {
        UserDTO dto = new UserDTO();
        dto.setId(rs.getLong("id"));
        dto.setLogin(rs.getString("login"));
        dto.setRole(rs.getString("role"));
        dto.setPassword(rs.getString("password"));
        return dto;
    }
}
