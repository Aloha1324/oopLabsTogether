package com.example.LAB5.servlet;

import com.example.LAB5.manual.DTO.*;
import com.example.LAB5.manual.DTO.PointDTO;
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

@WebServlet("/api/v1/points/*")
public class PointServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(PointServlet.class.getName());
    private final List<PointDTO> points = new ArrayList<>();
    private final Gson gson = new Gson();
    private Long currentId = 1L;

    public List<PointDTO> getPoints() {
        return points;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        getServletContext().setAttribute("pointServlet", this);

        PointDTO testPoint = new PointDTO(1L, 1.0, 1.0);
        testPoint.setId(currentId++);
        points.add(testPoint);
        logger.info("PointServlet initialized with test point");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        logger.info("GET /api/v1/points" + (pathInfo != null ? pathInfo : ""));

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
                // GET /api/v1/points - все точки
                // Фильтруем по правам доступа
                List<PointDTO> filteredPoints = filterPointsByRole(points, authenticatedUser);
                response.getWriter().write(gson.toJson(filteredPoints));
                logger.info("Returning " + filteredPoints.size() + " points to " +
                        authenticatedUser.get("username"));

            } else if (pathInfo.matches("/\\d+")) {
                // GET /api/v1/points/{id}
                Long id = extractIdFromPath(pathInfo);
                PointDTO point = findPointById(id);

                if (point != null) {
                    if (hasAccessToPoint(authenticatedUser, point.getFunctionId())) {
                        response.getWriter().write(gson.toJson(point));
                        logger.info("Point accessed by " + authenticatedUser.get("username"));
                    } else {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        logger.warning("Access denied to point " + id);
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    logger.warning("Point not found with id: " + id);
                }

            } else if (pathInfo.matches("/functions/\\d+/points")) {
                // GET /api/v1/functions/{functionId}/points
                Pattern pattern = Pattern.compile("/functions/(\\d+)/points");
                java.util.regex.Matcher matcher = pattern.matcher(pathInfo);

                if (matcher.find()) {
                    Long functionId = Long.parseLong(matcher.group(1));

                    // Получаем функцию для проверки владельца
                    FunctionServlet functionServlet = (FunctionServlet) getServletContext()
                            .getAttribute("functionServlet");
                    List<FunctionDTO> functions = functionServlet.getFunctions();

                    FunctionDTO function = functions.stream()
                            .filter(f -> f.getId().equals(functionId))
                            .findFirst()
                            .orElse(null);

                    if (function != null) {
                        // Проверка доступа к функции
                        if (hasAccessToPoint(authenticatedUser, functionId)) {
                            List<PointDTO> functionPoints = points.stream()
                                    .filter(p -> p.getFunctionId().equals(functionId))
                                    .collect(java.util.stream.Collectors.toList());
                            response.getWriter().write(gson.toJson(functionPoints));
                            logger.info("Returning points for function " + functionId);
                        } else {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            logger.warning("Access denied to function's points");
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        logger.warning("Function not found: " + functionId);
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("Invalid path: " + pathInfo);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            logger.severe("Error in GET /points: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        logger.info("POST /api/v1/points");

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
            PointDTO newPoint = gson.fromJson(request.getReader(), PointDTO.class);

            // Проверяем обязательные поля
            if (newPoint.getFunctionId() == null ||
                    newPoint.getXValue() == null ||
                    newPoint.getYValue() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("Missing required fields");
                return;
            }

            // Проверяем существование функции
            FunctionServlet functionServlet = (FunctionServlet) getServletContext().getAttribute("functionServlet");
            FunctionDTO function = functionServlet.getFunctions().stream()
                    .filter(f -> f.getId().equals(newPoint.getFunctionId()))
                    .findFirst()
                    .orElse(null);

            if (function == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("Function not found: " + newPoint.getFunctionId());
                return;
            }

            // Проверка доступа: USER может создавать точки только для своих функций
            if (!hasAccessToPoint(authenticatedUser, newPoint.getFunctionId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                logger.warning("Access denied to create point for function");
                return;
            }

            // Устанавливаем ID
            newPoint.setId(currentId++);
            points.add(newPoint);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(newPoint));
            logger.info("Point created for function " + newPoint.getFunctionId() +
                    " by " + authenticatedUser.get("username"));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.severe("Error creating point: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        logger.info("PUT /api/v1/points" + (pathInfo != null ? pathInfo : ""));

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
                logger.warning("PUT requires point ID");
                return;
            }

            Long id = extractIdFromPath(pathInfo);
            if (id == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Находим точку
            PointDTO existingPoint = findPointById(id);
            if (existingPoint == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                logger.warning("Point not found: " + id);
                return;
            }

            // Проверка доступа
            if (!hasAccessToPoint(authenticatedUser, existingPoint.getFunctionId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                logger.warning("Access denied to update point");
                return;
            }

            // Читаем обновления
            PointDTO updatedData = gson.fromJson(request.getReader(), PointDTO.class);

            // Обновляем поля
            if (updatedData.getXValue() != null) {
                existingPoint.setXValue(updatedData.getXValue());
            }
            if (updatedData.getYValue() != null) {
                existingPoint.setYValue(updatedData.getYValue());
            }

            // Не позволяем менять functionId
            if (updatedData.getFunctionId() != null &&
                    !updatedData.getFunctionId().equals(existingPoint.getFunctionId())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("Cannot change functionId for point");
                return;
            }

            response.getWriter().write(gson.toJson(existingPoint));
            logger.info("Point updated by " + authenticatedUser.get("username"));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.severe("Error updating point: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        logger.info("DELETE /api/v1/points" + (pathInfo != null ? pathInfo : ""));

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
                logger.warning("DELETE requires point ID");
                return;
            }

            Long id = extractIdFromPath(pathInfo);
            if (id == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            PointDTO point = findPointById(id);
            if (point == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                logger.warning("Point not found: " + id);
                return;
            }

            // Проверка доступа
            if (!hasAccessToPoint(authenticatedUser, point.getFunctionId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                logger.warning("Access denied to delete point");
                return;
            }

            boolean removed = points.removeIf(p -> p.getId().equals(id));
            if (removed) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                logger.info("Point deleted by " + authenticatedUser.get("username"));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            logger.severe("Error deleting point: " + e.getMessage());
        }
    }

    // Вспомогательные методы
    private PointDTO findPointById(Long id) {
        return points.stream()
                .filter(p -> p.getId().equals(id))
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

    private boolean hasAccessToPoint(Map<String, Object> user, Long functionId) {
        // Получаем информацию о функции
        FunctionServlet functionServlet = (FunctionServlet) getServletContext().getAttribute("functionServlet");
        if (functionServlet == null) return false;

        FunctionDTO function = functionServlet.getFunctions().stream()
                .filter(f -> f.getId().equals(functionId))
                .findFirst()
                .orElse(null);

        if (function == null) return false;

        // Проверяем доступ к функции
        return hasAccessToFunction(user, function.getUserId());
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

    private List<PointDTO> filterPointsByRole(List<PointDTO> allPoints,
                                              Map<String, Object> user) {
        String role = (String) user.get("role");
        Long userId = (Long) user.get("id");

        if (AuthUtils.ROLE_ADMIN.equals(role)) {
            return allPoints;
        }

        // USER и VIEWER видят только точки своих функций
        // Получаем ID функций пользователя
        FunctionServlet functionServlet = (FunctionServlet) getServletContext().getAttribute("functionServlet");
        List<Long> userFunctionIds = functionServlet.getFunctions().stream()
                .filter(f -> f.getUserId().equals(userId))
                .map(FunctionDTO::getId)
                .collect(java.util.stream.Collectors.toList());

        return allPoints.stream()
                .filter(p -> userFunctionIds.contains(p.getFunctionId()))
                .collect(java.util.stream.Collectors.toList());
    }
}