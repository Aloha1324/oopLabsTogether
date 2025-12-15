package com.example.LAB5.framework.controller;

import com.example.LAB5.framework.entity.User;
import com.example.LAB5.framework.repository.UserRepository;
import com.example.LAB5.security.AuthRequest;
import com.example.LAB5.security.AuthResponse;
import com.example.LAB5.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8080")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Конструкторное внедрение зависимостей
    @Autowired
    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Логин пользователя
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        logger.info("Попытка входа пользователя: {}", authRequest.getUsername());

        try {
            // 1. Аутентификация через AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            // 2. Используем UserDetails из аутентификации (уже с ролями)
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 3. Получаем пользователя из БД для дополнительных данных
            User user = userRepository.findByUsername(authRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // 4. Генерируем JWT токен
            String token = jwtUtil.generateToken(userDetails);

            // 5. Создаем ответ без email
            AuthResponse response = new AuthResponse(
                    token,
                    user.getUsername(),
                    user.getRole(),
                    user.getId()
            );

            logger.info("Успешный вход: {}", user.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Ошибка при входе пользователя {}: {}",
                    authRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(401).body(
                    Map.of("error", "Неверное имя пользователя или пароль")
            );
        }
    }

    /**
     * Регистрация нового пользователя
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        logger.info("Попытка регистрации: {}", username);

        // Валидация
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Имя пользователя обязательно")
            );
        }

        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Пароль должен содержать минимум 6 символов")
            );
        }

        // Проверка существования пользователя
        if (userRepository.findByUsername(username).isPresent()) {
            logger.warn("Пользователь {} уже существует", username);
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Пользователь с таким именем уже существует")
            );
        }

        try {
            // Создание пользователя
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole("USER");

            User savedUser = userRepository.save(user);
            logger.info("Пользователь зарегистрирован: ID={}, Username={}",
                    savedUser.getId(), savedUser.getUsername());

            // Создаем UserDetails для нового пользователя (с ролью)
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(savedUser.getUsername())
                    .password(savedUser.getPassword())
                    .roles(savedUser.getRole())
                    .build();

            // Генерируем токен
            String token = jwtUtil.generateToken(userDetails);

            // Создаем ответ без email
            AuthResponse response = new AuthResponse(
                    token,
                    savedUser.getUsername(),
                    savedUser.getRole(),
                    savedUser.getId()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Ошибка при регистрации {}: {}", username, e.getMessage());
            return ResponseEntity.status(500).body(
                    Map.of("error", "Ошибка при регистрации: " + e.getMessage())
            );
        }
    }

    /**
     * Проверка валидности токена
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(Map.of("valid", false));
        }

        String token = authHeader.substring(7);
        try {
            String username = jwtUtil.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "username", user.getUsername(),
                    "role", user.getRole(),
                    "userId", user.getId()
            ));
        } catch (Exception e) {
            logger.debug("Токен невалиден: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("valid", false));
        }
    }

    /**
     * Информация о текущем пользователе
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(
                    Map.of("error", "Пользователь не аутентифицирован")
            );
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("role", user.getRole());

        return ResponseEntity.ok(userInfo);
    }

    /**
     * Выход пользователя
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Успешный выход"));
    }
}