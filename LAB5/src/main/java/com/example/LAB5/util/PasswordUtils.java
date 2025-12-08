package com.example.LAB5.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Logger;

public class PasswordUtils {
    private static final Logger logger = Logger.getLogger(PasswordUtils.class.getName());
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;

    /**
     * Создать соль для пароля
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Хешировать пароль с солью
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt.getBytes());
            byte[] hashedBytes = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            logger.severe("Password hashing algorithm not found: " + e.getMessage());
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    /**
     * Проверить пароль
     */
    public static boolean verifyPassword(String password, String salt, String hashedPassword) {
        String newHash = hashPassword(password, salt);
        return newHash.equals(hashedPassword);
    }

    /**
     * Создать хешированный пароль
     */
    public static String[] createHashedPassword(String plainPassword) {
        String salt = generateSalt();
        String hashedPassword = hashPassword(plainPassword, salt);
        return new String[]{hashedPassword, salt};
    }
}