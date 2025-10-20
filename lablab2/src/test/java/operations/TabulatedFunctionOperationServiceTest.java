package operations;

import exceptions.InconsistentFunctionsException;
import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.Point;
import functions.TabulatedFunction;
import org.junit.jupiter.api.Test;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;

import static org.junit.jupiter.api.Assertions.*;

public class TabulatedFunctionOperationServiceTest {

    @Test
    public void testAsPoints() {
        double[] x = {0.0, 1.0, 2.0};
        double[] y = {0.0, 1.0, 4.0};
        TabulatedFunction func = new ArrayTabulatedFunction(x, y);
        Point[] points = TabulatedFunctionOperationService.asPoints(func);

        assertEquals(3, points.length);
        for (int i = 0; i < points.length; i++) {
            assertEquals(x[i], points[i].x, 1e-10);
            assertEquals(y[i], points[i].y, 1e-10);
        }
    }

    @Test
    public void testAddSameFactory() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(new ArrayTabulatedFunctionFactory());

        TabulatedFunction a = new ArrayTabulatedFunction(new double[]{0, 1, 2}, new double[]{0, 1, 4});
        TabulatedFunction b = new ArrayTabulatedFunction(new double[]{0, 1, 2}, new double[]{1, 2, 3});
        TabulatedFunction c = service.add(a, b);

        assertEquals(3, c.getCount());
        assertEquals(1, c.getY(0), 1e-10);
        assertEquals(3, c.getY(1), 1e-10);
        assertEquals(7, c.getY(2), 1e-10);
        assertTrue(c instanceof ArrayTabulatedFunction);
    }

    @Test
    public void testSubtractDifferentFactories() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(new LinkedListTabulatedFunctionFactory());

        TabulatedFunction a = new ArrayTabulatedFunction(new double[]{0, 1, 2}, new double[]{0, 1, 4});
        TabulatedFunction b = new LinkedListTabulatedFunction(new double[]{0, 1, 2}, new double[]{1, 2, 3});
        TabulatedFunction c = service.subtract(a, b);

        assertEquals(3, c.getCount());
        assertEquals(-1, c.getY(0), 1e-10);
        assertEquals(-1, c.getY(1), 1e-10);
        assertEquals(1, c.getY(2), 1e-10);
        assertTrue(c instanceof LinkedListTabulatedFunction);
    }

    @Test
    public void testInconsistentFunctionsExceptionOnAdd() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(new ArrayTabulatedFunctionFactory());

        TabulatedFunction a = new ArrayTabulatedFunction(new double[]{0, 1}, new double[]{0, 1});
        TabulatedFunction b = new ArrayTabulatedFunction(new double[]{0, 2}, new double[]{1, 2});

        assertThrows(InconsistentFunctionsException.class, () -> service.add(a, b));
    }

    @Test
    public void testInconsistentFunctionsExceptionOnMismatchX() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(new ArrayTabulatedFunctionFactory());

        TabulatedFunction a = new ArrayTabulatedFunction(new double[]{0, 1}, new double[]{0, 1});
        TabulatedFunction b = new ArrayTabulatedFunction(new double[]{0, 2}, new double[]{0, 1});

        assertThrows(InconsistentFunctionsException.class, () -> service.add(a, b));
    }

    @Test
    public void testDefaultConstructor() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        assertTrue(service.getFactory() instanceof ArrayTabulatedFunctionFactory);
    }

    @Test
    public void testConstructorWithFactory() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(factory);
        assertEquals(factory, service.getFactory());
    }

    @Test
    public void testSetFactory() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        TabulatedFunctionFactory newFactory = new LinkedListTabulatedFunctionFactory();
        service.setFactory(newFactory);
        assertEquals(newFactory, service.getFactory());
    }

    @Test
    void testMultiplyBasicOperation() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues1 = {2.0, 3.0, 4.0};
        double[] yValues2 = {5.0, 6.0, 7.0};
        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues, yValues2);

        TabulatedFunction result = service.multiply(func1, func2);

        assertEquals(3, result.getCount());
        assertEquals(1.0, result.getX(0), 1e-10);
        assertEquals(2.0, result.getX(1), 1e-10);
        assertEquals(3.0, result.getX(2), 1e-10);
        assertEquals(10.0, result.getY(0), 1e-10);
        assertEquals(18.0, result.getY(1), 1e-10);
        assertEquals(28.0, result.getY(2), 1e-10);
    }

    @Test
    void testMultiplyWithNegativeValues() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues = {-2.0, -1.0, 0.0, 1.0, 2.0};
        double[] yValues1 = {-3.0, -2.0, -1.0, 0.0, 1.0};
        double[] yValues2 = {2.0, 3.0, 4.0, 5.0, 6.0};
        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues, yValues2);

        TabulatedFunction result = service.multiply(func1, func2);

        assertEquals(5, result.getCount());
        assertEquals(-6.0, result.getY(0), 1e-10);
        assertEquals(-6.0, result.getY(1), 1e-10);
        assertEquals(-4.0, result.getY(2), 1e-10);
        assertEquals(0.0, result.getY(3), 1e-10);
        assertEquals(6.0, result.getY(4), 1e-10);
    }

    @Test
    void testDivideBasicOperation() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues1 = {6.0, 12.0, 18.0};
        double[] yValues2 = {2.0, 3.0, 6.0};
        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues, yValues2);

        TabulatedFunction result = service.divide(func1, func2);

        assertEquals(3, result.getCount());
        assertEquals(1.0, result.getX(0), 1e-10);
        assertEquals(2.0, result.getX(1), 1e-10);
        assertEquals(3.0, result.getX(2), 1e-10);
        assertEquals(3.0, result.getY(0), 1e-10);
        assertEquals(4.0, result.getY(1), 1e-10);
        assertEquals(3.0, result.getY(2), 1e-10);
    }

    @Test
    void testDivideWithFractionalResults() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues1 = {1.0, 1.0, 1.0};
        double[] yValues2 = {2.0, 4.0, 8.0};
        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues, yValues2);

        TabulatedFunction result = service.divide(func1, func2);

        assertEquals(3, result.getCount());
        assertEquals(0.5, result.getY(0), 1e-10);
        assertEquals(0.25, result.getY(1), 1e-10);
        assertEquals(0.125, result.getY(2), 1e-10);
    }

    @Test
    void testDivideByZeroThrowsException() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues1 = {1.0, 2.0, 3.0};
        double[] yValues2 = {1.0, 0.0, 3.0};
        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues, yValues2);

        ArithmeticException exception = assertThrows(ArithmeticException.class, () -> {
            service.divide(func1, func2);
        });

        assertTrue(exception.getMessage().contains("Division by zero"));
    }

    @Test
    void testMultiplyInconsistentFunctionsThrowsException() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] xValues2 = {1.0, 2.0};
        double[] yValues1 = {1.0, 2.0, 3.0};
        double[] yValues2 = {4.0, 5.0};
        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues2);

        InconsistentFunctionsException exception = assertThrows(InconsistentFunctionsException.class, () -> {
            service.multiply(func1, func2);
        });

        assertTrue(exception.getMessage().contains("different number of points"));
    }

    @Test
    void testDivideInconsistentFunctionsThrowsException() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] xValues2 = {1.0, 3.0, 4.0};
        double[] yValues1 = {1.0, 2.0, 3.0};
        double[] yValues2 = {4.0, 5.0, 6.0};
        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues2);

        InconsistentFunctionsException exception = assertThrows(InconsistentFunctionsException.class, () -> {
            service.divide(func1, func2);
        });

        assertTrue(exception.getMessage().contains("X values differ"));
    }

    @Test
    void testAllOperationsWithSameFunctions() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {2.0, 4.0, 6.0};
        TabulatedFunction func = new ArrayTabulatedFunction(xValues, yValues);

        TabulatedFunction addResult = service.add(func, func);
        TabulatedFunction subtractResult = service.subtract(func, func);
        TabulatedFunction multiplyResult = service.multiply(func, func);
        TabulatedFunction divideResult = service.divide(func, func);

        assertEquals(4.0, addResult.getY(0), 1e-10);
        assertEquals(8.0, addResult.getY(1), 1e-10);
        assertEquals(12.0, addResult.getY(2), 1e-10);

        assertEquals(0.0, subtractResult.getY(0), 1e-10);
        assertEquals(0.0, subtractResult.getY(1), 1e-10);
        assertEquals(0.0, subtractResult.getY(2), 1e-10);

        assertEquals(4.0, multiplyResult.getY(0), 1e-10);
        assertEquals(16.0, multiplyResult.getY(1), 1e-10);
        assertEquals(36.0, multiplyResult.getY(2), 1e-10);

        assertEquals(1.0, divideResult.getY(0), 1e-10);
        assertEquals(1.0, divideResult.getY(1), 1e-10);
        assertEquals(1.0, divideResult.getY(2), 1e-10);
    }

    @Test
    void testOperationsPreserveXValues() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues = {-1.0, 0.0, 1.0, 2.0};
        double[] yValues1 = {1.0, 2.0, 3.0, 4.0};
        double[] yValues2 = {5.0, 6.0, 7.0, 8.0};
        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues, yValues2);

        TabulatedFunction multiplyResult = service.multiply(func1, func2);
        TabulatedFunction divideResult = service.divide(func1, func2);

        for (int i = 0; i < xValues.length; i++) {
            assertEquals(xValues[i], multiplyResult.getX(i), 1e-10);
            assertEquals(xValues[i], divideResult.getX(i), 1e-10);
        }
    }

    @Test
    void testFactoryChangeAffectsNewOperations() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues = {1.0, 2.0};
        double[] yValues1 = {3.0, 4.0};
        double[] yValues2 = {5.0, 6.0};
        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues, yValues2);

        service.setFactory(new LinkedListTabulatedFunctionFactory());
        TabulatedFunction multiplyResult = service.multiply(func1, func2);
        TabulatedFunction divideResult = service.divide(func1, func2);

        assertTrue(multiplyResult instanceof LinkedListTabulatedFunction);
        assertTrue(divideResult instanceof LinkedListTabulatedFunction);

        assertEquals(15.0, multiplyResult.getY(0), 1e-10);
        assertEquals(24.0, multiplyResult.getY(1), 1e-10);
        assertEquals(0.6, divideResult.getY(0), 1e-10);
        assertEquals(0.6666666667, divideResult.getY(1), 1e-10);
    }

    // Тесты для покрытия InconsistentFunctionsException
    @Test
    void testInconsistentFunctionsExceptionDefaultConstructor() {
        // Покрываем конструктор по умолчанию
        InconsistentFunctionsException exception = new InconsistentFunctionsException();
        assertNull(exception.getMessage());
    }

    @Test
    void testInconsistentFunctionsExceptionWithMessage() {
        // Покрываем конструктор с сообщением
        String message = "Test message";
        InconsistentFunctionsException exception = new InconsistentFunctionsException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testInconsistentFunctionsExceptionWithEmptyMessage() {
        // Покрываем конструктор с пустым сообщением
        InconsistentFunctionsException exception = new InconsistentFunctionsException("");
        assertEquals("", exception.getMessage());
    }

    @Test
    void testInconsistentFunctionsExceptionWithNullMessage() {
        // Покрываем конструктор с null сообщением
        InconsistentFunctionsException exception = new InconsistentFunctionsException(null);
        assertNull(exception.getMessage());
    }

    @Test
    void testDoOperationWithDifferentXValuesThrowsDetailedException() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] xValues2 = {1.0, 2.5, 3.0}; // Different X at index 1
        double[] yValues1 = {1.0, 2.0, 3.0};
        double[] yValues2 = {4.0, 5.0, 6.0};

        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues2);

        InconsistentFunctionsException exception = assertThrows(InconsistentFunctionsException.class, () -> {
            service.add(func1, func2);
        });

        assertTrue(exception.getMessage().contains("X values differ at index 1"));
    }

    @Test
    void testDoOperationAllBranches() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        // Test 1: Different count of points
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] xValues2 = {1.0, 2.0}; // Different count
        double[] yValues1 = {1.0, 2.0, 3.0};
        double[] yValues2 = {4.0, 5.0};

        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues2);

        InconsistentFunctionsException exception1 = assertThrows(InconsistentFunctionsException.class, () -> {
            service.add(func1, func2);
        });
        assertTrue(exception1.getMessage().contains("different number of points"));

        // Test 2: Same count but different X values
        double[] xValues3 = {1.0, 2.0, 3.0};
        double[] xValues4 = {1.0, 2.1, 3.0}; // Different X at index 1
        double[] yValues3 = {1.0, 2.0, 3.0};
        double[] yValues4 = {4.0, 5.0, 6.0};

        TabulatedFunction func3 = new ArrayTabulatedFunction(xValues3, yValues3);
        TabulatedFunction func4 = new ArrayTabulatedFunction(xValues4, yValues4);

        InconsistentFunctionsException exception2 = assertThrows(InconsistentFunctionsException.class, () -> {
            service.add(func3, func4);
        });
        assertTrue(exception2.getMessage().contains("X values differ at index 1"));

        // Test 3: Successful operation with exact same X values
        double[] xValues5 = {1.0, 2.0, 3.0};
        double[] yValues5 = {1.0, 2.0, 3.0};
        double[] yValues6 = {4.0, 5.0, 6.0};

        TabulatedFunction func5 = new ArrayTabulatedFunction(xValues5, yValues5);
        TabulatedFunction func6 = new ArrayTabulatedFunction(xValues5, yValues6);

        // This should not throw exception
        assertDoesNotThrow(() -> service.add(func5, func6));
    }

    @Test
    void testSubtractWithInconsistentFunctions() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        TabulatedFunction a = new ArrayTabulatedFunction(new double[]{1.0, 2.0}, new double[]{1.0, 2.0});
        TabulatedFunction b = new ArrayTabulatedFunction(new double[]{1.0, 3.0}, new double[]{3.0, 4.0});

        InconsistentFunctionsException exception = assertThrows(InconsistentFunctionsException.class, () -> {
            service.subtract(a, b);
        });

        assertTrue(exception.getMessage().contains("X values differ"));
    }
}