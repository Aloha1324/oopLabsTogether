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
        TabulatedFunction b = new ArrayTabulatedFunction(new double[]{0}, new double[]{1});

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
        // Arrange
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues1 = {2.0, 3.0, 4.0};
        double[] yValues2 = {5.0, 6.0, 7.0};
        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues, yValues2);

        // Act
        TabulatedFunction result = service.multiply(func1, func2);

        // Assert
        assertEquals(3, result.getCount());
        assertEquals(1.0, result.getX(0), 1e-10);
        assertEquals(2.0, result.getX(1), 1e-10);
        assertEquals(3.0, result.getX(2), 1e-10);
        assertEquals(10.0, result.getY(0), 1e-10); // 2 * 5 = 10
        assertEquals(18.0, result.getY(1), 1e-10); // 3 * 6 = 18
        assertEquals(28.0, result.getY(2), 1e-10); // 4 * 7 = 28
    }

    @Test
    void testMultiplyWithNegativeValues() {
        // Arrange
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues = {-2.0, -1.0, 0.0, 1.0, 2.0};
        double[] yValues1 = {-3.0, -2.0, -1.0, 0.0, 1.0};
        double[] yValues2 = {2.0, 3.0, 4.0, 5.0, 6.0};
        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues, yValues2);

        // Act
        TabulatedFunction result = service.multiply(func1, func2);

        // Assert
        assertEquals(5, result.getCount());
        assertEquals(-6.0, result.getY(0), 1e-10); // -3 * 2 = -6
        assertEquals(-6.0, result.getY(1), 1e-10); // -2 * 3 = -6
        assertEquals(-4.0, result.getY(2), 1e-10); // -1 * 4 = -4
        assertEquals(0.0, result.getY(3), 1e-10);  // 0 * 5 = 0
        assertEquals(6.0, result.getY(4), 1e-10);  // 1 * 6 = 6
    }



    @Test
    void testDivideBasicOperation() {
        // Arrange
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues1 = {6.0, 12.0, 18.0};
        double[] yValues2 = {2.0, 3.0, 6.0};
        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues, yValues2);

        // Act
        TabulatedFunction result = service.divide(func1, func2);

        // Assert
        assertEquals(3, result.getCount());
        assertEquals(1.0, result.getX(0), 1e-10);
        assertEquals(2.0, result.getX(1), 1e-10);
        assertEquals(3.0, result.getX(2), 1e-10);
        assertEquals(3.0, result.getY(0), 1e-10); // 6 / 2 = 3
        assertEquals(4.0, result.getY(1), 1e-10); // 12 / 3 = 4
        assertEquals(3.0, result.getY(2), 1e-10); // 18 / 6 = 3
    }



    @Test
    void testDivideWithFractionalResults() {
        // Arrange
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues1 = {1.0, 1.0, 1.0};
        double[] yValues2 = {2.0, 4.0, 8.0};
        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues, yValues2);

        // Act
        TabulatedFunction result = service.divide(func1, func2);

        // Assert
        assertEquals(3, result.getCount());
        assertEquals(0.5, result.getY(0), 1e-10);  // 1 / 2 = 0.5
        assertEquals(0.25, result.getY(1), 1e-10); // 1 / 4 = 0.25
        assertEquals(0.125, result.getY(2), 1e-10); // 1 / 8 = 0.125
    }

    @Test
    void testDivideByZeroThrowsException() {
        // Arrange
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues1 = {1.0, 2.0, 3.0};
        double[] yValues2 = {1.0, 0.0, 3.0}; // ноль во второй точке
        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues, yValues2);

        // Act & Assert
        ArithmeticException exception = assertThrows(ArithmeticException.class, () -> {
            service.divide(func1, func2);
        });

        assertTrue(exception.getMessage().contains("Division by zero"));
    }

    @Test
    void testMultiplyInconsistentFunctionsThrowsException() {
        // Arrange
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] xValues2 = {1.0, 2.0}; // разное количество точек
        double[] yValues1 = {1.0, 2.0, 3.0};
        double[] yValues2 = {4.0, 5.0};
        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues2);

        // Act & Assert
        InconsistentFunctionsException exception = assertThrows(InconsistentFunctionsException.class, () -> {
            service.multiply(func1, func2);
        });

        assertTrue(exception.getMessage().contains("different number of points"));
    }

    @Test
    void testDivideInconsistentFunctionsThrowsException() {
        // Arrange
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] xValues2 = {1.0, 3.0, 4.0}; // разные x-значения
        double[] yValues1 = {1.0, 2.0, 3.0};
        double[] yValues2 = {4.0, 5.0, 6.0};
        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues2);

        // Act & Assert
        InconsistentFunctionsException exception = assertThrows(InconsistentFunctionsException.class, () -> {
            service.divide(func1, func2);
        });

        assertTrue(exception.getMessage().contains("X values differ"));
    }

    @Test
    void testAllOperationsWithSameFunctions() {
        // Arrange
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {2.0, 4.0, 6.0};
        TabulatedFunction func = new ArrayTabulatedFunction(xValues, yValues);

        // Act
        TabulatedFunction addResult = service.add(func, func);
        TabulatedFunction subtractResult = service.subtract(func, func);
        TabulatedFunction multiplyResult = service.multiply(func, func);
        TabulatedFunction divideResult = service.divide(func, func);

        // Assert
        // f + f = 2f
        assertEquals(4.0, addResult.getY(0), 1e-10); // 2 + 2 = 4
        assertEquals(8.0, addResult.getY(1), 1e-10); // 4 + 4 = 8
        assertEquals(12.0, addResult.getY(2), 1e-10); // 6 + 6 = 12

        // f - f = 0
        assertEquals(0.0, subtractResult.getY(0), 1e-10);
        assertEquals(0.0, subtractResult.getY(1), 1e-10);
        assertEquals(0.0, subtractResult.getY(2), 1e-10);

        // f * f = f²
        assertEquals(4.0, multiplyResult.getY(0), 1e-10); // 2 * 2 = 4
        assertEquals(16.0, multiplyResult.getY(1), 1e-10); // 4 * 4 = 16
        assertEquals(36.0, multiplyResult.getY(2), 1e-10); // 6 * 6 = 36

        // f / f = 1
        assertEquals(1.0, divideResult.getY(0), 1e-10);
        assertEquals(1.0, divideResult.getY(1), 1e-10);
        assertEquals(1.0, divideResult.getY(2), 1e-10);
    }

    @Test
    void testOperationsPreserveXValues() {
        // Arrange
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues = {-1.0, 0.0, 1.0, 2.0};
        double[] yValues1 = {1.0, 2.0, 3.0, 4.0};
        double[] yValues2 = {5.0, 6.0, 7.0, 8.0};
        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues, yValues2);

        // Act
        TabulatedFunction multiplyResult = service.multiply(func1, func2);
        TabulatedFunction divideResult = service.divide(func1, func2);

        // Assert - x-значения должны сохраниться
        for (int i = 0; i < xValues.length; i++) {
            assertEquals(xValues[i], multiplyResult.getX(i), 1e-10);
            assertEquals(xValues[i], divideResult.getX(i), 1e-10);
        }
    }

    @Test
    void testFactoryChangeAffectsNewOperations() {
        // Arrange
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        double[] xValues = {1.0, 2.0};
        double[] yValues1 = {3.0, 4.0};
        double[] yValues2 = {5.0, 6.0};
        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues, yValues2);

        // Act - меняем фабрику
        service.setFactory(new LinkedListTabulatedFunctionFactory());
        TabulatedFunction multiplyResult = service.multiply(func1, func2);
        TabulatedFunction divideResult = service.divide(func1, func2);

        // Assert - результаты должны быть нового типа
        assertTrue(multiplyResult instanceof LinkedListTabulatedFunction);
        assertTrue(divideResult instanceof LinkedListTabulatedFunction);

        // Проверяем корректность вычислений
        assertEquals(15.0, multiplyResult.getY(0), 1e-10); // 3 * 5 = 15
        assertEquals(24.0, multiplyResult.getY(1), 1e-10); // 4 * 6 = 24
        assertEquals(0.6, divideResult.getY(0), 1e-10);   // 3 / 5 = 0.6
        assertEquals(0.6666666667, divideResult.getY(1), 1e-10); // 4 / 6 ≈ 0.66
    }
}