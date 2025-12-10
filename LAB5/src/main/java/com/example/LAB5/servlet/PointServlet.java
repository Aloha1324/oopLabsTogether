
package com.example.LAB5.servlet;

import com.example.LAB5.manual.DTO.FunctionDTO;
import com.example.LAB5.manual.DTO.PointDTO;
import com.example.LAB5.manual.DTO.UserDTO;
import com.example.LAB5.manual.service.FunctionService;
import com.example.LAB5.manual.service.PointService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@WebServlet("/points/*")
public class PointServlet extends BaseAuthServlet {

    private static final Logger log = Logger.getLogger(PointServlet.class.getName());

    private PointService pointService;
    private FunctionService functionService;
    private ObjectMapper mapper;

    @Override
    public void init() throws ServletException {
        super.init();
        this.pointService = new PointService();
        this.functionService = new FunctionService();
        this.mapper = new ObjectMapper();
        log.info("PointControllerServlet initialized (Basic auth enabled)");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getPathInfo();

        log.info("GET " + req.getRequestURI() + " path=" + path);

        Optional<UserDTO> currentUser = authenticate(req);
        if (currentUser.isEmpty()) {
            sendUnauthorized(resp, "Authentication required");
            return;
        }

        try {
            if (path == null || "/".equals(path)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("Use /points/function/{functionId} to get points by function"));
                return;
            }

            if (path.startsWith("/function/")) {
                Long functionId = Long.parseLong(path.substring("/function/".length()));

                Optional<FunctionDTO> fnOpt = functionService.getFunctionById(functionId);
                if (fnOpt.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(), new ErrorResponse("Function not found"));
                    return;
                }

                Optional<UserDTO> owner = userService.getUserById(fnOpt.get().getUserId());
                if (owner.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(), new ErrorResponse("Function owner not found"));
                    return;
                }

                if (!checkPermission(currentUser.get(), "ADMIN", owner.get().getLogin())) {
                    sendForbidden(resp, "Access denied to function points");
                    return;
                }

                List<PointDTO> points = pointService.getPointsByFunctionId(functionId);
                log.info("User " + currentUser.get().getLogin()
                        + " retrieved " + points.size() + " points for function " + functionId);
                mapper.writeValue(resp.getWriter(), points);
                return;
            }

            if (path.startsWith("/max/")) {
                Long functionId = Long.parseLong(path.substring("/max/".length()));

                Optional<FunctionDTO> fnOpt = functionService.getFunctionById(functionId);
                if (fnOpt.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(), new ErrorResponse("Function not found"));
                    return;
                }

                Optional<UserDTO> owner = userService.getUserById(fnOpt.get().getUserId());
                if (owner.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(), new ErrorResponse("Function owner not found"));
                    return;
                }

                if (!checkPermission(currentUser.get(), "ADMIN", owner.get().getLogin())) {
                    sendForbidden(resp, "Access denied to function points");
                    return;
                }

                PointDTO maxPoint = pointService.findMaxYPoint(functionId);
                if (maxPoint != null) {
                    mapper.writeValue(resp.getWriter(), maxPoint);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(),
                            new ErrorResponse("No points found for function: " + functionId));
                }
                return;
            }

            if (path.startsWith("/min/")) {
                Long functionId = Long.parseLong(path.substring("/min/".length()));

                Optional<FunctionDTO> fnOpt = functionService.getFunctionById(functionId);
                if (fnOpt.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(), new ErrorResponse("Function not found"));
                    return;
                }

                Optional<UserDTO> owner = userService.getUserById(fnOpt.get().getUserId());
                if (owner.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(), new ErrorResponse("Function owner not found"));
                    return;
                }

                if (!checkPermission(currentUser.get(), "ADMIN", owner.get().getLogin())) {
                    sendForbidden(resp, "Access denied to function points");
                    return;
                }

                PointDTO minPoint = pointService.findMinYPoint(functionId);
                if (minPoint != null) {
                    mapper.writeValue(resp.getWriter(), minPoint);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(),
                            new ErrorResponse("No points found for function: " + functionId));
                }
                return;
            }

            if (path.startsWith("/stats/")) {
                Long functionId = Long.parseLong(path.substring("/stats/".length()));

                Optional<FunctionDTO> fnOpt = functionService.getFunctionById(functionId);
                if (fnOpt.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(), new ErrorResponse("Function not found"));
                    return;
                }

                Optional<UserDTO> owner = userService.getUserById(fnOpt.get().getUserId());
                if (owner.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(), new ErrorResponse("Function owner not found"));
                    return;
                }

                if (!checkPermission(currentUser.get(), "ADMIN", owner.get().getLogin())) {
                    sendForbidden(resp, "Access denied to function statistics");
                    return;
                }

                PointService.PointStatistics stats = pointService.getPointStatistics(functionId);
                if (stats != null) {
                    mapper.writeValue(resp.getWriter(), stats);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(),
                            new ErrorResponse("No points found for function: " + functionId));
                }
                return;
            }

            // GET /points/{id}
            Long id = Long.parseLong(path.substring(1));
            Optional<PointDTO> pointOpt = pointService.getPointById(id);

            if (pointOpt.isEmpty()) {
                log.warning("Point not found, id=" + id);
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("Point not found with ID: " + id));
                return;
            }

            Optional<FunctionDTO> fnOpt = functionService.getFunctionById(pointOpt.get().getFunctionId());
            if (fnOpt.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(), new ErrorResponse("Function not found"));
                return;
            }

            Optional<UserDTO> owner = userService.getUserById(fnOpt.get().getUserId());
            if (owner.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(), new ErrorResponse("Function owner not found"));
                return;
            }

            if (!checkPermission(currentUser.get(), "ADMIN", owner.get().getLogin())) {
                sendForbidden(resp, "Access denied to point");
                return;
            }

            log.info("User " + currentUser.get().getLogin() + " accessed point id=" + id);
            mapper.writeValue(resp.getWriter(), pointOpt.get());

        } catch (NumberFormatException ex) {
            log.warning("Invalid ID format in path: " + path);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(), new ErrorResponse("Invalid ID format"));
        } catch (Exception ex) {
            log.severe("Error processing GET /points: " + ex.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Internal server error: " + ex.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getPathInfo();

        Optional<UserDTO> currentUser = authenticate(req);
        if (currentUser.isEmpty()) {
            sendUnauthorized(resp, "Authentication required");
            return;
        }

        try {
            if (path != null && path.startsWith("/generate/")) {
                Long functionId = Long.parseLong(path.substring("/generate/".length()));

                Optional<FunctionDTO> fnOpt = functionService.getFunctionById(functionId);
                if (fnOpt.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(), new ErrorResponse("Function not found"));
                    return;
                }

                Optional<UserDTO> owner = userService.getUserById(fnOpt.get().getUserId());
                if (owner.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(), new ErrorResponse("Function owner not found"));
                    return;
                }

                if (!checkPermission(currentUser.get(), "ADMIN", owner.get().getLogin())) {
                    sendForbidden(resp, "Access denied to generate points for this function");
                    return;
                }

                PointGenerationRequest reqBody =
                        mapper.readValue(req.getInputStream(), PointGenerationRequest.class);

                int count = pointService.generateFunctionPoints(
                        functionId,
                        reqBody.functionType,
                        reqBody.start,
                        reqBody.end,
                        reqBody.step
                );

                log.info("User " + currentUser.get().getLogin()
                        + " generated " + count + " points for function " + functionId);
                mapper.writeValue(resp.getWriter(),
                        new SuccessResponse("Generated " + count + " points", count));
                return;
            }

            PointDTO dto = mapper.readValue(req.getInputStream(), PointDTO.class);
            log.info("User " + currentUser.get().getLogin()
                    + " creating point for function " + dto.getFunctionId());

            if (dto.getFunctionId() == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("Function ID is required"));
                return;
            }

            if (dto.getXValue() == null || dto.getYValue() == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("X and Y values are required"));
                return;
            }

            Optional<FunctionDTO> fnOpt = functionService.getFunctionById(dto.getFunctionId());
            if (fnOpt.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(), new ErrorResponse("Function not found"));
                return;
            }

            Optional<UserDTO> owner = userService.getUserById(fnOpt.get().getUserId());
            if (owner.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(), new ErrorResponse("Function owner not found"));
                return;
            }

            if (!checkPermission(currentUser.get(), "ADMIN", owner.get().getLogin())) {
                sendForbidden(resp, "Access denied to create points for this function");
                return;
            }

            Long pointId = pointService.createPoint(
                    dto.getFunctionId(),
                    dto.getXValue(),
                    dto.getYValue()
            );

            resp.setStatus(HttpServletResponse.SC_CREATED);
            mapper.writeValue(resp.getWriter(),
                    new SuccessResponse("Point created successfully", pointId));

        } catch (Exception ex) {
            log.severe("Error creating point: " + ex.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Invalid point data: " + ex.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getPathInfo();

        log.info("PUT " + req.getRequestURI() + " path=" + path);

        Optional<UserDTO> currentUser = authenticate(req);
        if (currentUser.isEmpty()) {
            sendUnauthorized(resp, "Authentication required");
            return;
        }

        if (path == null || "/".equals(path)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(), new ErrorResponse("Point ID is required"));
            return;
        }

        try {
            Long id = Long.parseLong(path.substring(1));
            PointDTO updates = mapper.readValue(req.getInputStream(), PointDTO.class);

            Optional<PointDTO> existing = pointService.getPointById(id);
            if (existing.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("Point not found with ID: " + id));
                return;
            }

            Optional<FunctionDTO> fnOpt =
                    functionService.getFunctionById(existing.get().getFunctionId());
            if (fnOpt.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(), new ErrorResponse("Function not found"));
                return;
            }

            Optional<UserDTO> owner =
                    userService.getUserById(fnOpt.get().getUserId());
            if (owner.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(), new ErrorResponse("Function owner not found"));
                return;
            }

            if (!checkPermission(currentUser.get(), "ADMIN", owner.get().getLogin())) {
                sendForbidden(resp, "Access denied to update point");
                return;
            }

            boolean ok = pointService.updatePoint(
                    id,
                    updates.getFunctionId(),
                    updates.getXValue(),
                    updates.getYValue()
            );

            if (ok) {
                mapper.writeValue(resp.getWriter(),
                        new SuccessResponse("Point updated successfully", id));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("Point not found with ID: " + id));
            }

        } catch (NumberFormatException ex) {
            log.warning("Invalid point ID format: " + path);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Invalid point ID format"));
        } catch (Exception ex) {
            log.severe("Error updating point: " + ex.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Internal server error: " + ex.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getPathInfo();

        log.info("DELETE " + req.getRequestURI() + " path=" + path);

        Optional<UserDTO> currentUser = authenticate(req);
        if (currentUser.isEmpty()) {
            sendUnauthorized(resp, "Authentication required");
            return;
        }

        if (path == null || "/".equals(path)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(), new ErrorResponse("Point ID is required"));
            return;
        }

        try {
            Long id = Long.parseLong(path.substring(1));

            Optional<PointDTO> existing = pointService.getPointById(id);
            if (existing.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("Point not found with ID: " + id));
                return;
            }

            Optional<FunctionDTO> fnOpt =
                    functionService.getFunctionById(existing.get().getFunctionId());
            if (fnOpt.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(), new ErrorResponse("Function not found"));
                return;
            }

            Optional<UserDTO> owner =
                    userService.getUserById(fnOpt.get().getUserId());
            if (owner.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(), new ErrorResponse("Function owner not found"));
                return;
            }

            if (!checkPermission(currentUser.get(), "ADMIN", owner.get().getLogin())) {
                sendForbidden(resp, "Access denied to delete point");
                return;
            }

            boolean deleted = pointService.deletePoint(id);
            if (deleted) {
                mapper.writeValue(resp.getWriter(),
                        new SuccessResponse("Point deleted successfully", id));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("Point not found with ID: " + id));
            }

        } catch (NumberFormatException ex) {
            log.warning("Invalid point ID format: " + path);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Invalid point ID format"));
        } catch (Exception ex) {
            log.severe("Error deleting point: " + ex.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Internal server error: " + ex.getMessage()));
        }
    }

    private static class PointGenerationRequest {
        public String functionType;
        public double start;
        public double end;
        public double step;
    }
}
