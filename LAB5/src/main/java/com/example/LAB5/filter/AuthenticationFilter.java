package com.example.LAB5.filter;

import com.example.LAB5.manual.DAO.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

public class AuthenticationFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private UserDAO userDAO;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        userDAO = new UserDAO();
        logger.info("AuthenticationFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            logger.warn("Unauthorized access attempt to: {}", httpRequest.getRequestURI());
            sendUnauthorized(httpResponse, "Authentication required");
            return;
        }

        try {
            String base64Credentials = authHeader.substring("Basic ".length());
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), "UTF-8");
            String[] values = credentials.split(":", 2);

            if (values.length != 2) {
                sendUnauthorized(httpResponse, "Invalid credentials format");
                return;
            }

            String username = values[0];
            String password = values[1];

            logger.debug("Authentication attempt for user: {}", username);

            Map<String, Object> user = userDAO.findByUsername(username);

            if (user == null) {
                logger.warn("User not found: {}", username);
                sendUnauthorized(httpResponse, "Invalid credentials");
                return;
            }

            // Проверка пароля (пока простое сравнение, потом добавим хеширование)
            String storedPassword = (String) user.get("password");
            if (!storedPassword.equals(password)) {
                logger.warn("Invalid password for user: {}", username);
                sendUnauthorized(httpResponse, "Invalid credentials");
                return;
            }

            // Добавляем пользователя в атрибуты запроса
            httpRequest.setAttribute("authenticatedUser", user);
            logger.info("User authenticated successfully: {}", username);

            chain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Authentication error", e);
            sendUnauthorized(httpResponse, "Authentication error");
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"User Realm\"");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    @Override
    public void destroy() {
        logger.info("AuthenticationFilter destroyed");
    }
}