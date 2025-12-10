package com.example.LAB5.servlet;

import com.example.LAB5.manual.DTO.UserDTO;
import com.example.LAB5.manual.logging.SecurityLogger;
import com.example.LAB5.manual.service.AuthService;
import com.example.LAB5.manual.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public abstract class BaseAuthServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(BaseAuthServlet.class);

    protected AuthService authService;
    protected UserService userService;

    @Override
    public void init() throws ServletException {
        this.userService = new UserService();
        this.authService = new AuthService(new com.example.LAB5.manual.DAO.UserDAO());
        log.info("BaseAuthServlet initialized: AuthService and UserService created");
    }

    protected Optional<UserDTO> authenticate(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return authService.authenticate(authHeader, request);
    }

    protected boolean checkPermission(UserDTO user,
                                      String requiredRole,
                                      String resourceOwner,
                                      String action,
                                      String resource) {
        return authService.hasPermission(user, requiredRole, resourceOwner, action, resource);
    }

    protected boolean checkPermission(UserDTO user, String requiredRole, String resourceOwner) {
        return authService.hasPermission(user, requiredRole, resourceOwner, "access", "resource");
    }

    protected void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        log.warn("Unauthorized request: {}", message);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("WWW-Authenticate", "Basic realm=\"LAB5 API\"");
        response.getWriter().write(
                "{\"error\":\"Unauthorized\",\"message\":\"" + escapeJson(message) + "\"}"
        );
    }

    protected void sendForbidden(HttpServletResponse response, String message) throws IOException {
        log.warn("Forbidden request: {}", message);
        SecurityLogger.logSecurityEvent("FORBIDDEN_ACCESS", message);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"error\":\"Forbidden\",\"message\":\"" + escapeJson(message) + "\"}"
        );
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\\\"");
    }

    protected static class SuccessResponse {
        private final String message;
        private final Long id;
        private final boolean success = true;

        public SuccessResponse(String message, Long id) {
            this.message = message;
            this.id = id;
        }

        public SuccessResponse(String message, int count) {
            this.message = message;
            this.id = (long) count;
        }

        public String getMessage() {
            return message;
        }

        public Long getId() {
            return id;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    protected static class ErrorResponse {
        private final String error;
        private final boolean success = false;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}
