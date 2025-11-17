package com.example.lab5;

import com.example.lab5.dao.FunctionDao;
import com.example.lab5.dao.PointDao;
import com.example.lab5.dao.UserDao;
import com.example.lab5.entity.Function;
import com.example.lab5.entity.Point;
import com.example.lab5.entity.User;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PointDaoTest {
    private static final Logger logger = LoggerFactory.getLogger(PointDaoTest.class);
    private PointDao pointDao;
    private FunctionDao functionDao;
    private UserDao userDao;
    private User testUser;
    private Function testFunction;

    @BeforeAll
    void setup() {
        pointDao = new PointDao();
        functionDao = new FunctionDao();
        userDao = new UserDao();

        // Create test user and function
        testUser = new User("point_test_user_" + System.currentTimeMillis(), "test_password_hash");
        testUser = userDao.save(testUser);

        testFunction = new Function("point_test_function_" + System.currentTimeMillis(), "x^2", testUser);
        testFunction = functionDao.save(testFunction);

        logger.info("Starting PointDao tests with function ID: {}", testFunction.getId());
    }

    @Test
    void testSaveAndFindPoint() {
        // Create point with diverse coordinates
        Point point = new Point(2.5, 6.25, testFunction); // x^2 where x=2.5

        // Save point
        Point savedPoint = pointDao.save(point);
        assertNotNull(savedPoint.getId());
        logger.info("Saved point with ID: {} (x={}, y={})", savedPoint.getId(), savedPoint.getXValue(), savedPoint.getYValue());

        // Find by ID
        Optional<Point> foundPoint = pointDao.findById(savedPoint.getId());
        assertTrue(foundPoint.isPresent());
        assertEquals(savedPoint.getXValue(), foundPoint.get().getXValue());
        assertEquals(savedPoint.getYValue(), foundPoint.get().getYValue());

        // Find by function
        List<Point> functionPoints = pointDao.findByFunction(testFunction);
        assertFalse(functionPoints.isEmpty());
    }

    @Test
    void testUpdatePoint() {
        Point point = new Point(1.0, 1.0, testFunction);
        Point savedPoint = pointDao.save(point);

        // Update point coordinates
        savedPoint.setXValue(3.0);
        savedPoint.setYValue(9.0); // 3^2 = 9
        boolean updated = pointDao.update(savedPoint);
        assertTrue(updated);

        // Verify update
        Optional<Point> foundPoint = pointDao.findById(savedPoint.getId());
        assertTrue(foundPoint.isPresent());
        assertEquals(3.0, foundPoint.get().getXValue());
        assertEquals(9.0, foundPoint.get().getYValue());
    }

    @Test
    void testDeletePoint() {
        Point point = new Point(5.0, 25.0, testFunction); // 5^2 = 25
        Point savedPoint = pointDao.save(point);

        // Delete point
        boolean deleted = pointDao.delete(savedPoint.getId());
        assertTrue(deleted);

        // Verify deletion
        Optional<Point> foundPoint = pointDao.findById(savedPoint.getId());
        assertFalse(foundPoint.isPresent());
    }

    @Test
    void testFindAllPoints() {
        // Create multiple points with diverse coordinates
        Point point1 = new Point(-2.0, 4.0, testFunction);  // (-2)^2 = 4
        Point point2 = new Point(0.0, 0.0, testFunction);   // 0^2 = 0
        Point point3 = new Point(3.0, 9.0, testFunction);   // 3^2 = 9
        Point point4 = new Point(1.5, 2.25, testFunction);  // 1.5^2 = 2.25

        pointDao.save(point1);
        pointDao.save(point2);
        pointDao.save(point3);
        pointDao.save(point4);

        List<Point> points = pointDao.findAll();
        assertTrue(points.size() >= 4);
        logger.info("Found {} total points", points.size());
    }

    @Test
    void testFindByFunctionId() {
        Point point = new Point(4.0, 16.0, testFunction); // 4^2 = 16
        pointDao.save(point);

        List<Point> functionPoints = pointDao.findByFunctionId(testFunction.getId());
        assertFalse(functionPoints.isEmpty());

        // Verify all points belong to the test function
        for (Point p : functionPoints) {
            assertEquals(testFunction.getId(), p.getFunction().getId());
        }
    }

    @Test
    void testFindByXValueBetween() {
        // Create points across a range
        Point point1 = new Point(-5.0, 25.0, testFunction);
        Point point2 = new Point(0.0, 0.0, testFunction);
        Point point3 = new Point(5.0, 25.0, testFunction);

        pointDao.save(point1);
        pointDao.save(point2);
        pointDao.save(point3);

        // Find points between -1 and 1
        List<Point> pointsInRange = pointDao.findByXValueBetween(-1.0, 1.0);
        assertEquals(1, pointsInRange.size()); // Only point at x=0.0
        assertEquals(0.0, pointsInRange.get(0).getXValue());
    }

    @Test
    void testFindByYValueGreaterThan() {
        Point point1 = new Point(1.0, 1.0, testFunction);
        Point point2 = new Point(3.0, 9.0, testFunction);
        Point point3 = new Point(4.0, 16.0, testFunction);

        pointDao.save(point1);
        pointDao.save(point2);
        pointDao.save(point3);

        // Find points with y > 5
        List<Point> pointsWithLargeY = pointDao.findByYValueGreaterThan(5.0);
        assertTrue(pointsWithLargeY.size() >= 2); // Points at x=3 and x=4
    }

    @Test
    void testDeleteByFunction() {
        Function tempFunction = new Function("temp_function_" + System.currentTimeMillis(), "x^3", testUser);
        tempFunction = functionDao.save(tempFunction);

        // Add points to temp function
        Point point1 = new Point(1.0, 1.0, tempFunction);
        Point point2 = new Point(2.0, 8.0, tempFunction);
        pointDao.save(point1);
        pointDao.save(point2);

        // Delete all points for the function
        boolean deleted = pointDao.deleteByFunction(tempFunction);
        assertTrue(deleted);

        // Verify deletion
        List<Point> remainingPoints = pointDao.findByFunction(tempFunction);
        assertTrue(remainingPoints.isEmpty());

        // Clean up temp function
        functionDao.delete(tempFunction.getId());
    }

    @Test
    void testCountByFunction() {
        int initialCount = pointDao.countByFunction(testFunction);

        Point point = new Point(6.0, 36.0, testFunction); // 6^2 = 36
        pointDao.save(point);

        int newCount = pointDao.countByFunction(testFunction);
        assertEquals(initialCount + 1, newCount);
    }

    @Test
    void testDiversePointCoordinates() {
        // Test with various coordinate combinations
        double[][] coordinates = {
                {-10.0, 100.0},   // x^2
                {-5.5, 30.25},    // x^2
                {0.0, 0.0},       // origin
                {2.7, 7.29},      // x^2
                {8.9, 79.21},     // x^2
                {15.0, 225.0}     // x^2
        };

        for (double[] coord : coordinates) {
            Point point = new Point(coord[0], coord[1], testFunction);
            Point saved = pointDao.save(point);
            assertNotNull(saved.getId());
            logger.debug("Saved point: x={}, y={}", coord[0], coord[1]);
        }

        List<Point> allPoints = pointDao.findByFunction(testFunction);
        assertTrue(allPoints.size() >= coordinates.length);
    }

    @Test
    void testMultipleFunctionsPoints() {
        // Create another function and test points across multiple functions
        Function linearFunction = new Function("linear_test_" + System.currentTimeMillis(), "2*x + 1", testUser);
        linearFunction = functionDao.save(linearFunction);

        // Add points to both functions
        Point quadraticPoint = new Point(3.0, 9.0, testFunction);      // x^2
        Point linearPoint = new Point(3.0, 7.0, linearFunction);       // 2*x + 1

        pointDao.save(quadraticPoint);
        pointDao.save(linearPoint);

        // Verify points are correctly associated with their functions
        List<Point> quadraticPoints = pointDao.findByFunction(testFunction);
        List<Point> linearPoints = pointDao.findByFunction(linearFunction);

        assertFalse(quadraticPoints.isEmpty());
        assertFalse(linearPoints.isEmpty());

        // Clean up
        functionDao.delete(linearFunction.getId());
    }

    @AfterEach
    void cleanup() {
        logger.info("Point test completed");
    }

    @AfterAll
    void tearDown() {
        // Clean up test data
        if (testFunction != null && testFunction.getId() != null) {
            functionDao.delete(testFunction.getId());
        }
        if (testUser != null && testUser.getId() != null) {
            userDao.delete(testUser.getId());
        }
        logger.info("Cleaned up all test data");
    }
}