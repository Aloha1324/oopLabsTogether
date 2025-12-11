package com.example.LAB5.manual.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SecurityLogger {

    private static final Logger securityLog = LoggerFactory.getLogger("SECURITY");

    private SecurityLogger() {
        // утилитный класс, создавать экземпляры не нужно
    }

    public static void logAuthenticationSuccess(String username, String role, String ip) {
        securityLog.info("auth.success user={} role={} ip={}", username, role, ip);
    }

    public static void logAuthenticationFailure(String username, String reason, String ip) {
        securityLog.warn("auth.failure user={} reason={} ip={}", username, reason, ip);
    }

    public static void logAuthorizationSuccess(String username, String action, String resource) {
        securityLog.info("authz.success user={} action={} resource={}", username, action, resource);
    }

    public static void logAuthorizationFailure(String username,
                                               String action,
                                               String resource,
                                               String reason) {
        securityLog.warn("authz.failure user={} action={} resource={} reason={}",
                username, action, resource, reason);
    }

    public static void logUserCreation(String createdBy, String newUser, String role) {
        securityLog.info("user.create by={} user={} role={}", createdBy, newUser, role);
    }

    public static void logUserDeletion(String deletedBy, String targetUser) {
        securityLog.info("user.delete by={} user={}", deletedBy, targetUser);
    }

    public static void logRoleChange(String changedBy,
                                     String targetUser,
                                     String oldRole,
                                     String newRole) {
        securityLog.info("user.role-change by={} user={} from={} to={}",
                changedBy, targetUser, oldRole, newRole);
    }

    public static void logSecurityEvent(String event, String details) {
        securityLog.info("security.event type={} details={}", event, details);
    }
}
