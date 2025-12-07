package com.example.LAB5.servlet;

import com.example.LAB5.manual.DAO.UserDAO;
import com.example.LAB5.manual.DTO.UserDTO;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@WebServlet("/api/v1/users/*")
public class UserServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(UserServlet.class.getName());
    private final Gson gson = new Gson();
    private UserDAO userDAO;

    // Константы ролей
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";
    private static final String ROLE_MODERATOR = "MODERATOR";

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
        logger.info("UserServlet initialized with UserDAO");

        // Инициализируем таблицу пользователей
        try {
            // Метод initTable нужно добавить в UserDAO
            // userDAO.initTable();
            logger.info("User table initialized");
        } catch (Exception e) {
            logger.severe("Error initializing user table: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        logger.info("GET /api/v1/users" + (pathInfo != null ? pathInfo : ""));

        try {
            // Проверка аутентификации
            Map<String, Object> currentUser = getAuthenticatedUser(request);
            if (currentUser == null) {
                sendUnauthorized(response, "Authentication required");
                return;
            }

            String currentUserRole = (String) currentUser.get("role");
            String currentUsername = (String) currentUser.get("username");

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/v1/users - все пользователи

                // Проверка роли: только ADMIN может видеть всех пользователей
                if (!ROLE_ADMIN.equals(currentUserRole)) {
                    logger.warning("User " + currentUsername + " with role " + currentUserRole +
                            " attempted to access all users list");
                    sendForbidden(response, "Insufficient permissions. Only ADMIN can view all users");
                    return;
                }

                List<Map<String, Object>> allUsers = userDAO.findAll();
                logger.info("User " + currentUsername + " retrieved all users, count: " + allUsers.size());

                // Создаем безопасные копии без паролей
                List<Map<String, Object>> safeUsers = allUsers.stream()
                        .map(this::createSafeUserMap)
                        .collect(Collectors.toList());

                response.getWriter().write(gson.toJson(safeUsers));

            } else if (pathInfo.startsWith("/role/")) {
                // GET /api/v1/users/role/{role} - пользователи по роли

                // Проверка роли: только ADMIN и MODERATOR могут фильтровать по ролям
                if (!ROLE_ADMIN.equals(currentUserRole) && !ROLE_MODERATOR.equals(currentUserRole)) {
                    logger.warning("User " + currentUsername + " with role " + currentUserRole +
                            " attempted to filter users by role");
                    sendForbidden(response, "Insufficient permissions");
                    return;
                }

                String role = pathInfo.substring(6); // Убираем "/role/"
                List<Map<String, Object>> usersByRole = userDAO.findByRole(role);
                logger.info("User " + currentUsername + " retrieved users with role: " + role +
                        ", count: " + usersByRole.size());

                List<Map<String, Object>> safeUsers = usersByRole.stream()
                        .map(this::createSafeUserMap)
                        .collect(Collectors.toList());

                response.getWriter().write(gson.toJson(safeUsers));

            } else {
                // GET /api/v1/users/{id} - конкретный пользователь
                Long id = extractIdFromPath(pathInfo);
                if (id == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    logger.warning("Invalid ID format: " + pathInfo);
                    return;
                }

                logger.info("User " + currentUsername + " looking for user with id: " + id);
                Map<String, Object> user = userDAO.findById(id);

                if (user != null) {
                    // Проверка: пользователь может видеть только свой профиль, 
                    // если он не ADMIN и не MODERATOR
                    Long userId = (Long) user.get("id");
                    Long currentUserId = (Long) currentUser.get("id");

                    if (!ROLE_ADMIN.equals(currentUserRole) &&
                            !ROLE_MODERATOR.equals(currentUserRole) &&
                            !userId.equals(currentUserId)) {
                        logger.warning("User " + currentUsername + " attempted to access user id: " + id);
                        sendForbidden(response, "You can only view your own profile");
                        return;
                    }

                    response.getWriter().write(gson.toJson(createSafeUserMap(user)));
                    logger.info("User found: " + user.get("username"));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    logger.warning("User not found with id: " + id);
                }
            }
        } catch (Exception e) {
            logger.severe("Error in GET /users: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error", "Internal server error")));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        logger.info("POST /api/v1/users" + (pathInfo != null ? pathInfo : ""));

        try {
            // Для регистрации нового пользователя не требуется аутентификация
            // Но если путь содержит /admin, то нужна проверка

            boolean isAdminPath = false;
            String currentUserRole = null;
            String currentUsername = null;

            if (pathInfo != null && pathInfo.startsWith("/admin/")) {
                // Создание пользователя администратором
                Map<String, Object> currentUser = getAuthenticatedUser(request);
                if (currentUser == null) {
                    sendUnauthorized(response, "Authentication required for admin operations");
                    return;
                }

                currentUserRole = (String) currentUser.get("role");
                currentUsername = (String) currentUser.get("username");
                isAdminPath = true;

                // Только ADMIN и MODERATOR могут создавать пользователей через admin путь
                if (!ROLE_ADMIN.equals(currentUserRole) && !ROLE_MODERATOR.equals(currentUserRole)) {
                    logger.warning("User " + currentUsername + " with role " + currentUserRole +
                            " attempted to create user via admin path");
                    sendForbidden(response, "Only ADMIN and MODERATOR can create users via admin path");
                    return;
                }
            }

            if (pathInfo != null && !pathInfo.equals("/") && !pathInfo.startsWith("/admin/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("POST should not have path parameters");
                response.getWriter().write(gson.toJson(Map.of("error", "Invalid path")));
                return;
            }

            // Читаем JSON и парсим в UserDTO
            UserDTO newUser = gson.fromJson(request.getReader(), UserDTO.class);

            // Проверяем обязательные поля
            if (newUser.getLogin() == null || newUser.getLogin().trim().isEmpty() ||
                    newUser.getPassword() == null || newUser.getPassword().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("Missing required fields (username or password)");
                response.getWriter().write(gson.toJson(Map.of("error", "Username and password are required")));
                return;
            }

            // Проверяем уникальность username
            if (userDAO.existsByUsername(newUser.getLogin())) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                logger.warning("User with username already exists: " + newUser.getLogin());
                response.getWriter().write(gson.toJson(Map.of("error", "Username already exists")));
                return;
            }

            // Устанавливаем роль по умолчанию если не указана
            if (newUser.getRole() == null || newUser.getRole().trim().isEmpty()) {
                newUser.setRole(ROLE_USER);
            }

            // Проверка роли при создании через admin путь
            if (isAdminPath) {
                // ADMIN/MODERATOR может создавать пользователей с любой ролью
                // Но MODERATOR не может создавать ADMIN
                if (ROLE_MODERATOR.equals(currentUserRole) && ROLE_ADMIN.equals(newUser.getRole())) {
                    logger.warning("MODERATOR attempted to create ADMIN user");
                    sendForbidden(response, "MODERATOR cannot create ADMIN users");
                    return;
                }
            } else {
                // Обычная регистрация - только USER роль
                if (!ROLE_USER.equals(newUser.getRole())) {
                    newUser.setRole(ROLE_USER);
                    logger.info("Role changed to USER for self-registration");
                }
            }

            // Создаем пользователя в БД
            // Определяем, какой метод использовать для создания

            Long userId = null;
            String email = null; // Если в вашем DTO нет email, используем null

            // Пытаемся получить email, если есть соответствующий метод в DTO
            try {
                // Если у UserDTO есть getEmail()
                // email = newUser.getEmail();
            } catch (Exception e) {
                // Если метода нет, оставляем null
                email = null;
            }

            // Используем существующий метод из UserDAO
            if (email != null && !email.trim().isEmpty()) {
                // Если есть email, используем createUserWithEmail (ваш существующий метод)
                userId = userDAO.createUserWithEmail(newUser.getLogin(), newUser.getPassword(), email);

                // После создания обновляем роль, если нужно
                if (userId != null && !ROLE_USER.equals(newUser.getRole())) {
                    userDAO.updateUserRole(userId, newUser.getRole());
                }
            } else {
                // Если нет email, используем createUser (ваш существующий метод)
                userId = userDAO.createUser(newUser.getLogin(), newUser.getPassword());

                // После создания обновляем роль, если нужно
                if (userId != null && !ROLE_USER.equals(newUser.getRole())) {
                    userDAO.updateUserRole(userId, newUser.getRole());
                }
            }

            // Если userId равен null, значит создание не удалось
            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                logger.severe("Failed to create user: " + newUser.getLogin());
                response.getWriter().write(gson.toJson(Map.of("error", "Failed to create user")));
                return;
            }

            // Получаем созданного пользователя из БД
            Map<String, Object> createdUser = userDAO.findById(userId);

            if (createdUser == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                logger.severe("User created but not found in database, id: " + userId);
                response.getWriter().write(gson.toJson(Map.of("error", "User creation failed")));
                return;
            }

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(createSafeUserMap(createdUser)));

            String logMessage = "User created successfully: " + newUser.getLogin() +
                    " (id: " + userId + ", role: " + newUser.getRole() + ")";
            if (isAdminPath) {
                logMessage += " by " + currentUsername + " (role: " + currentUserRole + ")";
            }
            logger.info(logMessage);

        } catch (Exception e) {
            logger.severe("Error creating user: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of(
                    "error", "Error creating user: " + e.getMessage()
            )));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        logger.info("PUT /api/v1/users" + (pathInfo != null ? pathInfo : ""));

        try {
            // Проверка аутентификации
            Map<String, Object> currentUser = getAuthenticatedUser(request);
            if (currentUser == null) {
                sendUnauthorized(response, "Authentication required");
                return;
            }

            String currentUserRole = (String) currentUser.get("role");
            String currentUsername = (String) currentUser.get("username");
            Long currentUserId = (Long) currentUser.get("id");

            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("PUT requires user ID in path");
                return;
            }

            if (pathInfo.startsWith("/role/")) {
                // PUT /api/v1/users/role/{userId} - изменение роли
                // Только ADMIN может менять роли
                if (!ROLE_ADMIN.equals(currentUserRole)) {
                    logger.warning("User " + currentUsername + " attempted to change user role without ADMIN privileges");
                    sendForbidden(response, "Only ADMIN can change user roles");
                    return;
                }

                // Извлекаем userId из /role/{userId}
                String[] parts = pathInfo.split("/");
                if (parts.length < 3) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                Long userId;
                try {
                    userId = Long.parseLong(parts[2]);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                // Читаем новую роль из тела запроса
                Map<String, String> requestData = gson.fromJson(request.getReader(), Map.class);
                String newRole = requestData.get("role");

                if (newRole == null || newRole.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                // Проверяем существование пользователя
                Map<String, Object> userToUpdate = userDAO.findById(userId);
                if (userToUpdate == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                // Обновляем роль
                boolean updated = userDAO.updateUserRole(userId, newRole);
                if (updated) {
                    logger.info("ADMIN " + currentUsername + " changed role for user id " +
                            userId + " to " + newRole);
                    response.getWriter().write(gson.toJson(Map.of(
                            "message", "Role updated successfully",
                            "userId", userId,
                            "newRole", newRole
                    )));
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                return;
            }

            // Обычное обновление пользователя
            Long id = extractIdFromPath(pathInfo);
            if (id == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Проверка прав: пользователь может обновлять только свой профиль,
            // если он не ADMIN и не MODERATOR
            if (!ROLE_ADMIN.equals(currentUserRole) &&
                    !ROLE_MODERATOR.equals(currentUserRole) &&
                    !id.equals(currentUserId)) {
                logger.warning("User " + currentUsername + " attempted to update user id: " + id);
                sendForbidden(response, "You can only update your own profile");
                return;
            }

            // Читаем обновленные данные
            UserDTO updatedData = gson.fromJson(request.getReader(), UserDTO.class);

            // Находим существующего пользователя
            Map<String, Object> existingUser = userDAO.findById(id);
            if (existingUser == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                logger.warning("User not found for update, id: " + id);
                return;
            }

            // Обновляем поля (сохраняем ID)
            String newUsername = existingUser.get("username").toString();
            String newPassword = existingUser.get("password").toString();

            if (updatedData.getLogin() != null && !updatedData.getLogin().trim().isEmpty()) {
                // Проверяем уникальность нового username
                if (!updatedData.getLogin().equals(existingUser.get("username")) &&
                        userDAO.existsByUsername(updatedData.getLogin())) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    logger.warning("Username already taken: " + updatedData.getLogin());
                    response.getWriter().write(gson.toJson(Map.of("error", "Username already exists")));
                    return;
                }
                newUsername = updatedData.getLogin();
            }

            if (updatedData.getPassword() != null && !updatedData.getPassword().trim().isEmpty()) {
                newPassword = updatedData.getPassword();
            }

            // Проверка роли: только ADMIN может менять роль
            String newRole = existingUser.get("role").toString();
            if (updatedData.getRole() != null && !updatedData.getRole().trim().isEmpty()) {
                if (ROLE_ADMIN.equals(currentUserRole)) {
                    newRole = updatedData.getRole();
                    logger.info("ADMIN " + currentUsername + " changed role for user id " +
                            id + " to " + newRole);
                } else {
                    logger.warning("User " + currentUsername + " attempted to change role without ADMIN privileges");
                }
            }

            // Обновляем пользователя
            boolean updated = userDAO.updateUser(id, newUsername, newPassword);

            if (updated && !newRole.equals(existingUser.get("role"))) {
                userDAO.updateUserRole(id, newRole);
            }

            // Получаем обновленного пользователя
            Map<String, Object> updatedUser = userDAO.findById(id);
            response.getWriter().write(gson.toJson(createSafeUserMap(updatedUser)));
            logger.info("User updated successfully: " + newUsername);

        } catch (Exception e) {
            logger.severe("Error updating user: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error", "Error updating user")));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        logger.info("DELETE /api/v1/users" + (pathInfo != null ? pathInfo : ""));

        try {
            // Проверка аутентификации
            Map<String, Object> currentUser = getAuthenticatedUser(request);
            if (currentUser == null) {
                sendUnauthorized(response, "Authentication required");
                return;
            }

            String currentUserRole = (String) currentUser.get("role");
            String currentUsername = (String) currentUser.get("username");
            Long currentUserId = (Long) currentUser.get("id");

            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("DELETE requires user ID in path");
                return;
            }

            Long id = extractIdFromPath(pathInfo);
            if (id == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Проверка прав: только ADMIN может удалять других пользователей
            // Пользователь может удалить только себя
            if (!ROLE_ADMIN.equals(currentUserRole) && !id.equals(currentUserId)) {
                logger.warning("User " + currentUsername + " attempted to delete user id: " + id);
                sendForbidden(response, "You can only delete your own account");
                return;
            }

            // ADMIN не может удалить себя (защита от случайного удаления)
            if (ROLE_ADMIN.equals(currentUserRole) && id.equals(currentUserId)) {
                logger.warning("ADMIN " + currentUsername + " attempted to delete themselves");
                sendForbidden(response, "ADMIN cannot delete their own account");
                return;
            }

            boolean removed = userDAO.deleteUser(id);

            if (removed) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                logger.info("User " + currentUsername + " deleted user with id: " + id);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                logger.warning("User not found for deletion, id: " + id);
            }

        } catch (Exception e) {
            logger.severe("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error", "Error deleting user")));
        }
    }

    // Вспомогательные методы
    private Map<String, Object> getAuthenticatedUser(HttpServletRequest request) {
        return (Map<String, Object>) request.getAttribute("authenticatedUser");
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"User Realm\"");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(gson.toJson(Map.of("error", message)));
    }

    private void sendForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(gson.toJson(Map.of("error", message)));
    }

    private Long extractIdFromPath(String pathInfo) {
        try {
            if (pathInfo.startsWith("/")) {
                return Long.parseLong(pathInfo.substring(1));
            }
            return Long.parseLong(pathInfo);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Map<String, Object> createSafeUserMap(Map<String, Object> user) {
        Map<String, Object> safeUser = new java.util.HashMap<>(user);
        // Удаляем пароль из ответа
        safeUser.remove("password");
        safeUser.remove("password_hash");
        return safeUser;
    }

    // Метод для получения всех пользователей (для тестирования)
    public List<Map<String, Object>> getAllUsersFromDAO() {
        return userDAO.findAll();
    }
}