package com.example.LAB5.servlet;

import com.example.LAB5.manual.DTO.FunctionDTO;
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

@WebServlet("/api/v1/functions/*")
public class FunctionServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(FunctionServlet.class.getName());
    private final List<FunctionDTO> functions = new ArrayList<>();
    private final Gson gson = new Gson();
    private Long currentId = 1L;

    // Для доступа из других сервлетов
    public List<FunctionDTO> getFunctions() {
        return functions;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        // Регистрируем себя в контексте
        getServletContext().setAttribute("functionServlet", this);

        // Добавляем тестовую функцию
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

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/v1/functions - все функции
                logger.info("Returning all functions, count: " + functions.size());
                response.getWriter().write(gson.toJson(functions));
            } else {
                // GET /api/v1/functions/{id} - конкретная функция
                Long id = extractIdFromPath(pathInfo);
                if (id == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    logger.warning("Invalid ID format: " + pathInfo);
                    return;
                }

                logger.info("Looking for function with id: " + id);
                FunctionDTO function = findFunctionById(id);

                if (function != null) {
                    response.getWriter().write(gson.toJson(function));
                    logger.info("Function found: " + function.getName());
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    logger.warning("Function not found with id: " + id);
                }
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

        String pathInfo = request.getPathInfo();
        logger.info("POST /api/v1/functions" + (pathInfo != null ? pathInfo : ""));

        try {
            if (pathInfo != null && !pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("POST should not have path parameters");
                return;
            }

            // Читаем JSON
            FunctionDTO newFunction = gson.fromJson(request.getReader(), FunctionDTO.class);

            // Проверяем обязательные поля
            if (newFunction.getName() == null || newFunction.getName().trim().isEmpty() ||
                    newFunction.getSignature() == null || newFunction.getSignature().trim().isEmpty() ||
                    newFunction.getUserId() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("Missing required fields (name, signature, or userId)");
                return;
            }

            // Проверяем существование пользователя
            UserServlet userServlet = (UserServlet) getServletContext().getAttribute("userServlet");
            if (userServlet != null) {
                boolean userExists = userServlet.getUsers().stream()
                        .anyMatch(u -> u.getId().equals(newFunction.getUserId()));
                if (!userExists) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    logger.warning("User not found with id: " + newFunction.getUserId());
                    return;
                }
            }

            // Устанавливаем ID
            newFunction.setId(currentId++);
            functions.add(newFunction);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(newFunction));
            logger.info("Function created successfully: " + newFunction.getName() +
                    " (id: " + newFunction.getId() + ", user: " + newFunction.getUserId() + ")");

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

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("PUT requires function ID in path");
                return;
            }

            Long id = extractIdFromPath(pathInfo);
            if (id == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Читаем обновленные данные
            FunctionDTO updatedData = gson.fromJson(request.getReader(), FunctionDTO.class);

            // Находим существующую функцию
            FunctionDTO existingFunction = findFunctionById(id);
            if (existingFunction == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                logger.warning("Function not found for update, id: " + id);
                return;
            }

            // Обновляем поля (сохраняем ID)
            if (updatedData.getName() != null && !updatedData.getName().trim().isEmpty()) {
                existingFunction.setName(updatedData.getName());
            }

            if (updatedData.getSignature() != null && !updatedData.getSignature().trim().isEmpty()) {
                existingFunction.setSignature(updatedData.getSignature());
            }

            if (updatedData.getUserId() != null) {
                // Проверяем существование пользователя
                UserServlet userServlet = (UserServlet) getServletContext().getAttribute("userServlet");
                if (userServlet != null) {
                    boolean userExists = userServlet.getUsers().stream()
                            .anyMatch(u -> u.getId().equals(updatedData.getUserId()));
                    if (!userExists) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        logger.warning("User not found with id: " + updatedData.getUserId());
                        return;
                    }
                }
                existingFunction.setUserId(updatedData.getUserId());
            }

            response.getWriter().write(gson.toJson(existingFunction));
            logger.info("Function updated successfully: " + existingFunction.getName());

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

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("DELETE requires function ID in path");
                return;
            }

            Long id = extractIdFromPath(pathInfo);
            if (id == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Также нужно удалить все точки этой функции
            PointServlet pointServlet = (PointServlet) getServletContext().getAttribute("pointServlet");
            if (pointServlet != null) {
                pointServlet.getPoints().removeIf(point -> point.getFunctionId().equals(id));
                logger.info("Removed points for function id: " + id);
            }

            boolean removed = functions.removeIf(func -> func.getId().equals(id));

            if (removed) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                logger.info("Function deleted successfully, id: " + id);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                logger.warning("Function not found for deletion, id: " + id);
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
                return Long.parseLong(pathInfo.substring(1));
            }
            return Long.parseLong(pathInfo);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}