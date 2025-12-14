package com.example.LAB5.manual.DAO;

import com.example.LAB5.manual.config.DatabaseConfig;
import com.example.LAB5.manual.DTO.FunctionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FunctionDAO {

    private static final Logger log = LoggerFactory.getLogger(FunctionDAO.class);

    public Long createFunction(FunctionDTO function) {
        final String insertSql =
                "INSERT INTO functions (u_id, name, signature) VALUES (?, ?, ?) RETURNING id";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(insertSql)) {

            ps.setLong(1, function.getUserId());
            ps.setString(2, function.getName());
            ps.setString(3, function.getSignature());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long newId = rs.getLong(1);
                    log.info("Создана функция: id={}, name={}", newId, function.getName());
                    return newId;
                }
            }
            throw new SQLException("Не удалось получить ID созданной функции");
        } catch (SQLException ex) {
            log.error("Ошибка при создании функции с именем {}", function.getName(), ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public Optional<FunctionDTO> findById(Long id) {
        final String query =
                "SELECT id, u_id, name, signature FROM functions WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    FunctionDTO dto = mapRow(rs);
                    log.debug("Найдена функция по id {}: {}", id, dto.getName());
                    return Optional.of(dto);
                }
            }

            log.debug("Функция с id {} не найдена", id);
            return Optional.empty();
        } catch (SQLException ex) {
            log.error("Ошибка при поиске функции по id {}", id, ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public List<FunctionDTO> findByUserId(Long userId) {
        final String query =
                "SELECT id, u_id, name, signature FROM functions WHERE u_id = ? ORDER BY name";

        List<FunctionDTO> list = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

            log.debug("Для пользователя {} найдено функций: {}", userId, list.size());
            return list;
        } catch (SQLException ex) {
            log.error("Ошибка при поиске функций по userId {}", userId, ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public List<FunctionDTO> findByName(String name) {
        final String query =
                "SELECT id, u_id, name, signature FROM functions " +
                        "WHERE name LIKE ? ORDER BY name";

        List<FunctionDTO> list = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, "%" + name + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

            log.debug("Найдено {} функций по шаблону имени '{}'", list.size(), name);
            return list;
        } catch (SQLException ex) {
            log.error("Ошибка при поиске функций по имени {}", name, ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public List<FunctionDTO> findAll() {
        final String query =
                "SELECT id, u_id, name, signature FROM functions ORDER BY u_id, name";

        List<FunctionDTO> list = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

            log.debug("Всего функций в базе: {}", list.size());
            return list;
        } catch (SQLException ex) {
            log.error("Ошибка при получении всех функций", ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public boolean updateFunction(FunctionDTO function) {
        final String updateSql =
                "UPDATE functions SET u_id = ?, name = ?, signature = ? WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(updateSql)) {

            ps.setLong(1, function.getUserId());
            ps.setString(2, function.getName());
            ps.setString(3, function.getSignature());
            ps.setLong(4, function.getId());

            int count = ps.executeUpdate();
            boolean updated = count > 0;

            if (updated) {
                log.info("Обновлена функция с id {}", function.getId());
            } else {
                log.warn("Функция с id {} не найдена для обновления", function.getId());
            }

            return updated;
        } catch (SQLException ex) {
            log.error("Ошибка при обновлении функции с id {}", function.getId(), ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public boolean deleteFunction(Long id) {
        final String deleteSql = "DELETE FROM functions WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(deleteSql)) {

            ps.setLong(1, id);
            int count = ps.executeUpdate();
            boolean removed = count > 0;

            if (removed) {
                log.info("Удалена функция с id {}", id);
            } else {
                log.warn("Функция с id {} не найдена для удаления", id);
            }

            return removed;
        } catch (SQLException ex) {
            log.error("Ошибка при удалении функции с id {}", id, ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public boolean deleteByUserId(Long userId) {
        final String deleteSql = "DELETE FROM functions WHERE u_id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(deleteSql)) {

            ps.setLong(1, userId);
            int count = ps.executeUpdate();
            boolean removed = count > 0;

            if (removed) {
                log.info("Удалено {} функций пользователя с id {}", count, userId);
            } else {
                log.warn("Для пользователя с id {} функции для удаления не найдены", userId);
            }

            return removed;
        } catch (SQLException ex) {
            log.error("Ошибка при удалении функций пользователя с id {}", userId, ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    private FunctionDTO mapRow(ResultSet rs) throws SQLException {
        FunctionDTO dto = new FunctionDTO();
        dto.setId(rs.getLong("id"));
        dto.setUserId(rs.getLong("u_id"));
        dto.setName(rs.getString("name"));
        dto.setSignature(rs.getString("signature"));
        return dto;
    }
}
