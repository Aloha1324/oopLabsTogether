package com.example.LAB5.framework.repository;

import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.User;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
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
    @Order(12)
    void testFindByNameExactMatch() {
        logger.info("=== Тест поиска функции по точному имени ===");

        User user = createTestUser();
        String exactName = uniqueName("exact_match_function");

        Function function = createTestFunction(user, exactName, "x^2 + 1");

        Optional<Function> foundFunction = functionRepository.findByName(exactName);
        assertTrue(foundFunction.isPresent());
        assertEquals(exactName, foundFunction.get().getName());
        assertEquals(user.getId(), foundFunction.get().getUser().getId());

        logger.info("Функция найдена по точному имени: {}", exactName);
    }

    @Test
    @Order(13)
    void testFindByIdBetween() {
        logger.info("=== Тест поиска функций по диапазону ID ===");

        User user = createTestUser();

        Function func1 = createTestFunction(user, uniqueName("range1"), "x");
        Function func2 = createTestFunction(user, uniqueName("range2"), "x^2");
        Function func3 = createTestFunction(user, uniqueName("range3"), "x^3");

        List<Function> functionsInRange = functionRepository.findByIdBetween(func1.getId(), func3.getId());
        assertTrue(functionsInRange.size() >= 3);

        functionsInRange.forEach(func -> {
            assertTrue(func.getId() >= func1.getId());
            assertTrue(func.getId() <= func3.getId());
        });

        logger.info("Найдено {} функций в диапазоне ID", functionsInRange.size());
    }

    @Test
    @Order(14)
    void testFindByUserOrderByNameAsc() {
        logger.info("=== Тест поиска функций с сортировкой по имени ===");

        User user = createTestUser();

        createTestFunction(user, uniqueName("zeta"), "z");
        createTestFunction(user, uniqueName("alpha"), "a");
        createTestFunction(user, uniqueName("beta"), "b");

        List<Function> sortedFunctions = functionRepository.findByUserOrderByNameAsc(user);
        assertFalse(sortedFunctions.isEmpty());

        // Проверяем сортировку
        for (int i = 0; i < sortedFunctions.size() - 1; i++) {
            String currentName = sortedFunctions.get(i).getName();
            String nextName = sortedFunctions.get(i + 1).getName();
            assertTrue(currentName.compareTo(nextName) <= 0);
        }

        logger.info("Функции отсортированы по имени, количество: {}", sortedFunctions.size());
    }

    @Test
    @Order(15)
    void testExistsByNameAndUser() {
        logger.info("=== Тест проверки существования функции ===");

        User user = createTestUser();
        String functionName = uniqueName("existence_check");

        // Проверяем перед созданием
        Boolean existsBefore = functionRepository.existsByNameAndUser(functionName, user);
        assertFalse(existsBefore);

        // Создаем функцию
        createTestFunction(user, functionName, "x + y");

        // Проверяем после создания
        Boolean existsAfter = functionRepository.existsByNameAndUser(functionName, user);
        assertTrue(existsAfter);

        logger.info("Проверка существования: до создания - {}, после создания - {}",
                existsBefore, existsAfter);
    }

    @Test
    @Order(16)
    void testDeleteByUser() {
        logger.info("=== Тест удаления функций по пользователю ===");

        User user = createTestUser();

        createTestFunction(user, uniqueName("to_delete1"), "x");
        createTestFunction(user, uniqueName("to_delete2"), "x^2");
        createTestFunction(user, uniqueName("to_delete3"), "x^3");

        // Проверяем что функции созданы
        List<Function> userFunctionsBefore = functionRepository.findByUser(user);
        assertEquals(3, userFunctionsBefore.size());

        // Удаляем все функции пользователя
        functionRepository.deleteByUser(user);

        // Проверяем что функции удалены
        List<Function> userFunctionsAfter = functionRepository.findByUser(user);
        assertTrue(userFunctionsAfter.isEmpty());

        logger.info("Все функции пользователя удалены, было: {}", userFunctionsBefore.size());
    }

    @Test
    @Order(17)
    void testCountAllFunctions() {
        logger.info("=== Тест подсчета всех функций ===");

        long initialCount = functionRepository.count();

        User user = createTestUser();
        createTestFunction(user, uniqueName("count1"), "x");
        createTestFunction(user, uniqueName("count2"), "x^2");

        long finalCount = functionRepository.count();
        assertEquals(initialCount + 2, finalCount);

        logger.info("Общее количество функций: было {}, стало {}", initialCount, finalCount);
    }

    @Test
    @Order(18)
    void testFindByNameStartingWith() {
        logger.info("=== Тест поиска функций по префиксу имени ===");

        User user = createTestUser();
        String prefix = testPrefix + "prefix_";

        createTestFunction(user, prefix + "function1", "x + 1");
        createTestFunction(user, prefix + "function2", "x^2 + 1");
        createTestFunction(user, uniqueName("other_function"), "sin(x)");

        List<Function> foundFunctions = functionRepository.findByNameStartingWith(prefix);
        assertEquals(2, foundFunctions.size());

        foundFunctions.forEach(func ->
                assertTrue(func.getName().startsWith(prefix))
        );

        logger.info("Найдено {} функций с префиксом '{}'", foundFunctions.size(), prefix);
    }

    @Test
    @Order(19)
    void testFindByNameEndingWith() {
        logger.info("=== Тест поиска функций по суффиксу имени ===");

        User user = createTestUser();
        String suffix = "_suffix_test";

        createTestFunction(user, uniqueName("func1") + suffix, "x");
        createTestFunction(user, uniqueName("func2") + suffix, "x^2");
        createTestFunction(user, uniqueName("func3"), "x^3"); // без суффикса

        List<Function> foundFunctions = functionRepository.findByNameEndingWith(suffix);
        assertEquals(2, foundFunctions.size());

        foundFunctions.forEach(func ->
                assertTrue(func.getName().endsWith(suffix))
        );

        logger.info("Найдено {} функций с суффиксом '{}'", foundFunctions.size(), suffix);
    }

    @Test
    @Order(20)
    void testFindByUserIn() {
        logger.info("=== Тест поиска функций по нескольким пользователям ===");

        User user1 = createTestUser();
        User user2 = createTestUser();
        User user3 = createTestUser();

        createTestFunction(user1, uniqueName("user1_func"), "x");
        createTestFunction(user2, uniqueName("user2_func"), "x^2");
        createTestFunction(user3, uniqueName("user3_func"), "x^3");

        List<User> users = List.of(user1, user2);
        List<Function> functions = functionRepository.findByUserIn(users);

        assertEquals(2, functions.size());

        functions.forEach(func -> {
            Long userId = func.getUser().getId();
            assertTrue(userId == user1.getId() || userId == user2.getId());
        });

        logger.info("Найдено {} функций для {} пользователей", functions.size(), users.size());
    }

    @Test
    @Order(21)
    void testComplexSearchScenario() {
        logger.info("=== Тест сложного сценария поиска ===");

        User user = createTestUser();

        // Создаем различные функции
        Function linear = createTestFunction(user, uniqueName("linear_complex"), "2*x + 3");
        Function quadratic = createTestFunction(user, uniqueName("quadratic_complex"), "x^2 + 2*x + 1");
        Function trigonometric = createTestFunction(user, uniqueName("trigonometric_complex"), "sin(x) + cos(x)");

        // Комплексные проверки
        List<Function> byUser = functionRepository.findByUser(user);
        assertEquals(3, byUser.size());

        List<Function> byNameContaining = functionRepository.findByNameContaining("complex");
        assertEquals(3, byNameContaining.size());

        List<Function> byExpression = functionRepository.findByExpressionContaining("x");
        assertTrue(byExpression.size() >= 3);

        long count = functionRepository.countByUser(user);
        assertEquals(3, count);

        logger.info("Комплексный сценарий выполнен: создано 3 функции, найдено {} по пользователю", byUser.size());
    }

    @Test
    @Order(22)
    void testPerformanceWithMultipleUsers() {
        logger.info("=== Тест производительности с множеством пользователей ===");

        int userCount = 5;
        int functionsPerUser = 3;

        for (int i = 0; i < userCount; i++) {
            User user = createTestUser();
            for (int j = 0; j < functionsPerUser; j++) {
                createTestFunction(user, uniqueName("user" + i + "_func" + j), "x^" + j);
            }
        }

        List<Function> allFunctions = functionRepository.findAll();
        assertTrue(allFunctions.size() >= userCount * functionsPerUser);

        logger.info("Создано {} пользователей с {} функциями каждый", userCount, functionsPerUser);
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