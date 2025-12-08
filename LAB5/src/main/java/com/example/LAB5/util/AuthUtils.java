package com.example.LAB5.util;

import com.example.LAB5.servlet.UserServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class AuthUtils {
    private static final Logger logger = Logger.getLogger(AuthUtils.class.getName());

    // Роли и их разрешения
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_VIEWER = "VIEWER";

    // Карта разрешений: Роль -> [Методы]
    private static final Map<String, String[]> ROLE_PERMISSIONS = new HashMap<>();

    static {
        // ADMIN: полный доступ
        ROLE_PERMISSIONS.put(ROLE_ADMIN, new String[]{"GET", "POST", "PUT", "DELETE"});
        // USER: может создавать/читать свои данные, обновлять/удалять только свои
        ROLE_PERMISSIONS.put(ROLE_USER, new String[]{"GET", "POST", "PUT", "DELETE"});
        // VIEWER: только чтение
        ROLE_PERMISSIONS.put(ROLE_VIEWER, new String[]{"GET"});
    }

    /**
     * Извлечь данные авторизации из заголовка
     */
    public static String[] extractCredentials(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            try {
                String base64Credentials = authHeader.substring("Basic ".length());
                String credentials = new String(Base64.getDecoder().decode(base64Credentials));
                return credentials.split(":", 2);
            } catch (Exception e) {
                logger.warning("Invalid Basic Auth header: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    /**
     * Проверить авторизацию пользователя
     */
    public static Map<String, Object> authenticateUser(HttpServletRequest request, UserServlet userServlet) {
        String[] credentials = extractCredentials(request);
        if (credentials == null || credentials.length != 2) {
            logger.warning("Missing or invalid Authorization header");
            return null;
        }

        String username = credentials[0];
        String password = credentials[1];

        // Находим пользователя по username
        return userServlet.findUserByUsernameAndPassword(username, password);
    }

    /**
     * Проверить разрешение на метод для роли
     */
    public static boolean hasPermission(String role, String httpMethod) {
        String[] allowedMethods = ROLE_PERMISSIONS.get(role);
        if (allowedMethods == null) {
            return false;
        }

        for (String method : allowedMethods) {
            if (method.equals(httpMethod)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Проверить, имеет ли пользователь доступ к ресурсу
     * (для USER: только к своим данным)
     */
    public static boolean hasResourceAccess(Map<String, Object> user, Long resourceUserId, String httpMethod) {
        String role = (String) user.get("role");
        Long userId = (Long) user.get("id");

        // ADMIN имеет доступ ко всему
        if (ROLE_ADMIN.equals(role)) {
            return true;
        }

        // USER может только читать чужие данные, но изменять только свои
        if (ROLE_USER.equals(role)) {
            if ("GET".equals(httpMethod)) {
                return true; // Может читать все
            } else {
                return userId.equals(resourceUserId); // Может изменять только свои
            }
        }

        // VIEWER только читает
        if (ROLE_VIEWER.equals(role)) {
            return "GET".equals(httpMethod);
        }

        return false;
    }

    /**
     * Отправить ошибку авторизации
     */
    public static void sendAuthError(HttpServletResponse response, String message) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"LAB5 API\"");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
        logger.warning("Auth error: " + message);
    }
}