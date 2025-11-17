package com.example.lab5.dao;

import com.example.lab5.DatabaseConnection;
import com.example.lab5.entity.Function;
import com.example.lab5.entity.User;
import com.example.lab5.repository.FunctionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FunctionDao implements FunctionRepository {
    private static final Logger logger = LoggerFactory.getLogger(FunctionDao.class);

    @Override
    public Function save(Function function) {
        String sql = "INSERT INTO functions (name, user_id, expression, created_at) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, function.getName());
            statement.setLong(2, function.getUser().getId());
            statement.setString(3, function.getExpression());
            statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating function failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    function.setId(generatedKeys.getLong(1));
                    logger.info("Function created with ID: {} - {}", function.getId(), function.getName());
                } else {
                    throw new SQLException("Creating function failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving function: {}", function.getName(), e);
            throw new RuntimeException("Database error", e);
        }
        return function;
    }

    @Override
    public Optional<Function> findById(Long id) {
        String sql = "SELECT f.*, u.username as user_username FROM functions f " +
                "JOIN users u ON f.user_id = u.id WHERE f.id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Function function = mapResultSetToFunction(resultSet);
                logger.debug("Found function by ID {}: {}", id, function.getName());
                return Optional.of(function);
            }
        } catch (SQLException e) {
            logger.error("Error finding function by ID: {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Function> findByUser(User user) {
        String sql = "SELECT f.*, u.username as user_username FROM functions f " +
                "JOIN users u ON f.user_id = u.id WHERE f.user_id = ? ORDER BY f.created_at DESC";

        return findFunctionsByUserId(user.getId(), sql);
    }

    @Override
    public List<Function> findByUserId(Long userId) {
        String sql = "SELECT f.*, u.username as user_username FROM functions f " +
                "JOIN users u ON f.user_id = u.id WHERE f.user_id = ? ORDER BY f.name";

        return findFunctionsByUserId(userId, sql);
    }

    @Override
    public List<Function> findAll() {
        String sql = "SELECT f.*, u.username as user_username FROM functions f " +
                "JOIN users u ON f.user_id = u.id ORDER BY f.created_at DESC";
        List<Function> functions = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                functions.add(mapResultSetToFunction(resultSet));
            }
            logger.debug("Found {} functions", functions.size());
        } catch (SQLException e) {
            logger.error("Error finding all functions", e);
        }
        return functions;
    }

    @Override
    public boolean update(Function function) {
        String sql = "UPDATE functions SET name = ?, expression = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, function.getName());
            statement.setString(2, function.getExpression());
            statement.setLong(3, function.getId());

            int affectedRows = statement.executeUpdate();
            boolean updated = affectedRows > 0;
            if (updated) {
                logger.info("Function updated: {} (ID: {})", function.getName(), function.getId());
            } else {
                logger.warn("No function found to update with ID: {}", function.getId());
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Error updating function: {}", function.getName(), e);
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM functions WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            int affectedRows = statement.executeUpdate();
            boolean deleted = affectedRows > 0;
            if (deleted) {
                logger.info("Function deleted with ID: {}", id);
            } else {
                logger.warn("No function found to delete with ID: {}", id);
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Error deleting function with ID: {}", id, e);
            return false;
        }
    }

    @Override
    public List<Function> findByNameContaining(String name) {
        String sql = "SELECT f.*, u.username as user_username FROM functions f " +
                "JOIN users u ON f.user_id = u.id WHERE f.name ILIKE ? ORDER BY f.name";
        List<Function> functions = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + name + "%");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                functions.add(mapResultSetToFunction(resultSet));
            }
            logger.debug("Found {} functions containing '{}'", functions.size(), name);
        } catch (SQLException e) {
            logger.error("Error finding functions containing: {}", name, e);
        }
        return functions;
    }

    @Override
    public int countByUser(User user) {
        String sql = "SELECT COUNT(*) as function_count FROM functions WHERE user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, user.getId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt("function_count");
                logger.debug("Counted {} functions for user ID: {}", count, user.getId());
                return count;
            }
        } catch (SQLException e) {
            logger.error("Error counting functions for user ID: {}", user.getId(), e);
        }
        return 0;
    }

    public List<Function> findByExpressionContaining(String expression) {
        String sql = "SELECT f.*, u.username as user_username FROM functions f " +
                "JOIN users u ON f.user_id = u.id WHERE f.expression ILIKE ? ORDER BY f.name";
        List<Function> functions = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + expression + "%");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                functions.add(mapResultSetToFunction(resultSet));
            }
            logger.debug("Found {} functions with expression containing '{}'", functions.size(), expression);
        } catch (SQLException e) {
            logger.error("Error finding functions with expression containing: {}", expression, e);
        }
        return functions;
    }

    public List<Function> findRecentFunctions(int limit) {
        String sql = "SELECT f.*, u.username as user_username FROM functions f " +
                "JOIN users u ON f.user_id = u.id ORDER BY f.created_at DESC LIMIT ?";
        List<Function> functions = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, limit);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                functions.add(mapResultSetToFunction(resultSet));
            }
            logger.debug("Found {} recent functions", functions.size());
        } catch (SQLException e) {
            logger.error("Error finding recent functions", e);
        }
        return functions;
    }

    public boolean existsByNameAndUser(String name, User user) {
        String sql = "SELECT 1 FROM functions WHERE name = ? AND user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setLong(2, user.getId());
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            logger.error("Error checking if function exists: {} for user ID: {}", name, user.getId(), e);
            return false;
        }
    }

    private List<Function> findFunctionsByUserId(Long userId, String sql) {
        List<Function> functions = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                functions.add(mapResultSetToFunction(resultSet));
            }
            logger.debug("Found {} functions for user ID: {}", functions.size(), userId);
        } catch (SQLException e) {
            logger.error("Error finding functions for user ID: {}", userId, e);
        }
        return functions;
    }

    private Function mapResultSetToFunction(ResultSet resultSet) throws SQLException {
        Function function = new Function();
        function.setId(resultSet.getLong("id"));
        function.setName(resultSet.getString("name"));
        function.setExpression(resultSet.getString("expression"));
        function.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());

        // Create User object with basic info
        User user = new User();
        user.setId(resultSet.getLong("user_id"));
        user.setUsername(resultSet.getString("user_username"));
        function.setUser(user);

        return function;
    }
}