package com.example.LAB5.manual.DAO;

import com.example.LAB5.manual.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    // Внедрение зависимостей для безопасного удаления
    private FunctionDAO functionDAO;
    private PointDAO pointDAO;

    public UserDAO() {
        this.functionDAO = new FunctionDAO();
        this.pointDAO = new PointDAO();
    }

    // Для тестирования можно добавить конструктор с внедрением зависимостей
    public UserDAO(FunctionDAO functionDAO, PointDAO pointDAO) {
        this.functionDAO = functionDAO;
        this.pointDAO = pointDAO;
    }

    // CREATE
    public Long createUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Создание пользователя не удалось");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    logger.info("Создан пользователь с ID: {}, username: {}", id, username);
                    return id;
                } else {
                    throw new SQLException("Создание пользователя не удалось, ID не получен");
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при создании пользователя: {}", username, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public Long createUserWithEmail(String username, String password, String email) {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Создание пользователя не удалось");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    logger.info("Создан пользователь с ID: {}, username: {}, email: {}", id, username, email);
                    return id;
                } else {
                    throw new SQLException("Создание пользователя не удалось, ID не получен");
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при создании пользователя: {}", username, e);
            throw new RuntimeException("Database error", e);
        }
    }

    // READ
    public List<Map<String, Object>> findAll() {
        String sql = "SELECT * FROM users";
        List<Map<String, Object>> users = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToMap(rs));
            }
            logger.debug("Найдено {} пользователей", users.size());
            return users;
        } catch (SQLException e) {
            logger.error("Ошибка при получении всех пользователей", e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> getAllUsersLimited(int limit) {
        List<Map<String, Object>> users = new ArrayList<>();
        String sql = "SELECT * FROM users LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", rs.getLong("id"));
                    user.put("username", rs.getString("username"));
                    user.put("password", rs.getString("password"));
                    user.put("email", rs.getString("email"));
                    user.put("created_at", rs.getTimestamp("created_at"));
                    user.put("updated_at", rs.getTimestamp("updated_at"));
                    users.add(user);
                }
            }
            logger.debug("Получено {} пользователей с ограничением LIMIT {}", users.size(), limit);
        } catch (SQLException e) {
            logger.error("Ошибка при получении пользователей с ограничением: {}", e.getMessage());
        }
        return users;
    }

    public Map<String, Object> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> user = mapResultSetToMap(rs);
                logger.debug("Найден пользователь по ID {}: {}", id, user.get("username"));
                return user;
            }
            logger.debug("Пользователь с ID {} не найден", id);
            return null;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске пользователя по ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public Map<String, Object> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> user = mapResultSetToMap(rs);
                logger.debug("Найден пользователь по username: {}", username);
                return user;
            }
            logger.debug("Пользователь с username {} не найден", username);
            return null;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске пользователя по username: {}", username, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> findByUsernameLike(String usernamePattern) {
        String sql = "SELECT * FROM users WHERE username LIKE ?";
        List<Map<String, Object>> users = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + usernamePattern + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToMap(rs));
            }
            logger.debug("Найдено {} пользователей с username содержащим '{}'", users.size(), usernamePattern);
            return users;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске пользователей по username pattern: {}", usernamePattern, e);
            throw new RuntimeException("Database error", e);
        }
    }

    // ДОБАВЛЕННЫЙ МЕТОД
    public List<Map<String, Object>> findByRole(String role) {
        String sql = "SELECT * FROM users WHERE role = ?";
        List<Map<String, Object>> users = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToMap(rs));
            }
            logger.debug("Найдено {} пользователей с ролью: {}", users.size(), role);
            return users;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске пользователей по роли: {}", role, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) as count FROM users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            return false;
        } catch (SQLException e) {
            logger.error("Ошибка при проверке существования пользователя: {}", username, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public int getTotalUsersCount() {
        String sql = "SELECT COUNT(*) as total_users FROM users";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int count = rs.getInt("total_users");
                logger.debug("Общее количество пользователей: {}", count);
                return count;
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Ошибка при подсчете пользователей", e);
            throw new RuntimeException("Database error", e);
        }
    }

    // UPDATE
    public boolean updateUser(Long id, String username, String password) {
        String sql = "UPDATE users SET username = ?, password = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setLong(3, id);

            int affectedRows = stmt.executeUpdate();
            boolean updated = affectedRows > 0;

            if (updated) {
                logger.info("Обновлен пользователь с ID: {}, новый username: {}", id, username);
            } else {
                logger.warn("Пользователь с ID {} не найден для обновления", id);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении пользователя с ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public boolean updateUserEmail(Long id, String email) {
        String sql = "UPDATE users SET email = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setLong(2, id);

            int affectedRows = stmt.executeUpdate();
            boolean updated = affectedRows > 0;

            if (updated) {
                logger.info("Обновлен email пользователя с ID: {}", id);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении email пользователя с ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    // DELETE - БЕЗОПАСНЫЕ МЕТОДЫ

    /**
     * Безопасное удаление пользователя с предварительным удалением всех связанных данных
     * @param id ID пользователя для удаления
     * @return true если удаление успешно, false если пользователь не найден
     */
    public boolean deleteUser(Long id) {
        try {
            // Сначала проверяем существование пользователя
            Map<String, Object> user = findById(id);
            if (user == null) {
                logger.warn("Пользователь с ID {} не найден для удаления", id);
                return false;
            }

            String username = (String) user.get("username");

            // 1. Сначала удаляем все точки пользователя
            int pointsDeleted = pointDAO.deletePointsByUserId(id);
            logger.debug("Удалено {} точек пользователя '{}' (ID: {})", pointsDeleted, username, id);

            // 2. Затем удаляем все функции пользователя
            int functionsDeleted = functionDAO.deleteFunctionsByUserId(id);
            logger.debug("Удалено {} функций пользователя '{}' (ID: {})", functionsDeleted, username, id);

            // 3. В конце удаляем самого пользователя
            String sql = "DELETE FROM users WHERE id = ?";

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, id);
                int affectedRows = stmt.executeUpdate();
                boolean deleted = affectedRows > 0;

                if (deleted) {
                    logger.info("Удален пользователь '{}' с ID: {} (предварительно удалено {} точек и {} функций)",
                            username, id, pointsDeleted, functionsDeleted);
                } else {
                    logger.warn("Пользователь с ID {} не был удален", id);
                }
                return deleted;
            }
        } catch (SQLException e) {
            logger.error("Ошибка при удалении пользователя с ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Безопасное удаление пользователя по имени пользователя
     * @param username имя пользователя
     * @return true если удаление успешно, false если пользователь не найден
     */
    public boolean deleteUserByUsername(String username) {
        try {
            // Сначала находим пользователя по имени
            Map<String, Object> user = findByUsername(username);
            if (user == null) {
                logger.warn("Пользователь с username '{}' не найден для удаления", username);
                return false;
            }

            Long userId = (Long) user.get("id");

            // Используем безопасное удаление по ID
            return deleteUser(userId);

        } catch (Exception e) {
            logger.error("Ошибка при удалении пользователя с username: {}", username, e);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Безопасное удаление пользователей по префиксу имени пользователя
     * Используется для очистки тестовых данных
     * @param prefix префикс имени пользователя
     * @return количество удаленных пользователей
     */
    public int deleteUsersByUsernamePrefix(String prefix) {
        try {
            // Сначала находим всех пользователей с данным префиксом
            List<Map<String, Object>> usersToDelete = findByUsernameLike(prefix);
            if (usersToDelete.isEmpty()) {
                logger.debug("Нет пользователей для удаления с префиксом: {}", prefix);
                return 0;
            }

            int totalDeleted = 0;

            // Безопасно удаляем каждого пользователя
            for (Map<String, Object> user : usersToDelete) {
                Long userId = (Long) user.get("id");
                String username = (String) user.get("username");

                if (deleteUser(userId)) {
                    totalDeleted++;
                    logger.debug("Успешно удален пользователь: {}", username);
                } else {
                    logger.warn("Не удалось удалить пользователя: {}", username);
                }
            }

            logger.info("Удалено {} пользователей с префиксом: {}", totalDeleted, prefix);
            return totalDeleted;

        } catch (Exception e) {
            logger.error("Ошибка при удалении пользователей по префиксу: {}", prefix, e);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Безопасное удаление пользователей по префиксу с проверкой
     * @param prefix префикс имени пользователя
     * @return количество удаленных пользователей, -1 в случае ошибки
     */
    public int safeDeleteUsersByUsernamePrefix(String prefix) {
        try {
            // Проверяем существование пользователей
            int countToDelete = getUsersCountByUsernamePrefix(prefix);
            if (countToDelete == 0) {
                logger.debug("Нет пользователей для удаления с префиксом: {}", prefix);
                return 0;
            }

            logger.debug("Найдено {} пользователей для удаления с префиксом: {}", countToDelete, prefix);

            // Выполняем удаление
            int deletedCount = deleteUsersByUsernamePrefix(prefix);

            if (deletedCount == countToDelete) {
                logger.info("Успешно удалено {} пользователей по префиксу: {}", deletedCount, prefix);
            } else {
                logger.warn("Удалено {} из {} пользователей по префиксу: {}",
                        deletedCount, countToDelete, prefix);
            }

            return deletedCount;

        } catch (Exception e) {
            logger.error("Ошибка при безопасном удалении пользователей по префиксу: {}", prefix, e);
            return -1;
        }
    }

    /**
     * Удаление пользователей по префиксу теста (удобный алиас)
     * @param testPrefix префикс теста
     * @return количество удаленных пользователей
     */
    public int deleteUsersByTestPrefix(String testPrefix) {
        return deleteUsersByUsernamePrefix(testPrefix);
    }

    /**
     * Полное удаление всех тестовых данных по префиксу
     * @param prefix префикс теста
     * @return Map с результатами удаления
     */
    public Map<String, Integer> deleteAllTestDataByPrefix(String prefix) {
        Map<String, Integer> results = new HashMap<>();

        try {
            // Находим пользователей для удаления
            List<Map<String, Object>> usersToDelete = findByUsernameLike(prefix);
            results.put("users_found", usersToDelete.size());

            int totalPointsDeleted = 0;
            int totalFunctionsDeleted = 0;
            int totalUsersDeleted = 0;

            // Для каждого пользователя безопасно удаляем все данные
            for (Map<String, Object> user : usersToDelete) {
                Long userId = (Long) user.get("id");
                String username = (String) user.get("username");

                // Удаляем точки пользователя
                int pointsDeleted = pointDAO.deletePointsByUserId(userId);
                totalPointsDeleted += pointsDeleted;

                // Удаляем функции пользователя
                int functionsDeleted = functionDAO.deleteFunctionsByUserId(userId);
                totalFunctionsDeleted += functionsDeleted;

                // Удаляем самого пользователя
                String deleteUserSql = "DELETE FROM users WHERE id = ?";
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(deleteUserSql)) {

                    stmt.setLong(1, userId);
                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows > 0) {
                        totalUsersDeleted++;
                        logger.debug("Удален пользователь: {} (точки: {}, функции: {})",
                                username, pointsDeleted, functionsDeleted);
                    }
                }
            }

            results.put("points_deleted", totalPointsDeleted);
            results.put("functions_deleted", totalFunctionsDeleted);
            results.put("users_deleted", totalUsersDeleted);

            logger.info("Полная очистка тестовых данных по префиксу {}: {} точек, {} функций, {} пользователей",
                    prefix, totalPointsDeleted, totalFunctionsDeleted, totalUsersDeleted);

            return results;

        } catch (Exception e) {
            logger.error("Ошибка при полной очистке тестовых данных по префиксу: {}", prefix, e);
            results.put("error", -1);
            return results;
        }
    }

    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ

    /**
     * Получение количества пользователей по префиксу имени пользователя
     * @param prefix префикс имени пользователя
     * @return количество пользователей
     */
    public int getUsersCountByUsernamePrefix(String prefix) {
        String sql = "SELECT COUNT(*) as count FROM users WHERE username LIKE ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                logger.debug("Найдено {} пользователей с префиксом: {}", count, prefix);
                return count;
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Ошибка при подсчете пользователей по префиксу: {}", prefix, e);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Проверка существования пользователей по префиксу имени пользователя
     * @param prefix префикс имени пользователя
     * @return true если пользователи существуют, false если нет
     */
    public boolean existsUsersByUsernamePrefix(String prefix) {
        return getUsersCountByUsernamePrefix(prefix) > 0;
    }

    /**
     * Получение информации о пользователях по префиксу
     * @param prefix префикс имени пользователя
     * @return список пользователей с информацией
     */
    public List<Map<String, Object>> getUsersByUsernamePrefix(String prefix) {
        return findByUsernameLike(prefix);
    }

    /**
     * Проверка, есть ли у пользователя связанные данные (функции или точки)
     * @param userId ID пользователя
     * @return true если есть связанные данные, false если нет
     */
    public boolean hasUserRelatedData(Long userId) {
        // Проверяем наличие функций
        String functionsSql = "SELECT COUNT(*) as count FROM functions WHERE user_id = ?";
        String pointsSql = "SELECT COUNT(*) as count FROM points WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement functionsStmt = conn.prepareStatement(functionsSql);
             PreparedStatement pointsStmt = conn.prepareStatement(pointsSql)) {

            functionsStmt.setLong(1, userId);
            ResultSet functionsRs = functionsStmt.executeQuery();

            pointsStmt.setLong(1, userId);
            ResultSet pointsRs = pointsStmt.executeQuery();

            boolean hasFunctions = false;
            boolean hasPoints = false;

            if (functionsRs.next()) {
                hasFunctions = functionsRs.getInt("count") > 0;
            }
            if (pointsRs.next()) {
                hasPoints = pointsRs.getInt("count") > 0;
            }

            boolean hasData = hasFunctions || hasPoints;
            logger.debug("Пользователь ID {} имеет связанные данные: функции={}, точки={}",
                    userId, hasFunctions, hasPoints);

            return hasData;

        } catch (SQLException e) {
            logger.error("Ошибка при проверке связанных данных пользователя с ID: {}", userId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Получение статистики по пользователю (количество функций и точек)
     * @param userId ID пользователя
     * @return Map со статистикой
     */
    public Map<String, Integer> getUserStatistics(Long userId) {
        Map<String, Integer> stats = new HashMap<>();

        String functionsSql = "SELECT COUNT(*) as function_count FROM functions WHERE user_id = ?";
        String pointsSql = "SELECT COUNT(*) as point_count FROM points WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement functionsStmt = conn.prepareStatement(functionsSql);
             PreparedStatement pointsStmt = conn.prepareStatement(pointsSql)) {

            functionsStmt.setLong(1, userId);
            ResultSet functionsRs = functionsStmt.executeQuery();

            pointsStmt.setLong(1, userId);
            ResultSet pointsRs = pointsStmt.executeQuery();

            if (functionsRs.next()) {
                stats.put("function_count", functionsRs.getInt("function_count"));
            }
            if (pointsRs.next()) {
                stats.put("point_count", pointsRs.getInt("point_count"));
            }

            logger.debug("Статистика пользователя ID {}: {} функций, {} точек",
                    userId, stats.get("function_count"), stats.get("point_count"));

            return stats;

        } catch (SQLException e) {
            logger.error("Ошибка при получении статистики пользователя с ID: {}", userId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Создание пользователя с ролью (для Basic Auth)
     */
    public Long createUserWithRole(String username, String password, String role, String email) {
        String sql = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setString(4, role != null ? role : "USER");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Создание пользователя не удалось");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    logger.info("Создан пользователь с ID: {}, username: {}, role: {}",
                            id, username, role);
                    return id;
                } else {
                    throw new SQLException("Создание пользователя не удалось, ID не получен");
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при создании пользователя: {}", username, e);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Обновление роли пользователя (для выдачи прав)
     */
    public boolean updateUserRole(Long id, String newRole) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newRole);
            stmt.setLong(2, id);

            int affectedRows = stmt.executeUpdate();
            boolean updated = affectedRows > 0;

            if (updated) {
                logger.info("Обновлена роль пользователя с ID: {} на {}", id, newRole);
            } else {
                logger.warn("Пользователь с ID {} не найден для обновления роли", id);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении роли пользователя с ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }



    private Map<String, Object> mapResultSetToMap(ResultSet rs) throws SQLException {
        Map<String, Object> user = new HashMap<>();
        user.put("id", rs.getLong("id"));
        user.put("username", rs.getString("username"));
        user.put("password", rs.getString("password"));
        user.put("email", rs.getString("email"));
        user.put("created_at", rs.getTimestamp("created_at"));
        user.put("updated_at", rs.getTimestamp("updated_at"));
        return user;
    }
}