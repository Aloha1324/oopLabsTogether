package com.example.LAB5.manual.DAO;

import com.example.LAB5.manual.config.DatabaseConfig;
import com.example.LAB5.manual.DTO.PointDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PointDAO {

    private static final Logger log = LoggerFactory.getLogger(PointDAO.class);

    public Long createPoint(PointDTO point) {
        final String insertSql =
                "INSERT INTO points (f_id, x_value, y_value) VALUES (?, ?, ?) RETURNING id";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(insertSql)) {

            ps.setLong(1, point.getFunctionId());
            ps.setDouble(2, point.getXValue());
            ps.setDouble(3, point.getYValue());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    log.debug("Создана точка id={}, f_id={}, x={}", id,
                            point.getFunctionId(), point.getXValue());
                    return id;
                }
            }

            throw new SQLException("Не удалось получить ID созданной точки");
        } catch (SQLException ex) {
            log.error("Ошибка при создании точки: f_id={}, x={}, y={}",
                    point.getFunctionId(), point.getXValue(), point.getYValue(), ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public int createPoints(List<PointDTO> points) {
        final String batchSql =
                "INSERT INTO points (f_id, x_value, y_value) VALUES (?, ?, ?)";

        int createdCount = 0;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(batchSql)) {

            for (PointDTO p : points) {
                ps.setLong(1, p.getFunctionId());
                ps.setDouble(2, p.getXValue());
                ps.setDouble(3, p.getYValue());
                ps.addBatch();
            }

            int[] result = ps.executeBatch();
            createdCount = result.length;
            log.info("Создано точек: {}", createdCount);
            return createdCount;
        } catch (SQLException ex) {
            log.error("Ошибка при пакетном создании точек", ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public Optional<PointDTO> findById(Long id) {
        final String query =
                "SELECT id, f_id, x_value, y_value FROM points WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

            log.debug("Точка с id {} не найдена", id);
            return Optional.empty();
        } catch (SQLException ex) {
            log.error("Ошибка при поиске точки по id {}", id, ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public List<PointDTO> findByFunctionId(Long functionId) {
        final String query =
                "SELECT id, f_id, x_value, y_value FROM points " +
                        "WHERE f_id = ? ORDER BY x_value";

        List<PointDTO> list = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setLong(1, functionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

            log.debug("Для функции {} найдено точек: {}", functionId, list.size());
            return list;
        } catch (SQLException ex) {
            log.error("Ошибка при поиске точек по functionId {}", functionId, ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public List<PointDTO> findByXRange(Long functionId, Double minX, Double maxX) {
        final String query =
                "SELECT id, f_id, x_value, y_value FROM points " +
                        "WHERE f_id = ? AND x_value BETWEEN ? AND ? ORDER BY x_value";

        List<PointDTO> list = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setLong(1, functionId);
            ps.setDouble(2, minX);
            ps.setDouble(3, maxX);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

            log.debug("Найдено {} точек в диапазоне x=[{}, {}] для функции {}",
                    list.size(), minX, maxX, functionId);
            return list;
        } catch (SQLException ex) {
            log.error("Ошибка при поиске точек по диапазону X {} - {}", minX, maxX, ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public List<PointDTO> findByYRange(Long functionId, Double minY, Double maxY) {
        final String query =
                "SELECT id, f_id, x_value, y_value FROM points " +
                        "WHERE f_id = ? AND y_value BETWEEN ? AND ? ORDER BY y_value";

        List<PointDTO> list = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setLong(1, functionId);
            ps.setDouble(2, minY);
            ps.setDouble(3, maxY);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

            log.debug("Найдено {} точек в диапазоне y=[{}, {}] для функции {}",
                    list.size(), minY, maxY, functionId);
            return list;
        } catch (SQLException ex) {
            log.error("Ошибка при поиске точек по диапазону Y {} - {}", minY, maxY, ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public List<PointDTO> findAll() {
        final String query =
                "SELECT id, f_id, x_value, y_value FROM points ORDER BY f_id, x_value";

        List<PointDTO> list = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

            log.debug("Всего точек в базе: {}", list.size());
            return list;
        } catch (SQLException ex) {
            log.error("Ошибка при получении всех точек", ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public boolean updatePoint(PointDTO point) {
        final String updateSql =
                "UPDATE points SET f_id = ?, x_value = ?, y_value = ? WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(updateSql)) {

            ps.setLong(1, point.getFunctionId());
            ps.setDouble(2, point.getXValue());
            ps.setDouble(3, point.getYValue());
            ps.setLong(4, point.getId());

            int count = ps.executeUpdate();
            boolean updated = count > 0;

            if (updated) {
                log.info("Обновлена точка с id {}", point.getId());
            } else {
                log.warn("Точка с id {} не найдена для обновления", point.getId());
            }

            return updated;
        } catch (SQLException ex) {
            log.error("Ошибка при обновлении точки с id {}", point.getId(), ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public boolean deletePoint(Long id) {
        final String deleteSql = "DELETE FROM points WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(deleteSql)) {

            ps.setLong(1, id);
            int count = ps.executeUpdate();
            boolean removed = count > 0;

            if (removed) {
                log.info("Удалена точка с id {}", id);
            } else {
                log.warn("Точка с id {} не найдена для удаления", id);
            }

            return removed;
        } catch (SQLException ex) {
            log.error("Ошибка при удалении точки с id {}", id, ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    public int deleteByFunctionId(Long functionId) {
        final String deleteSql = "DELETE FROM points WHERE f_id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(deleteSql)) {

            ps.setLong(1, functionId);
            int affected = ps.executeUpdate();

            log.info("Для функции {} удалено точек: {}", functionId, affected);
            return affected;
        } catch (SQLException ex) {
            log.error("Ошибка при удалении точек для функции {}", functionId, ex);
            throw new RuntimeException("Database error", ex);
        }
    }

    private PointDTO mapRow(ResultSet rs) throws SQLException {
        PointDTO dto = new PointDTO();
        dto.setId(rs.getLong("id"));
        dto.setFunctionId(rs.getLong("f_id"));
        dto.setXValue(rs.getDouble("x_value"));
        dto.setYValue(rs.getDouble("y_value"));
        return dto;
    }
}
