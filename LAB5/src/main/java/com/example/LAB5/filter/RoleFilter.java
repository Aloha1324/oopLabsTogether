package com.example.LAB5.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class RoleFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(RoleFilter.class);
    private String requiredRole;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        requiredRole = filterConfig.getInitParameter("requiredRole");
        logger.debug("RoleFilter initialized with role: {}", requiredRole);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        Map<String, Object> user = (Map<String, Object>) httpRequest.getAttribute("authenticatedUser");

        if (user == null) {
            logger.error("No authenticated user found");
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String userRole = (String) user.get("role");

        if (!requiredRole.equals(userRole)) {
            String username = (String) user.get("username");
            logger.warn("Access denied for user: {} (role: {}), required role: {}",
                    username, userRole, requiredRole);
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions");
            return;
        }

        logger.debug("Access granted for user: {} with role: {}",
                user.get("username"), userRole);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("RoleFilter destroyed");
    }
}