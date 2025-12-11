package com.example.LAB5.manual.service;

import com.example.LAB5.manual.DAO.UserDAO;
import com.example.LAB5.manual.DTO.UserDTO;
import com.example.LAB5.manual.logging.SecurityLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserDAO userDao;

    public AuthService(UserDAO userDao) {
        this.userDao = userDao;
    }

    public Optional<UserDTO> authenticate(String authorizationHeader, HttpServletRequest request) {
        final String clientIp = extractClientIp(request);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Basic ")) {
            log.warn("Отсутствует или некорректен Authorization header, IP={}", clientIp);
            SecurityLogger.logAuthenticationFailure("unknown", "Missing Authorization header", clientIp);
            return Optional.empty();
        }

        try {
            Credentials creds = parseBasicHeader(authorizationHeader);
            if (creds == null) {
                log.warn("Неверный формат Basic credentials, IP={}", clientIp);
                SecurityLogger.logAuthenticationFailure("unknown", "Invalid credentials format", clientIp);
                return Optional.empty();
            }

            String login = creds.login;
            String password = creds.password;

            Optional<UserDTO> userOpt = userDao.findByLogin(login);
            if (userOpt.isEmpty()) {
                log.warn("Пользователь {} не найден, IP={}", login, clientIp);
                SecurityLogger.logAuthenticationFailure(login, "User not found", clientIp);
                return Optional.empty();
            }

            UserDTO user = userOpt.get();
            boolean ok = password.equals(user.getPassword());

            if (ok) {
                log.info("Успешная аутентификация {} с ролью {} (IP={})",
                        login, user.getRole(), clientIp);
                SecurityLogger.logAuthenticationSuccess(login, user.getRole(), clientIp);
                return Optional.of(user);
            }

            log.warn("Ошибка аутентификации {}: неверный пароль (IP={})", login, clientIp);
            SecurityLogger.logAuthenticationFailure(login, "Invalid password", clientIp);
            return Optional.empty();

        } catch (Exception ex) {
            log.error("Сбой при аутентификации, IP={}", clientIp, ex);
            SecurityLogger.logAuthenticationFailure("unknown",
                    "Authentication error: " + ex.getMessage(), clientIp);
            return Optional.empty();
        }
    }

    public Optional<UserDTO> authenticate(String authorizationHeader) {
        return authenticate(authorizationHeader, null);
    }

    public boolean hasPermission(UserDTO user,
                                 String requiredRole,
                                 String resourceOwner,
                                 String action,
                                 String resource) {
        if (user == null) {
            log.warn("Проверка прав отклонена: user=null");
            return false;
        }

        String role = user.getRole();
        String login = user.getLogin();

        if ("ADMIN".equals(role)) {
            log.debug("Админ {} имеет доступ к {} {}", login, action, resource);
            SecurityLogger.logAuthorizationSuccess(login, action, resource);
            return true;
        }

        if ("USER".equals(role)) {
            boolean allowed = login != null && login.equals(resourceOwner);
            if (allowed) {
                SecurityLogger.logAuthorizationSuccess(login, action, resource);
            } else {
                log.warn("Пользователь {} пытался выполнить {} над ресурсом владельца {}",
                        login, action, resourceOwner);
                SecurityLogger.logAuthorizationFailure(
                        login, action, resource,
                        "Access denied to resource owned by " + resourceOwner
                );
            }
            return allowed;
        }

        log.warn("Пользователь {} с некорректной ролью {} для {} {}",
                login, role, action, resource);
        SecurityLogger.logAuthorizationFailure(
                login, action, resource, "Invalid role: " + role
        );
        return false;
    }

    public boolean hasPermission(UserDTO user, String requiredRole, String resourceOwner) {
        return hasPermission(user, requiredRole, resourceOwner, "access", "resource");
    }

    public boolean isValidRole(String role) {
        return "ADMIN".equals(role) || "USER".equals(role);
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Credentials parseBasicHeader(String header) {
        try {
            String base64Part = header.substring("Basic ".length()).trim();
            byte[] decoded = Base64.getDecoder().decode(base64Part);
            String token = new String(decoded, StandardCharsets.UTF_8);
            String[] pieces = token.split(":", 2);
            if (pieces.length != 2) {
                return null;
            }
            return new Credentials(pieces[0], pieces[1]);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static class Credentials {
        final String login;
        final String password;

        Credentials(String login, String password) {
            this.login = login;
            this.password = password;
        }
    }
}

