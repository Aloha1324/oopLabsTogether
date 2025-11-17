package com.example.lab5;

import com.example.lab5.dao.FunctionDao;
import com.example.lab5.dao.UserDao;
import com.example.lab5.entity.Function;
import com.example.lab5.entity.User;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FunctionDaoTest {
    private static final Logger logger = LoggerFactory.getLogger(FunctionDaoTest.class);
    private FunctionDao functionDao;
    private UserDao userDao;
    private User testUser;

    @BeforeAll
    void setup() {
        functionDao = new FunctionDao();
        userDao = new UserDao();

        // Create test user for functions
        testUser = new User("function_test_user_" + System.currentTimeMillis(), "test_password_hash");
        testUser = userDao.save(testUser);

        logger.info("Starting FunctionDao tests with user ID: {}", testUser.getId());
    }

    @Test
    void testSaveAndFindFunction() {
        // Create diverse function data
        Function function = new Function("quadratic_" + System.currentTimeMillis(), "x^2 + 2*x + 1", testUser);

        // Save function
        Function savedFunction = functionDao.save(function);
        assertNotNull(savedFunction.getId());
        logger.info("Saved function with ID: {}", savedFunction.getId());

        // Find by ID
        Optional<Function> foundFunction = functionDao.findById(savedFunction.getId());
        assertTrue(foundFunction.isPresent());
        assertEquals(savedFunction.getName(), foundFunction.get().getName());
        assertEquals(savedFunction.getExpression(), foundFunction.get().getExpression());

        // Find by user
        List<Function> userFunctions = functionDao.findByUser(testUser);
        assertFalse(userFunctions.isEmpty());
    }

    @Test
    void testUpdateFunction() {
        Function function = new Function("update_test_" + System.currentTimeMillis(), "x + 1", testUser);
        Function savedFunction = functionDao.save(function);

        // Update function
        savedFunction.setName("updated_function_name");
        savedFunction.setExpression("2*x + 3");
        boolean updated = functionDao.update(savedFunction);
        assertTrue(updated);

        // Verify update
        Optional<Function> foundFunction = functionDao.findById(savedFunction.getId());
        assertTrue(foundFunction.isPresent());
        assertEquals("updated_function_name", foundFunction.get().getName());
        assertEquals("2*x + 3", foundFunction.get().getExpression());
    }

    @Test
    void testDeleteFunction() {
        Function function = new Function("delete_test_" + System.currentTimeMillis(), "x^3", testUser);
        Function savedFunction = functionDao.save(function);

        // Delete function
        boolean deleted = functionDao.delete(savedFunction.getId());
        assertTrue(deleted);

        // Verify deletion
        Optional<Function> foundFunction = functionDao.findById(savedFunction.getId());
        assertFalse(foundFunction.isPresent());
    }

    @Test
    void testFindAllFunctions() {
        // Create multiple functions with diverse expressions
        Function function1 = new Function("linear_" + System.currentTimeMillis(), "2*x + 1", testUser);
        Function function2 = new Function("sine_" + System.currentTimeMillis(), "sin(x)", testUser);
        Function function3 = new Function("exponential_" + System.currentTimeMillis(), "e^x", testUser);

        functionDao.save(function1);
        functionDao.save(function2);
        functionDao.save(function3);

        List<Function> functions = functionDao.findAll();
        assertTrue(functions.size() >= 3);
        logger.info("Found {} total functions", functions.size());
    }

    @Test
    void testFindByUserId() {
        Function function = new Function("user_id_test_" + System.currentTimeMillis(), "x^2", testUser);
        functionDao.save(function);

        List<Function> userFunctions = functionDao.findByUserId(testUser.getId());
        assertFalse(userFunctions.isEmpty());

        // Verify all functions belong to the test user
        for (Function func : userFunctions) {
            assertEquals(testUser.getId(), func.getUser().getId());
        }
    }

    @Test
    void testFindByNameContaining() {
        String uniquePrefix = "search_func_" + System.currentTimeMillis();
        Function function1 = new Function(uniquePrefix + "_quadratic", "x^2", testUser);
        Function function2 = new Function(uniquePrefix + "_linear", "x", testUser);

        functionDao.save(function1);
        functionDao.save(function2);

        List<Function> foundFunctions = functionDao.findByNameContaining(uniquePrefix);
        assertEquals(2, foundFunctions.size());
    }

    @Test
    void testCountByUser() {
        int initialCount = functionDao.countByUser(testUser);

        Function function = new Function("count_test_" + System.currentTimeMillis(), "x^4", testUser);
        functionDao.save(function);

        int newCount = functionDao.countByUser(testUser);
        assertEquals(initialCount + 1, newCount);
    }

    @Test
    void testDiverseFunctionExpressions() {
        // Test with various mathematical expressions
        String[] expressions = {
                "x^2 + 2*x + 1",
                "sin(x) * cos(x)",
                "log(x) + exp(x)",
                "1/(1 + e^(-x))", // sigmoid
                "sqrt(x^2 + y^2)",
                "tan(x)",
                "abs(x)",
                "x^3 - 3*x^2 + x - 1"
        };

        for (String expression : expressions) {
            Function function = new Function("diverse_" + System.currentTimeMillis() + "_" + expression.hashCode(), expression, testUser);
            Function saved = functionDao.save(function);
            assertNotNull(saved.getId());
            logger.debug("Saved function with expression: {}", expression);
        }

        List<Function> allFunctions = functionDao.findByUser(testUser);
        assertTrue(allFunctions.size() >= expressions.length);
    }

    @AfterEach
    void cleanup() {
        logger.info("Function test completed");
    }

    @AfterAll
    void tearDown() {
        // Clean up test user
        if (testUser != null && testUser.getId() != null) {
            userDao.delete(testUser.getId());
            logger.info("Cleaned up test user");
        }
    }
}