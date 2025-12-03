package com.example.LAB5.servlet;

import com.example.LAB5.manual.DTO.PointDTO;
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

@WebServlet("/api/v1/points/*")
public class PointServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(PointServlet.class.getName());
    private final List<PointDTO> points = new ArrayList<>();
    private final Gson gson = new Gson();
    private Long currentId = 1L;

    // Для доступа из других сервлетов
    public List<PointDTO> getPoints() {
        return points;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        // Регистрируем себя в контексте
        getServletContext().setAttribute("pointServlet", this);

        // Добавляем тестовую точку
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

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/v1/points - все точки
                logger.info("Returning all points, count: " + points.size());
                response.getWriter().write(gson.toJson(points));
            } else {
                // GET /api/v1/points/{id} - конкретная точка
                Long id = extractIdFromPath(pathInfo);
                if (id == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    logger.warning("Invalid ID format: " + pathInfo);
                    return;
                }

                logger.info("Looking for point with id: " + id);
                PointDTO point = findPointById(id);

                if (point != null) {
                    response.getWriter().write(gson.toJson(point));
                    logger.info("Point found: id=" + point.getId() +
                            ", x=" + point.getXValue() + ", y=" + point.getYValue());
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    logger.warning("Point not found with id: " + id);
                }
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

        String pathInfo = request.getPathInfo();
        logger.info("POST /api/v1/points" + (pathInfo != null ? pathInfo : ""));

        try {
            if (pathInfo != null && !pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("POST should not have path parameters");
                return;
            }

            // Читаем JSON
            PointDTO newPoint = gson.fromJson(request.getReader(), PointDTO.class);

            // Проверяем обязательные поля
            if (newPoint.getFunctionId() == null ||
                    newPoint.getXValue() == null ||
                    newPoint.getYValue() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("Missing required fields (functionId, xValue, or yValue)");
                return;
            }

            // Проверяем существование функции
            FunctionServlet functionServlet = (FunctionServlet) getServletContext().getAttribute("functionServlet");
            if (functionServlet != null) {
                boolean functionExists = functionServlet.getFunctions().stream()
                        .anyMatch(f -> f.getId().equals(newPoint.getFunctionId()));
                if (!functionExists) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    logger.warning("Function not found with id: " + newPoint.getFunctionId());
                    return;
                }
            }

            // Устанавливаем ID
            newPoint.setId(currentId++);
            points.add(newPoint);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(newPoint));
            logger.info("Point created successfully: functionId=" + newPoint.getFunctionId() +
                    ", x=" + newPoint.getXValue() + ", y=" + newPoint.getYValue());

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

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("PUT requires point ID in path");
                return;
            }

            Long id = extractIdFromPath(pathInfo);
            if (id == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Читаем обновленные данные
            PointDTO updatedData = gson.fromJson(request.getReader(), PointDTO.class);

            // Находим существующую точку
            PointDTO existingPoint = findPointById(id);
            if (existingPoint == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                logger.warning("Point not found for update, id: " + id);
                return;
            }

            // Обновляем поля (сохраняем ID и functionId)
            if (updatedData.getXValue() != null) {
                existingPoint.setXValue(updatedData.getXValue());
            }

            if (updatedData.getYValue() != null) {
                existingPoint.setYValue(updatedData.getYValue());
            }

            // Не позволяем менять functionId (точка привязана к функции)
            if (updatedData.getFunctionId() != null &&
                    !updatedData.getFunctionId().equals(existingPoint.getFunctionId())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("Cannot change functionId for existing point");
                return;
            }

            response.getWriter().write(gson.toJson(existingPoint));
            logger.info("Point updated successfully: id=" + existingPoint.getId());

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

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.warning("DELETE requires point ID in path");
                return;
            }

            Long id = extractIdFromPath(pathInfo);
            if (id == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            boolean removed = points.removeIf(point -> point.getId().equals(id));

            if (removed) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                logger.info("Point deleted successfully, id: " + id);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                logger.warning("Point not found for deletion, id: " + id);
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
                return Long.parseLong(pathInfo.substring(1));
            }
            return Long.parseLong(pathInfo);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}