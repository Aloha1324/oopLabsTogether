package functions.factory;

import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TabulatedFunctionFactoryTest {

    @Test
    void testArrayTabulatedFunctionFactory() {
        // Arrange
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};

        // Act
        TabulatedFunction function = factory.create(xValues, yValues);

        // Assert
        assertNotNull(function, "Созданная функция не должна быть null");
        assertTrue(function instanceof ArrayTabulatedFunction,
                "Фабрика должна создавать ArrayTabulatedFunction");
        assertEquals(4, function.getCount(), "Количество точек должно соответствовать входным данным");

        // Проверяем корректность данных - исправлено!
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);  // было 4.0 - неправильно
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(4.0, function.getX(3), 1e-10);

        assertEquals(1.0, function.getY(0), 1e-10);
        assertEquals(4.0, function.getY(1), 1e-10);
        assertEquals(9.0, function.getY(2), 1e-10);
        assertEquals(16.0, function.getY(3), 1e-10);
    }

    @Test
    void testArrayTabulatedFunctionFactoryWithDifferentData() {
        // Arrange
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {0.0, 0.5, 1.0};
        double[] yValues = {0.0, 0.25, 1.0};

        // Act
        TabulatedFunction function = factory.create(xValues, yValues);

        // Assert
        assertTrue(function instanceof ArrayTabulatedFunction);
        assertEquals(3, function.getCount());
        assertEquals(0.0, function.leftBound(), 1e-10);
        assertEquals(1.0, function.rightBound(), 1e-10);
    }

    @Test
    void testLinkedListTabulatedFunctionFactory() {
        // Arrange
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};

        // Act
        TabulatedFunction function = factory.create(xValues, yValues);

        // Assert
        assertNotNull(function, "Созданная функция не должна быть null");
        assertTrue(function instanceof LinkedListTabulatedFunction,
                "Фабрика должна создавать LinkedListTabulatedFunction");
        assertEquals(4, function.getCount(), "Количество точек должно соответствовать входным данным");

        // Проверяем корректность данных
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(4.0, function.getX(3), 1e-10);
        assertEquals(1.0, function.getY(0), 1e-10);
        assertEquals(4.0, function.getY(1), 1e-10);
        assertEquals(9.0, function.getY(2), 1e-10);
        assertEquals(16.0, function.getY(3), 1e-10);
    }

    @Test
    void testLinkedListTabulatedFunctionFactoryWithDifferentData() {
        // Arrange
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        double[] xValues = {-2.0, -1.0, 0.0, 1.0, 2.0};
        double[] yValues = {4.0, 1.0, 0.0, 1.0, 4.0};

        // Act
        TabulatedFunction function = factory.create(xValues, yValues);

        // Assert
        assertTrue(function instanceof LinkedListTabulatedFunction);
        assertEquals(5, function.getCount());
        assertEquals(-2.0, function.leftBound(), 1e-10);
        assertEquals(2.0, function.rightBound(), 1e-10);
    }

    @Test
    void testArrayFactoryCreatesCorrectType() {
        // Arrange
        TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 2.0};

        // Act
        TabulatedFunction function = arrayFactory.create(xValues, yValues);

        // Assert
        assertTrue(function instanceof ArrayTabulatedFunction,
                "ArrayTabulatedFunctionFactory должна создавать ArrayTabulatedFunction");
        assertFalse(function instanceof LinkedListTabulatedFunction,
                "ArrayTabulatedFunctionFactory не должна создавать LinkedListTabulatedFunction");
    }

    @Test
    void testLinkedListFactoryCreatesCorrectType() {
        // Arrange
        TabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 2.0};

        // Act
        TabulatedFunction function = linkedListFactory.create(xValues, yValues);

        // Assert
        assertTrue(function instanceof LinkedListTabulatedFunction,
                "LinkedListTabulatedFunctionFactory должна создавать LinkedListTabulatedFunction");
        assertFalse(function instanceof ArrayTabulatedFunction,
                "LinkedListTabulatedFunctionFactory не должна создавать ArrayTabulatedFunction");
    }

    @Test
    void testFactoryCreatedFunctionIsFunctional() {
        // Arrange
        TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
        TabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};

        // Act
        TabulatedFunction arrayFunction = arrayFactory.create(xValues, yValues);
        TabulatedFunction linkedListFunction = linkedListFactory.create(xValues, yValues);

        // Assert - проверяем, что созданные функции работают корректно
        assertEquals(0.0, arrayFunction.apply(0.0), 1e-10);
        assertEquals(1.0, arrayFunction.apply(1.0), 1e-10);
        assertEquals(4.0, arrayFunction.apply(2.0), 1e-10);

        assertEquals(0.0, linkedListFunction.apply(0.0), 1e-10);
        assertEquals(1.0, linkedListFunction.apply(1.0), 1e-10);
        assertEquals(4.0, linkedListFunction.apply(2.0), 1e-10);

        // Проверяем интерполяцию
        assertEquals(2.5, arrayFunction.apply(1.5), 1e-10);
        assertEquals(2.5, linkedListFunction.apply(1.5), 1e-10);
    }

    @Test
    void testFactoryCreatedFunctionHasCorrectBounds() {
        // Arrange
        TabulatedFunctionFactory factory1 = new ArrayTabulatedFunctionFactory();
        TabulatedFunctionFactory factory2 = new LinkedListTabulatedFunctionFactory();
        double[] xValues = {1.5, 2.5, 3.5};
        double[] yValues = {2.25, 6.25, 12.25};

        // Act
        TabulatedFunction arrayFunction = factory1.create(xValues, yValues);
        TabulatedFunction linkedListFunction = factory2.create(xValues, yValues);

        // Assert
        assertEquals(1.5, arrayFunction.leftBound(), 1e-10);
        assertEquals(3.5, arrayFunction.rightBound(), 1e-10);
        assertEquals(1.5, linkedListFunction.leftBound(), 1e-10);
        assertEquals(3.5, linkedListFunction.rightBound(), 1e-10);
    }

    @Test
    void testFactoryHandlesMinimumPoints() {
        // Arrange
        TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
        TabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0}; // минимальное количество точек
        double[] yValues = {1.0, 4.0};

        // Act & Assert - не должно быть исключений
        assertDoesNotThrow(() -> {
            TabulatedFunction arrayFunction = arrayFactory.create(xValues, yValues);
            TabulatedFunction linkedListFunction = linkedListFactory.create(xValues, yValues);

            assertEquals(2, arrayFunction.getCount());
            assertEquals(2, linkedListFunction.getCount());
        });
    }

    @Test
    void testFactoryPropagatesExceptions() {
        // Arrange
        TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
        TabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();

        // Test 1: Разные длины массивов
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0}; // разная длина

        // Проверяем, что оба фабрики бросают исключение (не обязательно одинаковое)
        assertThrows(Exception.class, () -> arrayFactory.create(xValues, yValues));
        assertThrows(Exception.class, () -> linkedListFactory.create(xValues, yValues));

        // Test 2: Меньше 2 точек
        double[] singleX = {1.0};
        double[] singleY = {2.0};

        assertThrows(Exception.class, () -> arrayFactory.create(singleX, singleY));
        assertThrows(Exception.class, () -> linkedListFactory.create(singleX, singleY));

        // Test 3: Пустые массивы
        double[] emptyX = {};
        double[] emptyY = {};

        assertThrows(Exception.class, () -> arrayFactory.create(emptyX, emptyY));
        assertThrows(Exception.class, () -> linkedListFactory.create(emptyX, emptyY));
    }

    @Test
    void testMultipleCreationsFromSameFactory() {
        // Arrange
        TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
        TabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();

        double[] xValues1 = {1.0, 2.0};
        double[] yValues1 = {1.0, 4.0};

        double[] xValues2 = {3.0, 4.0, 5.0};
        double[] yValues2 = {9.0, 16.0, 25.0};

        // Act
        TabulatedFunction function1 = arrayFactory.create(xValues1, yValues1);
        TabulatedFunction function2 = arrayFactory.create(xValues2, yValues2);

        TabulatedFunction function3 = linkedListFactory.create(xValues1, yValues1);
        TabulatedFunction function4 = linkedListFactory.create(xValues2, yValues2);

        // Assert
        assertTrue(function1 instanceof ArrayTabulatedFunction);
        assertTrue(function2 instanceof ArrayTabulatedFunction);
        assertTrue(function3 instanceof LinkedListTabulatedFunction);
        assertTrue(function4 instanceof LinkedListTabulatedFunction);

        assertEquals(2, function1.getCount());
        assertEquals(3, function2.getCount());
        assertEquals(2, function3.getCount());
        assertEquals(3, function4.getCount());
    }
}