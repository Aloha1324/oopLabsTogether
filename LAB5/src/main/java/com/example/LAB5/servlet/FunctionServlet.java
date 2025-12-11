package com.example.LAB5.servlet;


import com.example.LAB5.manual.DTO.FunctionDTO;
import com.example.LAB5.manual.DTO.UserDTO;
import com.example.LAB5.manual.service.FunctionService;
import com.example.LAB5.manual.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@WebServlet("/functions/*")
public class FunctionServlet extends BaseAuthServlet {

    private static final Logger log = Logger.getLogger(FunctionServlet.class.getName());

    private FunctionService functionService;
    private ObjectMapper mapper;

    @Override
    public void init() throws ServletException {
        super.init();
        this.functionService = new FunctionService();
        this.mapper = new ObjectMapper();
        log.info("FunctionControllerServlet initialized (Basic auth enabled)");
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
                // GET /functions
                if (!checkPermission(currentUser.get(), "ADMIN", null)) {
                    sendForbidden(resp, "Admin access required");
                    return;
                }

                List<FunctionDTO> all = functionService.getAllFunctions();
                log.info("Admin " + currentUser.get().getLogin()
                        + " requested all functions, count=" + all.size());
                mapper.writeValue(resp.getWriter(), all);

            } else if (path.startsWith("/user/")) {
                // GET /functions/user/{userId}
                Long userId = Long.parseLong(path.substring("/user/".length()));
                Optional<UserDTO> owner = userService.getUserById(userId);

                if (owner.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(), new ErrorResponse("User not found"));
                    return;
                }

                if (!checkPermission(currentUser.get(), "ADMIN", owner.get().getLogin())) {
                    sendForbidden(resp, "Access denied to user functions");
                    return;
                }

                List<FunctionDTO> list = functionService.getFunctionsByUserId(userId);
                log.info("User " + currentUser.get().getLogin()
                        + " retrieved " + list.size() + " functions for user " + userId);
                mapper.writeValue(resp.getWriter(), list);

            } else if (path.startsWith("/name/")) {
                // GET /functions/name/{name}
                if (!checkPermission(currentUser.get(), "ADMIN", null)) {
                    sendForbidden(resp, "Admin access required");
                    return;
                }

                String name = path.substring("/name/".length());
                List<FunctionDTO> list = functionService.getFunctionsByName(name);
                log.info("Admin " + currentUser.get().getLogin()
                        + " found " + list.size() + " functions by name=" + name);
                mapper.writeValue(resp.getWriter(), list);

            } else if (path.startsWith("/stats/")) {
                // GET /functions/stats/{id}
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

                FunctionService.FunctionStatistics stats =
                        functionService.getFunctionStatistics(functionId);
                if (stats != null) {
                    mapper.writeValue(resp.getWriter(), stats);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(),
                            new ErrorResponse("Function not found with ID: " + functionId));
                }

            } else {
                // GET /functions/{id}
                Long id = Long.parseLong(path.substring(1));
                Optional<FunctionDTO> fnOpt = functionService.getFunctionById(id);

                if (fnOpt.isEmpty()) {
                    log.warning("Function not found, id=" + id);
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(),
                            new ErrorResponse("Function not found with ID: " + id));
                    return;
                }

                Optional<UserDTO> owner = userService.getUserById(fnOpt.get().getUserId());
                if (owner.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(), new ErrorResponse("Function owner not found"));
                    return;
                }

                if (!checkPermission(currentUser.get(), "ADMIN", owner.get().getLogin())) {
                    sendForbidden(resp, "Access denied to function");
                    return;
                }

                log.info("User " + currentUser.get().getLogin()
                        + " accessed function " + fnOpt.get().getName()
                        + " (id=" + id + ")");
                mapper.writeValue(resp.getWriter(), fnOpt.get());
            }
        } catch (NumberFormatException e) {
            log.warning("Invalid ID format in path: " + path);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(), new ErrorResponse("Invalid ID format"));
        } catch (Exception e) {
            log.severe("Error handling GET /functions: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(), new ErrorResponse("Internal server error"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");

        Optional<UserDTO> currentUser = authenticate(req);
        if (currentUser.isEmpty()) {
            sendUnauthorized(resp, "Authentication required");
            return;
        }

        try {
            FunctionDTO dto = mapper.readValue(req.getInputStream(), FunctionDTO.class);
            log.info("User " + currentUser.get().getLogin()
                    + " requested new function creation: " + dto.getName());

            if (dto.getUserId() == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(resp.getWriter(), new ErrorResponse("User ID is required"));
                return;
            }

            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(resp.getWriter(), new ErrorResponse("Function name is required"));
                return;
            }

            Optional<UserDTO> targetUser = userService.getUserById(dto.getUserId());
            if (targetUser.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(), new ErrorResponse("User not found"));
                return;
            }

            if (!checkPermission(currentUser.get(), "ADMIN", targetUser.get().getLogin())) {
                sendForbidden(resp, "Access denied to create function for this user");
                return;
            }

            Long newId = functionService.createFunction(
                    dto.getUserId(),
                    dto.getName(),
                    dto.getSignature()
            );

            log.info("Function created, id=" + newId + " by " + currentUser.get().getLogin());
            resp.setStatus(HttpServletResponse.SC_CREATED);
            mapper.writeValue(resp.getWriter(),
                    new SuccessResponse("Function created successfully", newId));

        } catch (Exception e) {
            log.severe("Error creating function: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Invalid function data: " + e.getMessage()));
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
            mapper.writeValue(resp.getWriter(), new ErrorResponse("Function ID is required"));
            return;
        }

        try {
            Long id = Long.parseLong(path.substring(1));
            FunctionDTO updates = mapper.readValue(req.getInputStream(), FunctionDTO.class);

            Optional<FunctionDTO> existing = functionService.getFunctionById(id);
            if (existing.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("Function not found with ID: " + id));
                return;
            }

            Optional<UserDTO> owner = userService.getUserById(existing.get().getUserId());
            if (owner.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("Function owner not found"));
                return;
            }

            if (!checkPermission(currentUser.get(), "ADMIN", owner.get().getLogin())) {
                sendForbidden(resp, "Access denied to update function");
                return;
            }

            log.info("User " + currentUser.get().getLogin()
                    + " updating function id=" + id);

            boolean updated = functionService.updateFunction(
                    id,
                    updates.getUserId(),
                    updates.getName(),
                    updates.getSignature()
            );

            if (updated) {
                mapper.writeValue(resp.getWriter(),
                        new SuccessResponse("Function updated successfully", id));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("Function not found with ID: " + id));
            }

        } catch (NumberFormatException e) {
            log.warning("Invalid function ID format: " + path);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Invalid function ID format"));
        } catch (Exception e) {
            log.severe("Error updating function: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Internal server error"));
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
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Function ID is required"));
            return;
        }

        try {
            Long id = Long.parseLong(path.substring(1));

            Optional<FunctionDTO> existing = functionService.getFunctionById(id);
            if (existing.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("Function not found with ID: " + id));
                return;
            }

            Optional<UserDTO> owner = userService.getUserById(existing.get().getUserId());
            if (owner.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("Function owner not found"));
                return;
            }

            if (!checkPermission(currentUser.get(), "ADMIN", owner.get().getLogin())) {
                sendForbidden(resp, "Access denied to delete function");
                return;
            }

            log.info("User " + currentUser.get().getLogin()
                    + " deleting function id=" + id);

            boolean deleted = functionService.deleteFunction(id);
            if (deleted) {
                mapper.writeValue(resp.getWriter(),
                        new SuccessResponse("Function deleted successfully", id));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("Function not found with ID: " + id));
            }

        } catch (NumberFormatException e) {
            log.warning("Invalid function ID format: " + path);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Invalid function ID format"));
        } catch (Exception e) {
            log.severe("Error deleting function: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Internal server error"));
        }
    }
}
