package com.example.LAB5.manual.service;

import com.example.LAB5.framework.entity.User;
import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ManualJdbcService {
    private static final Logger logger = LoggerFactory.getLogger(ManualJdbcService.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ManualJdbcService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // === USER OPERATIONS ===

    public Long createUser(String username, String passwordHash) {
        long startTime = System.nanoTime();

        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, username);
                ps.setString(2, passwordHash);
                return ps;
            }, keyHolder);

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            logger.info("MANUAL JDBC: Создан пользователь {} за {} мс", username, durationMs);

            Number key = keyHolder.getKey();
            if (key != null) {
                return key.longValue();
            } else {
                throw new RuntimeException("Не удалось получить сгенерированный ключ");
            }
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка создания пользователя: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public User getUserById(Long id) {
        long startTime = System.nanoTime();

        String sql = "SELECT id, username, password FROM users WHERE id = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password"));
                return u;
            });

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            logger.debug("MANUAL JDBC: Получен пользователь ID {} за {} мс", id, durationMs);

            return user;
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка получения пользователя: {}", e.getMessage());
            return null;
        }
    }

    // === ДОБАВЛЕННЫЕ МЕТОДЫ ДЛЯ ПОЛЬЗОВАТЕЛЕЙ ===

    public List<User> getAllUsers() {
        long startTime = System.nanoTime();

        String sql = "SELECT id, username, password FROM users";

        try {
            List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password"));
                return user;
            });

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            logger.info("MANUAL JDBC: Получено {} пользователей за {} мс", users.size(), durationMs);

            return users;
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка получения всех пользователей: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public User getUserByUsername(String username) {
        long startTime = System.nanoTime();

        String sql = "SELECT id, username, password FROM users WHERE username = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, new Object[]{username}, (rs, rowNum) -> {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password"));
                return u;
            });

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            logger.debug("MANUAL JDBC: Получен пользователь {} за {} мс", username, durationMs);

            return user;
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка получения пользователя по имени: {}", e.getMessage());
            return null;
        }
    }

    // ДОБАВЛЕН НЕДОСТАЮЩИЙ МЕТОД
    public List<User> getUsersByUsername(String usernamePattern) {
        long startTime = System.nanoTime();

        String sql = "SELECT id, username, password FROM users WHERE username LIKE ?";

        try {
            List<User> users = jdbcTemplate.query(sql, new Object[]{"%" + usernamePattern + "%"}, (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password"));
                return user;
            });

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            logger.debug("MANUAL JDBC: Получено {} пользователей по шаблону '{}' за {} мс",
                    users.size(), usernamePattern, durationMs);

            return users;
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка получения пользователей по имени: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean updateUser(Long userId, String newUsername, String newPasswordHash) {
        long startTime = System.nanoTime();

        String sql = "UPDATE users SET username = ?, password = ? WHERE id = ?";

        try {
            int affectedRows = jdbcTemplate.update(sql, newUsername, newPasswordHash, userId);

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            if (affectedRows > 0) {
                logger.debug("MANUAL JDBC: Обновлен пользователь ID {} за {} мс", userId, durationMs);
                return true;
            } else {
                logger.debug("MANUAL JDBC: Пользователь ID {} не найден для обновления", userId);
                return false;
            }
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка обновления пользователя: {}", e.getMessage());
            return false;
        }
    }

    // === FUNCTION OPERATIONS ===

    public Long createFunction(Long userId, String name, String expression) {
        long startTime = System.nanoTime();

        String sql = "INSERT INTO functions (name, expression, user_id, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, name);
                ps.setString(2, expression);
                ps.setLong(3, userId);
                return ps;
            }, keyHolder);

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            logger.info("MANUAL JDBC: Создана функция {} за {} мс", name, durationMs);

            Number key = keyHolder.getKey();
            if (key != null) {
                return key.longValue();
            } else {
                throw new RuntimeException("Не удалось получить сгенерированный ключ для функции");
            }
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка создания функции: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Function getFunctionById(Long id) {
        long startTime = System.nanoTime();

        String sql = "SELECT f.id, f.name, f.expression, f.created_at, f.user_id, u.username " +
                "FROM functions f JOIN users u ON f.user_id = u.id WHERE f.id = ?";

        try {
            Function function = jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {
                Function f = new Function();
                f.setId(rs.getLong("id"));
                f.setName(rs.getString("name"));
                f.setExpression(rs.getString("expression"));
                f.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                User user = new User();
                user.setId(rs.getLong("user_id"));
                user.setUsername(rs.getString("username"));
                f.setUser(user);

                return f;
            });

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            logger.debug("MANUAL JDBC: Получена функция ID {} за {} мс", id, durationMs);

            return function;
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка получения функции: {}", e.getMessage());
            return null;
        }
    }

    // === ДОБАВЛЕННЫЕ МЕТОДЫ ДЛЯ ФУНКЦИЙ ===

    public List<Function> getAllFunctions() {
        long startTime = System.nanoTime();

        String sql = "SELECT f.id, f.name, f.expression, f.created_at, f.user_id, u.username " +
                "FROM functions f JOIN users u ON f.user_id = u.id";

        try {
            List<Function> functions = jdbcTemplate.query(sql, (rs, rowNum) -> {
                Function function = new Function();
                function.setId(rs.getLong("id"));
                function.setName(rs.getString("name"));
                function.setExpression(rs.getString("expression"));
                function.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                User user = new User();
                user.setId(rs.getLong("user_id"));
                user.setUsername(rs.getString("username"));
                function.setUser(user);

                return function;
            });

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            logger.info("MANUAL JDBC: Получено {} функций за {} мс", functions.size(), durationMs);

            return functions;
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка получения всех функций: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Function> getFunctionsByUserId(Long userId) {
        long startTime = System.nanoTime();

        String sql = "SELECT f.id, f.name, f.expression, f.created_at, f.user_id, u.username " +
                "FROM functions f JOIN users u ON f.user_id = u.id WHERE f.user_id = ?";

        try {
            List<Function> functions = jdbcTemplate.query(sql, new Object[]{userId}, (rs, rowNum) -> {
                Function function = new Function();
                function.setId(rs.getLong("id"));
                function.setName(rs.getString("name"));
                function.setExpression(rs.getString("expression"));
                function.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                User user = new User();
                user.setId(rs.getLong("user_id"));
                user.setUsername(rs.getString("username"));
                function.setUser(user);

                return function;
            });

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            logger.debug("MANUAL JDBC: Получено {} функций пользователя ID {} за {} мс",
                    functions.size(), userId, durationMs);

            return functions;
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка получения функций пользователя: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Function> getFunctionsByName(String namePattern) {
        long startTime = System.nanoTime();

        String sql = "SELECT f.id, f.name, f.expression, f.created_at, f.user_id, u.username " +
                "FROM functions f JOIN users u ON f.user_id = u.id WHERE f.name LIKE ?";

        try {
            List<Function> functions = jdbcTemplate.query(sql, new Object[]{"%" + namePattern + "%"}, (rs, rowNum) -> {
                Function function = new Function();
                function.setId(rs.getLong("id"));
                function.setName(rs.getString("name"));
                function.setExpression(rs.getString("expression"));
                function.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                User user = new User();
                user.setId(rs.getLong("user_id"));
                user.setUsername(rs.getString("username"));
                function.setUser(user);

                return function;
            });

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            logger.debug("MANUAL JDBC: Получено {} функций по шаблону '{}' за {} мс",
                    functions.size(), namePattern, durationMs);

            return functions;
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка получения функций по имени: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean updateFunction(Long functionId, String newName, String newExpression) {
        long startTime = System.nanoTime();

        String sql = "UPDATE functions SET name = ?, expression = ? WHERE id = ?";

        try {
            int affectedRows = jdbcTemplate.update(sql, newName, newExpression, functionId);

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            if (affectedRows > 0) {
                logger.debug("MANUAL JDBC: Обновлена функция ID {} за {} мс", functionId, durationMs);
                return true;
            } else {
                logger.debug("MANUAL JDBC: Функция ID {} не найдена для обновления", functionId);
                return false;
            }
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка обновления функции: {}", e.getMessage());
            return false;
        }
    }

    public boolean deleteFunction(Long functionId) {
        long startTime = System.nanoTime();

        try {
            jdbcTemplate.update("DELETE FROM points WHERE function_id = ?", functionId);
            int affectedRows = jdbcTemplate.update("DELETE FROM functions WHERE id = ?", functionId);

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            if (affectedRows > 0) {
                logger.debug("MANUAL JDBC: Удалена функция ID {} за {} мс", functionId, durationMs);
                return true;
            } else {
                logger.debug("MANUAL JDBC: Функция ID {} не найдена для удаления", functionId);
                return false;
            }
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка удаления функции: {}", e.getMessage());
            return false;
        }
    }

    // === POINT OPERATIONS ===

    public Long createPoint(Long functionId, Long userId, Double x, Double y) {
        long startTime = System.nanoTime();

        String sql = "INSERT INTO points (x_value, y_value, function_id, user_id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setDouble(1, x);
                ps.setDouble(2, y);
                ps.setLong(3, functionId);
                ps.setLong(4, userId);
                return ps;
            }, keyHolder);

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            logger.debug("MANUAL JDBC: Создана точка за {} мс", durationMs);

            Number key = keyHolder.getKey();
            if (key != null) {
                return key.longValue();
            } else {
                throw new RuntimeException("Не удалось получить сгенерированный ключ для точки");
            }
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка создания точки: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Point> getPointsByFunctionId(Long functionId) {
        long startTime = System.nanoTime();

        String sql = "SELECT id, x_value, y_value, function_id, user_id FROM points WHERE function_id = ?";

        try {
            List<Point> points = jdbcTemplate.query(sql, new Object[]{functionId}, (rs, rowNum) -> {
                Point point = new Point();
                point.setId(rs.getLong("id"));
                point.setXValue(rs.getDouble("x_value"));
                point.setYValue(rs.getDouble("y_value"));

                Function function = new Function();
                function.setId(rs.getLong("function_id"));
                point.setFunction(function);

                User user = new User();
                user.setId(rs.getLong("user_id"));
                point.setUser(user);

                return point;
            });

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            logger.debug("MANUAL JDBC: Получено {} точек функции ID {} за {} мс",
                    points.size(), functionId, durationMs);

            return points;
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка получения точек: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Point> getAllPoints() {
        long startTime = System.nanoTime();

        String sql = "SELECT id, x_value, y_value, function_id, user_id FROM points";

        try {
            List<Point> points = jdbcTemplate.query(sql, (rs, rowNum) -> {
                Point point = new Point();
                point.setId(rs.getLong("id"));
                point.setXValue(rs.getDouble("x_value"));
                point.setYValue(rs.getDouble("y_value"));

                Function function = new Function();
                function.setId(rs.getLong("function_id"));
                point.setFunction(function);

                User user = new User();
                user.setId(rs.getLong("user_id"));
                point.setUser(user);

                return point;
            });

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            logger.info("MANUAL JDBC: Получено {} точек за {} мс", points.size(), durationMs);

            return points;
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка получения всех точек: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // === BATCH OPERATIONS ===

    public int createPointsBatch(Long functionId, Long userId, List<Double> xValues, List<Double> yValues) {
        long startTime = System.nanoTime();

        String sql = "INSERT INTO points (x_value, y_value, function_id, user_id) VALUES (?, ?, ?, ?)";

        try {
            List<Object[]> batchArgs = new ArrayList<>();
            for (int i = 0; i < xValues.size(); i++) {
                batchArgs.add(new Object[]{xValues.get(i), yValues.get(i), functionId, userId});
            }

            int[] results = jdbcTemplate.batchUpdate(sql, batchArgs);
            int totalCreated = results.length;

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            logger.info("MANUAL JDBC: Массово создано {} точек за {} мс ({} точек/сек)",
                    totalCreated, durationMs, (totalCreated * 1000.0) / durationMs);

            return totalCreated;
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка массового создания точек: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // === UTILITY METHODS ===

    public int countAllUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public int countAllFunctions() {
        String sql = "SELECT COUNT(*) FROM functions";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public int countAllPoints() {
        String sql = "SELECT COUNT(*) FROM points";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public void deleteUser(Long userId) {
        try {
            jdbcTemplate.update("DELETE FROM points WHERE user_id = ?", userId);
            jdbcTemplate.update("DELETE FROM functions WHERE user_id = ?", userId);
            jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);

            logger.debug("MANUAL JDBC: Удален пользователь ID {} и связанные данные", userId);
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка удаления пользователя: {}", e.getMessage());
        }
    }

    // ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С ТОЧКАМИ

    public Point getPointById(Long pointId) {
        long startTime = System.nanoTime();

        String sql = "SELECT id, x_value, y_value, function_id, user_id FROM points WHERE id = ?";

        try {
            Point point = jdbcTemplate.queryForObject(sql, new Object[]{pointId}, (rs, rowNum) -> {
                Point p = new Point();
                p.setId(rs.getLong("id"));
                p.setXValue(rs.getDouble("x_value"));
                p.setYValue(rs.getDouble("y_value"));

                Function function = new Function();
                function.setId(rs.getLong("function_id"));
                p.setFunction(function);

                User user = new User();
                user.setId(rs.getLong("user_id"));
                p.setUser(user);

                return p;
            });

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            logger.debug("MANUAL JDBC: Получена точка ID {} за {} мс", pointId, durationMs);

            return point;
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка получения точки: {}", e.getMessage());
            return null;
        }
    }

    public boolean updatePoint(Long pointId, Double newX, Double newY) {
        long startTime = System.nanoTime();

        String sql = "UPDATE points SET x_value = ?, y_value = ? WHERE id = ?";

        try {
            int affectedRows = jdbcTemplate.update(sql, newX, newY, pointId);

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            if (affectedRows > 0) {
                logger.debug("MANUAL JDBC: Обновлена точка ID {} за {} мс", pointId, durationMs);
                return true;
            } else {
                logger.debug("MANUAL JDBC: Точка ID {} не найдена для обновления", pointId);
                return false;
            }
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка обновления точки: {}", e.getMessage());
            return false;
        }
    }

    public boolean deletePoint(Long pointId) {
        long startTime = System.nanoTime();

        String sql = "DELETE FROM points WHERE id = ?";

        try {
            int affectedRows = jdbcTemplate.update(sql, pointId);

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            if (affectedRows > 0) {
                logger.debug("MANUAL JDBC: Удалена точка ID {} за {} мс", pointId, durationMs);
                return true;
            } else {
                logger.debug("MANUAL JDBC: Точка ID {} не найдена для удаления", pointId);
                return false;
            }
        } catch (Exception e) {
            logger.error("MANUAL JDBC: Ошибка удаления точки: {}", e.getMessage());
            return false;
        }
    }
}