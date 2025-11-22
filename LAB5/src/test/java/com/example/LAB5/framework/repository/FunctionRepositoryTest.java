package com.example.LAB5.framework.repository;

import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.User;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FunctionRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(FunctionRepositoryTest.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FunctionRepository functionRepository;

    private static String testPrefix;

    @BeforeAll
    static void setUpAll() {
        testPrefix = "func_test_" + UUID.randomUUID().toString().substring(0, 8) + "_";
        logger.info("Установлен префикс тестов: {}", testPrefix);
    }

    private String uniqueName(String baseName) {
        return testPrefix + baseName + "_" + System.currentTimeMillis();
    }

    private User createTestUser() {
        String username = uniqueName("func_user");
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("test_hash");
        return userRepository.save(user);
    }

    private Function createTestFunction(User user, String name, String expression) {
        Function function = new Function();
        function.setName(name);
        function.setUser(user);
        function.setExpression(expression);
        return functionRepository.save(function);
    }

    @Test
    @Order(1)
    void testSaveAndFindById() {
        logger.info("=== Тест сохранения и поиска функции по ID ===");

        User user = createTestUser();
        String functionName = uniqueName("quadratic");
        String expression = "x^2 + 2*x + 1";

        Function function = createTestFunction(user, functionName, expression);

        assertNotNull(function.getId());
        assertTrue(function.getId() > 0);

        Optional<Function> foundFunction = functionRepository.findById(function.getId());
        assertTrue(foundFunction.isPresent());
        assertEquals(functionName, foundFunction.get().getName());
        assertEquals(expression, foundFunction.get().getExpression());
        assertEquals(user.getId(), foundFunction.get().getUser().getId());

        logger.info("Создана функция: ID={}, Name={}", function.getId(), function.getName());
    }

    @Test
    @Order(2)
    void testFindByUser() {
        logger.info("=== Тест поиска функций по пользователю ===");

        User user1 = createTestUser();
        User user2 = createTestUser();

        createTestFunction(user1, uniqueName("linear1"), "2*x + 3");
        createTestFunction(user1, uniqueName("quadratic1"), "x^2");
        createTestFunction(user2, uniqueName("linear2"), "x + 1");

        List<Function> user1Functions = functionRepository.findByUser(user1);
        List<Function> user2Functions = functionRepository.findByUser(user2);

        assertEquals(2, user1Functions.size());
        assertEquals(1, user2Functions.size());

        user1Functions.forEach(func ->
                assertEquals(user1.getId(), func.getUser().getId())
        );

        logger.info("Найдено {} функций для user1, {} функций для user2",
                user1Functions.size(), user2Functions.size());
    }

    @Test
    @Order(3)
    void testFindByUserId() {
        logger.info("=== Тест поиска функций по ID пользователя ===");

        User user = createTestUser();

        createTestFunction(user, uniqueName("func1"), "sin(x)");
        createTestFunction(user, uniqueName("func2"), "cos(x)");
        createTestFunction(user, uniqueName("func3"), "tan(x)");

        List<Function> functions = functionRepository.findByUserId(user.getId());

        assertFalse(functions.isEmpty());
        assertEquals(3, functions.size());
        functions.forEach(func ->
                assertEquals(user.getId(), func.getUser().getId())
        );

        logger.info("Найдено {} функций для пользователя ID={}", functions.size(), user.getId());
    }

    @Test
    @Order(4)
    void testFindAll() {
        logger.info("=== Тест поиска всех функций ===");

        int initialCount = functionRepository.findAll().size();

        User user = createTestUser();
        createTestFunction(user, uniqueName("all_test1"), "x^3");
        createTestFunction(user, uniqueName("all_test2"), "x^4");

        List<Function> allFunctions = functionRepository.findAll();
        assertTrue(allFunctions.size() >= initialCount + 2);

        logger.info("Найдено {} функций (было: {})", allFunctions.size(), initialCount);
    }

    @Test
    @Order(5)
    void testFindByNameContaining() {
        logger.info("=== Тест поиска функций по части имени ===");

        User user = createTestUser();
        String searchTerm = "polynomial";

        createTestFunction(user, uniqueName(searchTerm + "_linear"), "x + 1");
        createTestFunction(user, uniqueName(searchTerm + "_quadratic"), "x^2 + 1");
        createTestFunction(user, uniqueName("trigonometric"), "sin(x)");

        List<Function> foundFunctions = functionRepository.findByNameContaining(searchTerm);

        assertFalse(foundFunctions.isEmpty());
        assertTrue(foundFunctions.size() >= 2);
        foundFunctions.forEach(func ->
                assertTrue(func.getName().contains(searchTerm))
        );

        logger.info("Найдено {} функций содержащих '{}'", foundFunctions.size(), searchTerm);
    }

    @Test
    @Order(6)
    void testCountByUser() {
        logger.info("=== Тест подсчета функций пользователя ===");

        User user = createTestUser();

        int initialCount = functionRepository.countByUser(user);

        createTestFunction(user, uniqueName("count_test1"), "x");
        createTestFunction(user, uniqueName("count_test2"), "x^2");
        createTestFunction(user, uniqueName("count_test3"), "x^3");

        int finalCount = functionRepository.countByUser(user);
        assertEquals(initialCount + 3, finalCount);

        logger.info("Количество функций: было {}, стало {}", initialCount, finalCount);
    }

    @Test
    @Order(7)
    void testUpdateFunction() {
        logger.info("=== Тест обновления функции ===");

        User user = createTestUser();
        Function function = createTestFunction(user, uniqueName("original"), "x");

        String updatedName = uniqueName("updated");
        String updatedExpression = "2*x + 5";
        function.setName(updatedName);
        function.setExpression(updatedExpression);

        Function updatedFunction = functionRepository.save(function);
        assertNotNull(updatedFunction);

        Optional<Function> foundFunction = functionRepository.findById(function.getId());
        assertTrue(foundFunction.isPresent());
        assertEquals(updatedName, foundFunction.get().getName());
        assertEquals(updatedExpression, foundFunction.get().getExpression());

        logger.info("Функция обновлена: {}", updatedName);
    }

    @Test
    @Order(8)
    void testDeleteFunction() {
        logger.info("=== Тест удаления функции ===");

        User user = createTestUser();
        Function function = createTestFunction(user, uniqueName("to_delete"), "x^2");

        functionRepository.delete(function);

        Optional<Function> foundFunction = functionRepository.findById(function.getId());
        assertFalse(foundFunction.isPresent());

        logger.info("Функция удалена: ID={}", function.getId());
    }

    @Test
    @Order(9)
    void testFunctionNotFound() {
        logger.info("=== Тест поиска несуществующей функции ===");

        Optional<Function> nonExistentFunction = functionRepository.findById(999999L);
        assertFalse(nonExistentFunction.isPresent());

        logger.info("Несуществующая функция не найдена (ожидаемо)");
    }

    @Test
    @Order(10)
    void testFindByExpressionContaining() {
        logger.info("=== Тест поиска функций по части выражения ===");

        User user = createTestUser();
        String expressionPart = "x^2";

        createTestFunction(user, uniqueName("poly1"), "x^2 + 3*x + 2");
        createTestFunction(user, uniqueName("poly2"), "2*x^2 + x + 1");
        createTestFunction(user, uniqueName("linear"), "x + 1");

        List<Function> foundFunctions = functionRepository.findByExpressionContaining(expressionPart);

        assertFalse(foundFunctions.isEmpty());
        assertTrue(foundFunctions.size() >= 2);
        foundFunctions.forEach(func ->
                assertTrue(func.getExpression().contains(expressionPart))
        );

        logger.info("Найдено {} функций содержащих '{}' в выражении",
                foundFunctions.size(), expressionPart);
    }

    @Test
    @Order(11)
    void testFindByUserIdAndNameContaining() {
        logger.info("=== Тест поиска функций по ID пользователя и части имени ===");

        User user = createTestUser();
        String namePart = "math";

        createTestFunction(user, uniqueName("math_function1"), "x + 1");
        createTestFunction(user, uniqueName("math_function2"), "x^2");
        createTestFunction(user, uniqueName("physics_function"), "sin(x)");

        List<Function> foundFunctions = functionRepository.findByUserIdAndNameContaining(user.getId(), namePart);

        assertFalse(foundFunctions.isEmpty());
        assertTrue(foundFunctions.size() >= 2);
        foundFunctions.forEach(func -> {
            assertEquals(user.getId(), func.getUser().getId());
            assertTrue(func.getName().contains(namePart));
        });

        logger.info("Найдено {} функций пользователя ID={} содержащих '{}'",
                foundFunctions.size(), user.getId(), namePart);
    }

    @AfterEach
    void tearDown() {
        try {
            cleanTestData();
        } catch (Exception e) {
            logger.warn("Ошибка при очистке тестовых данных: {}", e.getMessage());
        }
    }

    private void cleanTestData() {
        logger.info("Очистка тестовых данных функций...");

        // Очистка функций
        List<Function> allFunctions = functionRepository.findAll();
        int deletedFunctions = 0;

        for (Function function : allFunctions) {
            if (function.getName().startsWith(testPrefix)) {
                functionRepository.delete(function);
                deletedFunctions++;
            }
        }

        // Очистка пользователей
        List<User> allUsers = userRepository.findAll();
        int deletedUsers = 0;

        for (User user : allUsers) {
            if (user.getUsername().startsWith(testPrefix)) {
                userRepository.delete(user);
                deletedUsers++;
            }
        }

        if (deletedFunctions > 0 || deletedUsers > 0) {
            logger.info("Очищено {} функций и {} пользователей", deletedFunctions, deletedUsers);
        }
    }
}