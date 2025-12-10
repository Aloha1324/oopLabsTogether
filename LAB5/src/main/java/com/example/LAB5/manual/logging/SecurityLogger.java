package com.example.LAB5.manual.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityLogger {
    private static final Logger logger = LoggerFactory.getLogger("SECURITY");

    public static void logAuthenticationSuccess(String username, String role, String ip) {
        logger.info("AUTH_SUCCESS - User: {}, Role: {}, IP: {}", username, role, ip);
    }

    public static void logAuthenticationFailure(String username, String reason, String ip) {
        logger.warn("AUTH_FAILURE - User: {}, Reason: {}, IP: {}", username, reason, ip);
    }

    public static void logAuthorizationSuccess(String username, String action, String resource) {
        logger.info("AUTHZ_SUCCESS - User: {}, Action: {}, Resource: {}", username, action, resource);
    }

    public static void logAuthorizationFailure(String username, String action, String resource, String reason) {
        logger.warn("AUTHZ_FAILURE - User: {}, Action: {}, Resource: {}, Reason: {}",
                username, action, resource, reason);
    }

    public static void logUserCreation(String createdBy, String newUser, String role) {
        logger.info("USER_CREATED - CreatedBy: {}, NewUser: {}, Role: {}", createdBy, newUser, role);
    }

    public static void logUserDeletion(String deletedBy, String targetUser) {
        logger.info("USER_DELETED - DeletedBy: {}, TargetUser: {}", deletedBy, targetUser);
    }

    public static void logRoleChange(String changedBy, String targetUser, String oldRole, String newRole) {
        logger.info("ROLE_CHANGED - ChangedBy: {}, TargetUser: {}, OldRole: {}, NewRole: {}",
                changedBy, targetUser, oldRole, newRole);
    }

    public static void logSecurityEvent(String event, String details) {
        logger.info("SECURITY_EVENT - {}: {}", event, details);
    }
}