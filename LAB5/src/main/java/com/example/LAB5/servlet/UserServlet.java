package com.example.LAB5.servlet;

import com.example.LAB5.manual.DTO.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@WebServlet("/users/*")
public class UserServlet extends BaseAuthServlet {

    private static final Logger log = Logger.getLogger(UserServlet.class.getName());

    private ObjectMapper mapper;

    @Override
    public void init() throws ServletException {
        super.init();
        this.mapper = new ObjectMapper();
        log.info("UserControllerServlet initialized (Basic auth enabled)");
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
                if (!checkPermission(currentUser.get(), "ADMIN", null)) {
                    sendForbidden(resp, "Admin access required");
                    return;
                }

                List<UserDTO> users = userService.getAllUsers();
                log.info("Admin " + currentUser.get().getLogin()
                        + " requested all users, count=" + users.size());
                mapper.writeValue(resp.getWriter(), users);
                return;
            }

            if (path.startsWith("/login/")) {
                String login = path.substring("/login/".length());

                if (!checkPermission(currentUser.get(), "ADMIN", login)) {
                    sendForbidden(resp, "Access denied to user data");
                    return;
                }

                Optional<UserDTO> user = userService.getUserByLogin(login);
                if (user.isPresent()) {
                    mapper.writeValue(resp.getWriter(), user.get());
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(),
                            new ErrorResponse("User not found with login: " + login));
                }
                return;
            }

            if (path.startsWith("/role/")) {
                if (!checkPermission(currentUser.get(), "ADMIN", null)) {
                    sendForbidden(resp, "Admin access required");
                    return;
                }

                String role = path.substring("/role/".length());
                List<UserDTO> users = userService.getUsersByRole(role);
                log.info("Admin " + currentUser.get().getLogin()
                        + " found " + users.size() + " users with role=" + role);
                mapper.writeValue(resp.getWriter(), users);
                return;
            }

            Long id = Long.parseLong(path.substring(1));
            Optional<UserDTO> user = userService.getUserById(id);

            if (user.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("User not found with ID: " + id));
                return;
            }

            if (!checkPermission(currentUser.get(), "ADMIN", user.get().getLogin())) {
                sendForbidden(resp, "Access denied to user data");
                return;
            }

            mapper.writeValue(resp.getWriter(), user.get());

        } catch (NumberFormatException ex) {
            log.warning("Invalid ID format in path: " + path);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Invalid ID format"));
        } catch (Exception ex) {
            log.severe("Error processing GET /users: " + ex.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Internal server error"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");

        try {
            UserDTO dto = mapper.readValue(req.getInputStream(), UserDTO.class);
            log.info("Registration request for login=" + dto.getLogin());

            if (dto.getLogin() == null || dto.getLogin().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("Login is required"));
                return;
            }

            if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("Password is required"));
                return;
            }

            if (dto.getRole() == null || dto.getRole().trim().isEmpty()) {
                dto.setRole("USER");
            }

            if (!authService.isValidRole(dto.getRole())) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("Invalid role. Allowed roles: USER, ADMIN"));
                return;
            }

            Long id = userService.createUser(dto.getLogin(), dto.getRole(), dto.getPassword());
            resp.setStatus(HttpServletResponse.SC_CREATED);
            mapper.writeValue(resp.getWriter(),
                    new SuccessResponse("User created successfully", id));

        } catch (Exception ex) {
            log.severe("Error creating user: " + ex.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Invalid user data: " + ex.getMessage()));
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
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("User ID is required"));
            return;
        }

        try {
            Long id = Long.parseLong(path.substring(1));
            UserDTO updates = mapper.readValue(req.getInputStream(), UserDTO.class);

            Optional<UserDTO> target = userService.getUserById(id);
            if (target.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("User not found with ID: " + id));
                return;
            }

            if (!checkPermission(currentUser.get(), "ADMIN", target.get().getLogin())) {
                sendForbidden(resp, "Access denied to update user data");
                return;
            }

            boolean ok = userService.updateUser(
                    id,
                    updates.getLogin(),
                    updates.getRole(),
                    updates.getPassword()
            );

            if (ok) {
                mapper.writeValue(resp.getWriter(),
                        new SuccessResponse("User updated successfully", id));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("User not found with ID: " + id));
            }

        } catch (NumberFormatException ex) {
            log.warning("Invalid user ID format: " + path);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Invalid user ID format"));
        } catch (Exception ex) {
            log.severe("Error updating user: " + ex.getMessage());
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
                    new ErrorResponse("User ID is required"));
            return;
        }

        try {
            Long id = Long.parseLong(path.substring(1));

            Optional<UserDTO> target = userService.getUserById(id);
            if (target.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("User not found with ID: " + id));
                return;
            }

            if (!checkPermission(currentUser.get(), "ADMIN", null)) {
                sendForbidden(resp, "Admin access required to delete users");
                return;
            }

            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                mapper.writeValue(resp.getWriter(),
                        new SuccessResponse("User deleted successfully", id));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(),
                        new ErrorResponse("User not found with ID: " + id));
            }

        } catch (NumberFormatException ex) {
            log.warning("Invalid user ID format: " + path);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Invalid user ID format"));
        } catch (Exception ex) {
            log.severe("Error deleting user: " + ex.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Internal server error"));
        }
    }
}
