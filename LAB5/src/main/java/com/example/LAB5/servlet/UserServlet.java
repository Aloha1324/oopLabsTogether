package com.example.LAB5.servlet;

import com.example.LAB5.manual.DTO.UserDTO;
import com.example.LAB5.util.AuthUtils;
import com.example.LAB5.util.PasswordUtils;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@WebServlet("/api/v1/users/*")
public class UserServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(UserServlet.class.getName());
    private final List<UserDTO> users = new ArrayList<>();
    private final Gson gson = new Gson();
    private Long currentId = 1L;

    // Для хранения соли и хешированных паролей
    private final Map<Long, String[]> userCredentials = new HashMap<>();

    // Для доступа из других сервлетов
    public List<UserDTO> getUsers() {
        return users;
    }

    /**
     * Найти пользователя по username и password (для аутентификации)
     */
    public Map<String, Object> findUserByUsernameAndPassword(String username, String password) {
        logger.info("Authenticating user: " + username);

        for (UserDTO user : users) {
            if (user.getLogin().equals(username)) {
                String[] credentials = userCredentials.get(user.getId());
                if (credentials != null) {
                    String storedHash = credentials[0];
                    String salt = credentials[1];

                    if (PasswordUtils.verifyPassword(password, salt, storedHash)) {
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("id", user.getId());
                        userInfo.put("username", user.getLogin());
                        userInfo.put("role", user.getRole());
                        logger.info("User authenticated: " + username + ", role: " + user.getRole());
                        return userInfo;
                    }
                }
            }
        }

        logger.warning("Authentication failed for user: " + username);
        return null;
    }

    /**
     * Найти пользователя по username
     */
    public Map<String, Object> findUserByUsername(String username) {
        for (UserDTO user : users) {
            if (user.getLogin().equals(username)) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getLogin());
                userInfo.put("role", user.getRole());
                return userInfo;
            }
        }
        return null;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        getServletContext().setAttribute("userServlet", this);

        // Создаем администратора с хешированным паролем
        String[] adminCredentials = PasswordUtils.createHashedPassword("admin123");

        UserDTO admin = new UserDTO("admin", AuthUtils.ROLE_ADMIN, adminCredentials[0]);
        admin.setId(currentId++);
        users.add(admin);
        userCredentials.put(admin.getId(), adminCredentials);

        logger.info("UserServlet initialized with admin user (hashed password)");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        logger.info("GET /api/v1/users" + (request.getPathInfo() != null ? request.getPathInfo() : ""));

        // 1. Аутентификация
        Map<String, Object> authenticatedUser = AuthUtils.authenticateUser(request, this);
        if (authenticatedUser == null) {
            AuthUtils.sendAuthError(response, "Authentication required");
            return;
        }

        // 2. Проверка роли
        String role = (String) authenticatedUser.get("role");
        if (!AuthUtils.hasPermission(role, "GET")) {
            AuthUtils.sendAuthError(response, "Insufficient permissions");
            return;
        }

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/v1/users - все пользователи
                // ADMIN видит всех, USER/VIEWER видят только публичную информацию
                List<UserDTO> safeUsers = users.stream()
                        .map(user -> createSafeUserDTO(user, authenticatedUser))
                        .collect(Collectors.toList());

                response.getWriter().write(gson.toJson(safeUsers));
                logger.info("Returning " + safeUsers.size() + " users to " + authenticatedUser.get("username"));

            } else {
                // GET /api/v1/users/{id}
                Long id = extractIdFromPath(pathInfo);
                if (id == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    logger.warning("Invalid ID format: " + pathInfo);
                    return;
                }

                UserDTO user = findUserById(id);
                if (user != null) {
                    // Проверка доступа: USER может видеть только себя, ADMIN всех
                    if (AuthUtils.hasResourceAccess(authenticatedUser, user.getId(), "GET")) {
                        response.getWriter().write(gson.toJson(createSafeUserDTO(user, authenticatedUser)));
                        logger.info("User " + authenticatedUser.get("username") + " accessed user ID: " + id);
                    } else {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        logger.warning("Access denied for user " + authenticatedUser.get("username") +
                                " to user ID: " + id);
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    logger.warning("User not found with id: " + id);
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            logger.severe("Error in GET /users: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        logger.info("POST /api/v1/users" + (request.getPathInfo() != null ? request.getPathInfo() : ""));

        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.warning("POST should not have path parameters");
            return;
        }

        // Регистрация нового пользователя - не требует аутентификации
        try {
            UserDTO newUser = gson.fromJson(request.getReader(), UserDTO.class);

            // Проверяем обязательные поля
            if (newUser.getLogin() == null || newUser.getLogin().trim().isEmpty() ||
                    newUser.getPassword() == null || newUser.getPassword().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("Missing required fields (username or password)");
                return;
            }

            // Проверяем уникальность username
            if (users.stream().anyMatch(u -> u.getLogin().equals(newUser.getLogin()))) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                logger.warning("User with username already exists: " + newUser.getLogin());
                return;
            }

            // Хешируем пароль
            String[] credentials = PasswordUtils.createHashedPassword(newUser.getPassword());

            // Устанавливаем ID и роль по умолчанию
            newUser.setId(currentId++);
            if (newUser.getRole() == null || newUser.getRole().trim().isEmpty()) {
                newUser.setRole(AuthUtils.ROLE_USER); // По умолчанию USER
            } else if (!isValidRole(newUser.getRole())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("Invalid role: " + newUser.getRole());
                return;
            }

            // Сохраняем пользователя и его хешированный пароль
            users.add(newUser);
            userCredentials.put(newUser.getId(), credentials);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(createSafeUserDTO(newUser, null)));
            logger.info("User created successfully: " + newUser.getLogin() +
                    " (id: " + newUser.getId() + ", role: " + newUser.getRole() + ")");

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.severe("Error creating user: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        logger.info("PUT /api/v1/users" + (request.getPathInfo() != null ? request.getPathInfo() : ""));

        // 1. Аутентификация
        Map<String, Object> authenticatedUser = AuthUtils.authenticateUser(request, this);
        if (authenticatedUser == null) {
            AuthUtils.sendAuthError(response, "Authentication required");
            return;
        }

        // 2. Проверка роли
        String role = (String) authenticatedUser.get("role");
        if (!AuthUtils.hasPermission(role, "PUT")) {
            AuthUtils.sendAuthError(response, "Insufficient permissions");
            return;
        }

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("PUT requires user ID in path");
                return;
            }

            Long id = extractIdFromPath(pathInfo);
            if (id == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Проверяем существование пользователя
            UserDTO existingUser = findUserById(id);
            if (existingUser == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                logger.warning("User not found for update, id: " + id);
                return;
            }

            // Проверка доступа: USER может обновлять только себя, ADMIN всех
            if (!AuthUtils.hasResourceAccess(authenticatedUser, existingUser.getId(), "PUT")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                logger.warning("Access denied for user " + authenticatedUser.get("username") +
                        " to update user ID: " + id);
                return;
            }

            // Читаем обновленные данные
            UserDTO updatedData = gson.fromJson(request.getReader(), UserDTO.class);

            // Обновляем поля
            if (updatedData.getLogin() != null && !updatedData.getLogin().trim().isEmpty()) {
                // Проверяем уникальность нового username
                if (!updatedData.getLogin().equals(existingUser.getLogin()) &&
                        users.stream().anyMatch(u -> u.getLogin().equals(updatedData.getLogin()))) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    logger.warning("Username already taken: " + updatedData.getLogin());
                    return;
                }
                existingUser.setLogin(updatedData.getLogin());
            }

            if (updatedData.getPassword() != null && !updatedData.getPassword().trim().isEmpty()) {
                // Обновляем хешированный пароль
                String[] credentials = PasswordUtils.createHashedPassword(updatedData.getPassword());
                userCredentials.put(existingUser.getId(), credentials);
                existingUser.setPassword(credentials[0]); // Сохраняем хеш в DTO
            }

            // Проверка роли: только ADMIN может менять роль
            if (updatedData.getRole() != null && !updatedData.getRole().trim().isEmpty()) {
                if (AuthUtils.ROLE_ADMIN.equals(role) && isValidRole(updatedData.getRole())) {
                    existingUser.setRole(updatedData.getRole());
                    logger.info("Role updated to " + updatedData.getRole() + " by admin");
                } else {
                    logger.warning("Role change attempted by non-admin or invalid role");
                }
            }

            response.getWriter().write(gson.toJson(createSafeUserDTO(existingUser, authenticatedUser)));
            logger.info("User updated successfully: " + existingUser.getLogin() +
                    " by " + authenticatedUser.get("username"));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.severe("Error updating user: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        logger.info("DELETE /api/v1/users" + (request.getPathInfo() != null ? request.getPathInfo() : ""));

        // 1. Аутентификация
        Map<String, Object> authenticatedUser = AuthUtils.authenticateUser(request, this);
        if (authenticatedUser == null) {
            AuthUtils.sendAuthError(response, "Authentication required");
            return;
        }

        // 2. Проверка роли
        String role = (String) authenticatedUser.get("role");
        if (!AuthUtils.hasPermission(role, "DELETE")) {
            AuthUtils.sendAuthError(response, "Insufficient permissions");
            return;
        }

        try {
            String pathInfo = request.getPathInfo();
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

            UserDTO userToDelete = findUserById(id);
            if (userToDelete == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                logger.warning("User not found for deletion, id: " + id);
                return;
            }

            // Проверка доступа: USER может удалять только себя, ADMIN всех
            // Но не позволяем удалить последнего ADMIN
            if (AuthUtils.ROLE_ADMIN.equals(userToDelete.getRole()) &&
                    countAdmins() <= 1 &&
                    !userToDelete.getId().equals(authenticatedUser.get("id"))) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                logger.warning("Cannot delete the last admin user");
                return;
            }

            if (!AuthUtils.hasResourceAccess(authenticatedUser, userToDelete.getId(), "DELETE")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                logger.warning("Access denied for user " + authenticatedUser.get("username") +
                        " to delete user ID: " + id);
                return;
            }

            // Удаляем пользователя и его credentials
            boolean removed = users.removeIf(user -> user.getId().equals(id));
            if (removed) {
                userCredentials.remove(id);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                logger.info("User deleted successfully, id: " + id +
                        " by " + authenticatedUser.get("username"));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                logger.warning("User not found for deletion, id: " + id);
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            logger.severe("Error deleting user: " + e.getMessage());
        }
    }

    // Вспомогательные методы
    private UserDTO findUserById(Long id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private Long extractIdFromPath(String pathInfo) {
        try {
            if (pathInfo.startsWith("/")) {
                String idStr = pathInfo.substring(1);
                // Убираем возможные дополнительные пути
                if (idStr.contains("/")) {
                    idStr = idStr.substring(0, idStr.indexOf("/"));
                }
                return Long.parseLong(idStr);
            }
            return Long.parseLong(pathInfo);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private UserDTO createSafeUserDTO(UserDTO user, Map<String, Object> requester) {
        // ADMIN видит больше информации
        if (requester != null && AuthUtils.ROLE_ADMIN.equals(requester.get("role"))) {
            return new UserDTO(user.getId(), user.getLogin(), user.getRole(), "***");
        }
        // Остальные видят только публичную информацию
        return new UserDTO(user.getId(), user.getLogin(), user.getRole(), null);
    }

    private boolean isValidRole(String role) {
        return AuthUtils.ROLE_ADMIN.equals(role) ||
                AuthUtils.ROLE_USER.equals(role) ||
                AuthUtils.ROLE_VIEWER.equals(role);
    }

    private int countAdmins() {
        return (int) users.stream()
                .filter(u -> AuthUtils.ROLE_ADMIN.equals(u.getRole()))
                .count();
    }
}