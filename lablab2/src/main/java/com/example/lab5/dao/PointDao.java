package com.example.lab5.dao;

import com.example.lab5.DatabaseConnection;
import com.example.lab5.entity.Function;
import com.example.lab5.entity.Point;
import com.example.lab5.repository.PointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PointDao implements PointRepository {
    private static final Logger logger = LoggerFactory.getLogger(PointDao.class);

    @Override
    public Point save(Point point) {
        String sql = "INSERT INTO points (function_id, x_value, y_value, created_at) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, point.getFunction().getId());
            statement.setDouble(2, point.getXValue());
            statement.setDouble(3, point.getYValue());
            statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating point failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    point.setId(generatedKeys.getLong(1));
                    logger.info("Point created with ID: {} (x={}, y={})", point.getId(), point.getXValue(), point.getYValue());
                } else {
                    throw new SQLException("Creating point failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving point: x={}, y={}", point.getXValue(), point.getYValue(), e);
            throw new RuntimeException("Database error", e);
        }
        return point;
    }

    @Override
    public Optional<Point> findById(Long id) {
        String sql = "SELECT p.*, f.name as function_name FROM points p " +
                "JOIN functions f ON p.function_id = f.id WHERE p.id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Point point = mapResultSetToPoint(resultSet);
                logger.debug("Found point by ID {}: (x={}, y={})", id, point.getXValue(), point.getYValue());
                return Optional.of(point);
            }
        } catch (SQLException e) {
            logger.error("Error finding point by ID: {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Point> findByFunction(Function function) {
        String sql = "SELECT p.*, f.name as function_name FROM points p " +
                "JOIN functions f ON p.function_id = f.id WHERE p.function_id = ? ORDER BY p.x_value";

        return findPointsByFunctionId(function.getId(), sql);
    }

    @Override
    public List<Point> findByFunctionId(Long functionId) {
        String sql = "SELECT p.*, f.name as function_name FROM points p " +
                "JOIN functions f ON p.function_id = f.id WHERE p.function_id = ? ORDER BY p.x_value";

        return findPointsByFunctionId(functionId, sql);
    }

    @Override
    public List<Point> findAll() {
        String sql = "SELECT p.*, f.name as function_name FROM points p " +
                "JOIN functions f ON p.function_id = f.id ORDER BY p.function_id, p.x_value";
        List<Point> points = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                points.add(mapResultSetToPoint(resultSet));
            }
            logger.debug("Found {} points", points.size());
        } catch (SQLException e) {
            logger.error("Error finding all points", e);
        }
        return points;
    }

    @Override
    public boolean update(Point point) {
        String sql = "UPDATE points SET x_value = ?, y_value = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, point.getXValue());
            statement.setDouble(2, point.getYValue());
            statement.setLong(3, point.getId());

            int affectedRows = statement.executeUpdate();
            boolean updated = affectedRows > 0;
            if (updated) {
                logger.info("Point updated: ID {} (x={}, y={})", point.getId(), point.getXValue(), point.getYValue());
            } else {
                logger.warn("No point found to update with ID: {}", point.getId());
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Error updating point ID: {}", point.getId(), e);
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM points WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            int affectedRows = statement.executeUpdate();
            boolean deleted = affectedRows > 0;
            if (deleted) {
                logger.info("Point deleted with ID: {}", id);
            } else {
                logger.warn("No point found to delete with ID: {}", id);
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Error deleting point with ID: {}", id, e);
            return false;
        }
    }

    @Override
    public boolean deleteByFunction(Function function) {
        String sql = "DELETE FROM points WHERE function_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, function.getId());
            int affectedRows = statement.executeUpdate();
            boolean deleted = affectedRows > 0;
            if (deleted) {
                logger.info("Deleted {} points for function ID: {}", affectedRows, function.getId());
            } else {
                logger.info("No points found to delete for function ID: {}", function.getId());
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Error deleting points for function ID: {}", function.getId(), e);
            return false;
        }
    }

    @Override
    public List<Point> findByXValueBetween(Double minX, Double maxX) {
        String sql = "SELECT p.*, f.name as function_name FROM points p " +
                "JOIN functions f ON p.function_id = f.id WHERE p.x_value BETWEEN ? AND ? ORDER BY p.x_value";
        List<Point> points = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, minX);
            statement.setDouble(2, maxX);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                points.add(mapResultSetToPoint(resultSet));
            }
            logger.debug("Found {} points between x={} and x={}", points.size(), minX, maxX);
        } catch (SQLException e) {
            logger.error("Error finding points between x={} and x={}", minX, maxX, e);
        }
        return points;
    }

    @Override
    public List<Point> findByYValueGreaterThan(Double yValue) {
        String sql = "SELECT p.*, f.name as function_name FROM points p " +
                "JOIN functions f ON p.function_id = f.id WHERE p.y_value > ? ORDER BY p.y_value DESC";
        List<Point> points = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, yValue);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                points.add(mapResultSetToPoint(resultSet));
            }
            logger.debug("Found {} points with y > {}", points.size(), yValue);
        } catch (SQLException e) {
            logger.error("Error finding points with y > {}", yValue, e);
        }
        return points;
    }

    @Override
    public int countByFunction(Function function) {
        String sql = "SELECT COUNT(*) as point_count FROM points WHERE function_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, function.getId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt("point_count");
                logger.debug("Counted {} points for function ID: {}", count, function.getId());
                return count;
            }
        } catch (SQLException e) {
            logger.error("Error counting points for function ID: {}", function.getId(), e);
        }
        return 0;
    }

    public List<Point> findByYValueLessThan(Double yValue) {
        String sql = "SELECT p.*, f.name as function_name FROM points p " +
                "JOIN functions f ON p.function_id = f.id WHERE p.y_value < ? ORDER BY p.y_value";
        List<Point> points = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, yValue);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                points.add(mapResultSetToPoint(resultSet));
            }
            logger.debug("Found {} points with y < {}", points.size(), yValue);
        } catch (SQLException e) {
            logger.error("Error finding points with y < {}", yValue, e);
        }
        return points;
    }

    public List<Point> findPointsInRange(Double minX, Double maxX, Double minY, Double maxY) {
        String sql = "SELECT p.*, f.name as function_name FROM points p " +
                "JOIN functions f ON p.function_id = f.id " +
                "WHERE p.x_value BETWEEN ? AND ? AND p.y_value BETWEEN ? AND ? " +
                "ORDER BY p.x_value";
        List<Point> points = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, minX);
            statement.setDouble(2, maxX);
            statement.setDouble(3, minY);
            statement.setDouble(4, maxY);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                points.add(mapResultSetToPoint(resultSet));
            }
            logger.debug("Found {} points in range x[{},{}] y[{},{}]", points.size(), minX, maxX, minY, maxY);
        } catch (SQLException e) {
            logger.error("Error finding points in range", e);
        }
        return points;
    }

    public Double findAverageYByFunction(Function function) {
        String sql = "SELECT AVG(y_value) as avg_y FROM points WHERE function_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, function.getId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble("avg_y");
            }
        } catch (SQLException e) {
            logger.error("Error calculating average Y for function ID: {}", function.getId(), e);
        }
        return null;
    }

    private List<Point> findPointsByFunctionId(Long functionId, String sql) {
        List<Point> points = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, functionId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                points.add(mapResultSetToPoint(resultSet));
            }
            logger.debug("Found {} points for function ID: {}", points.size(), functionId);
        } catch (SQLException e) {
            logger.error("Error finding points for function ID: {}", functionId, e);
        }
        return points;
    }

    private Point mapResultSetToPoint(ResultSet resultSet) throws SQLException {
        Point point = new Point();
        point.setId(resultSet.getLong("id"));
        point.setXValue(resultSet.getDouble("x_value"));
        point.setYValue(resultSet.getDouble("y_value"));
        point.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());

        // Create Function object with basic info
        Function function = new Function();
        function.setId(resultSet.getLong("function_id"));
        function.setName(resultSet.getString("function_name"));
        point.setFunction(function);

        return point;
    }
}