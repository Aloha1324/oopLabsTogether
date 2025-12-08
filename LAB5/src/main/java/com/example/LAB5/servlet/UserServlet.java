package com.example.LAB5.servlet;

import com.example.LAB5.manual.DTO.UserDTO;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@WebServlet("/api/v1/users/*")
public class UserServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(UserServlet.class.getName());
    private final List<UserDTO> users = new ArrayList<>();
    private final Gson gson = new Gson();
    private Long currentId = 1L;

    // Для доступа из других сервлетов
    public List<UserDTO> getUsers() {
        return users;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        // Регистрируем себя в контексте для доступа из других сервлетов
        getServletContext().setAttribute("userServlet", this);

        // Добавляем тестового пользователя
        UserDTO admin = new UserDTO("admin", "ADMIN", "admin123");
        admin.setId(currentId++);
        users.add(admin);
        logger.info("UserServlet initialized with test admin user");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        logger.info("GET /api/v1/users" + (pathInfo != null ? pathInfo : ""));

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/v1/users - все пользователи
                logger.info("Returning all users, count: " + users.size());

                // Создаем безопасные копии без паролей
                List<UserDTO> safeUsers = users.stream()
                        .map(this::createSafeUserDTO)
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

                logger.info("Looking for user with id: " + id);
                UserDTO user = findUserById(id);

                if (user != null) {
                    response.getWriter().write(gson.toJson(createSafeUserDTO(user)));
                    logger.info("User found: " + user.getLogin());
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

        String pathInfo = request.getPathInfo();
        logger.info("POST /api/v1/users" + (pathInfo != null ? pathInfo : ""));

        try {
            if (pathInfo != null && !pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("POST should not have path parameters");
                return;
            }

            // Читаем JSON и парсим в UserDTO
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

            // Устанавливаем ID и роль по умолчанию, если не указана
            newUser.setId(currentId++);
            if (newUser.getRole() == null || newUser.getRole().trim().isEmpty()) {
                newUser.setRole("USER");
            }

            users.add(newUser);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(createSafeUserDTO(newUser)));
            logger.info("User created successfully: " + newUser.getLogin() + " (id: " + newUser.getId() + ")");

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

        String pathInfo = request.getPathInfo();
        logger.info("PUT /api/v1/users" + (pathInfo != null ? pathInfo : ""));

        try {
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

            // Читаем обновленные данные
            UserDTO updatedData = gson.fromJson(request.getReader(), UserDTO.class);

            // Находим существующего пользователя
            UserDTO existingUser = findUserById(id);
            if (existingUser == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                logger.warning("User not found for update, id: " + id);
                return;
            }

            // Обновляем поля (сохраняем ID)
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
                existingUser.setPassword(updatedData.getPassword());
            }

            if (updatedData.getRole() != null && !updatedData.getRole().trim().isEmpty()) {
                existingUser.setRole(updatedData.getRole());
            }

            response.getWriter().write(gson.toJson(createSafeUserDTO(existingUser)));
            logger.info("User updated successfully: " + existingUser.getLogin());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.severe("Error updating user: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        logger.info("DELETE /api/v1/users" + (pathInfo != null ? pathInfo : ""));

        try {
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

            boolean removed = users.removeIf(user -> user.getId().equals(id));

            if (removed) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                logger.info("User deleted successfully, id: " + id);
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
                return Long.parseLong(pathInfo.substring(1));
            }
            return Long.parseLong(pathInfo);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private UserDTO createSafeUserDTO(UserDTO user) {
        // Возвращаем копию без пароля для безопасности
        return new UserDTO(user.getId(), user.getLogin(), user.getRole(), null);
    }
}