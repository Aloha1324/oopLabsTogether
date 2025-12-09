package com.example.LAB5.servlet;

import com.example.LAB5.manual.DTO.FunctionDTO;
import com.example.LAB5.util.AuthUtils;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@WebServlet("/api/v1/functions/*")
public class FunctionServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(FunctionServlet.class.getName());
    private final List<FunctionDTO> functions = new ArrayList<>();
    private final Gson gson = new Gson();
    private Long currentId = 1L;

    public List<FunctionDTO> getFunctions() {
        return functions;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        getServletContext().setAttribute("functionServlet", this);

        FunctionDTO testFunc = new FunctionDTO(1L, "Test Function", "x^2");
        testFunc.setId(currentId++);
        functions.add(testFunc);
        logger.info("FunctionServlet initialized with test function");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        logger.info("GET /api/v1/functions" + (pathInfo != null ? pathInfo : ""));

        // 1. Аутентификация
        UserServlet userServlet = (UserServlet) getServletContext().getAttribute("userServlet");
        Map<String, Object> authenticatedUser = AuthUtils.authenticateUser(request, userServlet);
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
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/v1/functions - все функции
                // USER видит только свои, ADMIN все
                List<FunctionDTO> filteredFunctions = filterFunctionsByRole(functions, authenticatedUser);
                response.getWriter().write(gson.toJson(filteredFunctions));
                logger.info("Returning " + filteredFunctions.size() + " functions to " +
                        authenticatedUser.get("username"));

            } else if (pathInfo.matches("/\\d+")) {
                // GET /api/v1/functions/{id}
                Long id = extractIdFromPath(pathInfo);
                FunctionDTO function = findFunctionById(id);

                if (function != null) {
                    if (hasAccessToFunction(authenticatedUser, function.getUserId())) {
                        response.getWriter().write(gson.toJson(function));
                        logger.info("Function accessed by " + authenticatedUser.get("username"));
                    } else {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        logger.warning("Access denied to function " + id);
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    logger.warning("Function not found with id: " + id);
                }

            } else if (pathInfo.matches("/users/\\d+/functions")) {
                // GET /api/v1/users/{userId}/functions
                Pattern pattern = Pattern.compile("/users/(\\d+)/functions");
                java.util.regex.Matcher matcher = pattern.matcher(pathInfo);

                if (matcher.find()) {
                    Long userId = Long.parseLong(matcher.group(1));

                    // Проверка доступа: USER может видеть только свои функции
                    if (hasAccessToResource(authenticatedUser, userId, "GET")) {
                        List<FunctionDTO> userFunctions = functions.stream()
                                .filter(f -> f.getUserId().equals(userId))
                                .collect(java.util.stream.Collectors.toList());
                        response.getWriter().write(gson.toJson(userFunctions));
                        logger.info("Returning functions for user " + userId);
                    } else {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        logger.warning("Access denied to user's functions");
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("Invalid path: " + pathInfo);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            logger.severe("Error in GET /functions: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        logger.info("POST /api/v1/functions");

        // 1. Аутентификация
        UserServlet userServlet = (UserServlet) getServletContext().getAttribute("userServlet");
        Map<String, Object> authenticatedUser = AuthUtils.authenticateUser(request, userServlet);
        if (authenticatedUser == null) {
            AuthUtils.sendAuthError(response, "Authentication required");
            return;
        }

        // 2. Проверка роли - VIEWER не может создавать
        String role = (String) authenticatedUser.get("role");
        if (!AuthUtils.hasPermission(role, "POST") || AuthUtils.ROLE_VIEWER.equals(role)) {
            AuthUtils.sendAuthError(response, "Insufficient permissions");
            return;
        }

        try {
            // Читаем JSON
            FunctionDTO newFunction = gson.fromJson(request.getReader(), FunctionDTO.class);

            // Проверяем обязательные поля
            if (newFunction.getName() == null || newFunction.getName().trim().isEmpty() ||
                    newFunction.getSignature() == null || newFunction.getSignature().trim().isEmpty() ||
                    newFunction.getUserId() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("Missing required fields");
                return;
            }

            // Проверка: USER может создавать функции только для себя
            Long authenticatedUserId = (Long) authenticatedUser.get("id");
            if (!AuthUtils.ROLE_ADMIN.equals(role) &&
                    !authenticatedUserId.equals(newFunction.getUserId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                logger.warning("User can only create functions for themselves");
                return;
            }

            // Проверяем существование пользователя
            boolean userExists = userServlet.getUsers().stream()
                    .anyMatch(u -> u.getId().equals(newFunction.getUserId()));
            if (!userExists) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("User not found: " + newFunction.getUserId());
                return;
            }

            // Устанавливаем ID
            newFunction.setId(currentId++);
            functions.add(newFunction);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(newFunction));
            logger.info("Function created: " + newFunction.getName() +
                    " by " + authenticatedUser.get("username"));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.severe("Error creating function: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        logger.info("PUT /api/v1/functions" + (pathInfo != null ? pathInfo : ""));

        // 1. Аутентификация
        UserServlet userServlet = (UserServlet) getServletContext().getAttribute("userServlet");
        Map<String, Object> authenticatedUser = AuthUtils.authenticateUser(request, userServlet);
        if (authenticatedUser == null) {
            AuthUtils.sendAuthError(response, "Authentication required");
            return;
        }

        // 2. Проверка роли - VIEWER не может обновлять
        String role = (String) authenticatedUser.get("role");
        if (!AuthUtils.hasPermission(role, "PUT") || AuthUtils.ROLE_VIEWER.equals(role)) {
            AuthUtils.sendAuthError(response, "Insufficient permissions");
            return;
        }

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("PUT requires function ID");
                return;
            }

            Long id = extractIdFromPath(pathInfo);
            if (id == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Находим функцию
            FunctionDTO existingFunction = findFunctionById(id);
            if (existingFunction == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                logger.warning("Function not found: " + id);
                return;
            }

            // Проверка доступа
            if (!hasAccessToFunction(authenticatedUser, existingFunction.getUserId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                logger.warning("Access denied to update function");
                return;
            }

            // Читаем обновления
            FunctionDTO updatedData = gson.fromJson(request.getReader(), FunctionDTO.class);

            // Обновляем поля
            if (updatedData.getName() != null) {
                existingFunction.setName(updatedData.getName());
            }
            if (updatedData.getSignature() != null) {
                existingFunction.setSignature(updatedData.getSignature());
            }

            // Проверка userId: только ADMIN может менять владельца
            if (updatedData.getUserId() != null &&
                    !updatedData.getUserId().equals(existingFunction.getUserId())) {
                if (AuthUtils.ROLE_ADMIN.equals(role)) {
                    existingFunction.setUserId(updatedData.getUserId());
                } else {
                    logger.warning("Non-admin attempted to change function owner");
                }
            }

            response.getWriter().write(gson.toJson(existingFunction));
            logger.info("Function updated by " + authenticatedUser.get("username"));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.severe("Error updating function: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        logger.info("DELETE /api/v1/functions" + (pathInfo != null ? pathInfo : ""));

        // 1. Аутентификация
        UserServlet userServlet = (UserServlet) getServletContext().getAttribute("userServlet");
        Map<String, Object> authenticatedUser = AuthUtils.authenticateUser(request, userServlet);
        if (authenticatedUser == null) {
            AuthUtils.sendAuthError(response, "Authentication required");
            return;
        }

        // 2. Проверка роли - VIEWER не может удалять
        String role = (String) authenticatedUser.get("role");
        if (!AuthUtils.hasPermission(role, "DELETE") || AuthUtils.ROLE_VIEWER.equals(role)) {
            AuthUtils.sendAuthError(response, "Insufficient permissions");
            return;
        }

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("DELETE requires function ID");
                return;
            }

            Long id = extractIdFromPath(pathInfo);
            if (id == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            FunctionDTO function = findFunctionById(id);
            if (function == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                logger.warning("Function not found: " + id);
                return;
            }

            // Проверка доступа
            if (!hasAccessToFunction(authenticatedUser, function.getUserId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                logger.warning("Access denied to delete function");
                return;
            }

            // Удаляем точки функции
            PointServlet pointServlet = (PointServlet) getServletContext().getAttribute("pointServlet");
            if (pointServlet != null) {
                pointServlet.getPoints().removeIf(point -> point.getFunctionId().equals(id));
            }

            // Удаляем функцию
            boolean removed = functions.removeIf(func -> func.getId().equals(id));
            if (removed) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                logger.info("Function deleted by " + authenticatedUser.get("username"));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            logger.severe("Error deleting function: " + e.getMessage());
        }
    }

    // Вспомогательные методы
    private FunctionDTO findFunctionById(Long id) {
        return functions.stream()
                .filter(f -> f.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private Long extractIdFromPath(String pathInfo) {
        try {
            if (pathInfo.startsWith("/")) {
                String idStr = pathInfo.substring(1);
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

    private boolean hasAccessToFunction(Map<String, Object> user, Long functionUserId) {
        String role = (String) user.get("role");
        Long userId = (Long) user.get("id");

        if (AuthUtils.ROLE_ADMIN.equals(role)) {
            return true;
        }

        // USER может работать только со своими функциями
        return userId.equals(functionUserId);
    }

    private boolean hasAccessToResource(Map<String, Object> user, Long resourceUserId, String method) {
        return AuthUtils.hasResourceAccess(user, resourceUserId, method);
    }

    private List<FunctionDTO> filterFunctionsByRole(List<FunctionDTO> allFunctions,
                                                    Map<String, Object> user) {
        String role = (String) user.get("role");
        Long userId = (Long) user.get("id");

        if (AuthUtils.ROLE_ADMIN.equals(role)) {
            return allFunctions;
        }

        // USER и VIEWER видят только свои функции
        return allFunctions.stream()
                .filter(f -> f.getUserId().equals(userId))
                .collect(java.util.stream.Collectors.toList());
    }
}