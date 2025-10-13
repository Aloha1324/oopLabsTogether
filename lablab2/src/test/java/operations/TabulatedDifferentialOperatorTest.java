package operations;

import functions.*;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TabulatedDifferentialOperatorTest {

    @Test
    void testDefaultConstructor() {
        // Arrange & Act
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();

        // Assert
        assertNotNull(operator.getFactory());
        assertTrue(operator.getFactory() instanceof ArrayTabulatedFunctionFactory);
    }

    @Test
    void testConstructorWithFactory() {
        // Arrange
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();

        // Act
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(factory);

        // Assert
        assertNotNull(operator.getFactory());
        assertSame(factory, operator.getFactory());
        assertTrue(operator.getFactory() instanceof LinkedListTabulatedFunctionFactory);
    }

    @Test
    void testSetFactory() {
        // Arrange
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        TabulatedFunctionFactory newFactory = new LinkedListTabulatedFunctionFactory();

        // Act
        operator.setFactory(newFactory);

        // Assert
        assertSame(newFactory, operator.getFactory());
        assertTrue(operator.getFactory() instanceof LinkedListTabulatedFunctionFactory);
    }

    @Test
    void testDeriveLinearFunctionWithArrayFactory() {
        // Arrange
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(
                new ArrayTabulatedFunctionFactory());
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 2.0, 4.0, 6.0}; // y = 2x
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        // Act
        TabulatedFunction derivative = operator.derive(function);

        // Assert
        assertNotNull(derivative);
        assertTrue(derivative instanceof ArrayTabulatedFunction);
        assertEquals(4, derivative.getCount());

        // Производная линейной функции должна быть постоянной
        for (int i = 0; i < derivative.getCount(); i++) {
            assertEquals(2.0, derivative.getY(i), 1e-10,
                    "Производная линейной функции y=2x должна быть равна 2");
        }
    }

    @Test
    void testDeriveLinearFunctionWithLinkedListFactory() {
        // Arrange
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(
                new LinkedListTabulatedFunctionFactory());
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {5.0, 7.0, 9.0, 11.0}; // y = 2x + 5
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        // Act
        TabulatedFunction derivative = operator.derive(function);

        // Assert
        assertNotNull(derivative);
        assertTrue(derivative instanceof LinkedListTabulatedFunction);
        assertEquals(4, derivative.getCount());

        // Производная линейной функции должна быть постоянной
        for (int i = 0; i < derivative.getCount(); i++) {
            assertEquals(2.0, derivative.getY(i), 1e-10,
                    "Производная линейной функции y=2x+5 должна быть равна 2");
        }
    }

    @Test
    void testDeriveQuadraticFunction() {
        // Arrange
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0}; // y = x²
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        // Act
        TabulatedFunction derivative = operator.derive(function);

        // Assert
        assertEquals(4, derivative.getCount());

        // Проверяем численные производные
        // В точке x=0.5 (между 0 и 1): (1-0)/(1-0) = 1
        assertEquals(1.0, derivative.getY(0), 1e-10);

        // В точке x=1.5 (между 1 и 2): (4-1)/(2-1) = 3
        assertEquals(3.0, derivative.getY(1), 1e-10);

        // В точке x=2.5 (между 2 и 3): (9-4)/(3-2) = 5
        assertEquals(5.0, derivative.getY(2), 1e-10);

        // Последняя точка такая же как предпоследняя
        assertEquals(5.0, derivative.getY(3), 1e-10);
    }

    @Test
    void testDeriveConstantFunction() {
        // Arrange
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {5.0, 5.0, 5.0, 5.0}; // y = 5 (постоянная функция)
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        // Act
        TabulatedFunction derivative = operator.derive(function);

        // Assert
        assertEquals(4, derivative.getCount());

        // Производная постоянной функции должна быть 0 везде
        for (int i = 0; i < derivative.getCount(); i++) {
            assertEquals(0.0, derivative.getY(i), 1e-10,
                    "Производная постоянной функции должна быть 0");
        }
    }

    @Test
    void testDeriveTwoPointFunction() {
        // Arrange
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        double[] xValues = {1.0, 3.0};
        double[] yValues = {2.0, 8.0}; // наклон: (8-2)/(3-1) = 3
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        // Act
        TabulatedFunction derivative = operator.derive(function);

        // Assert
        assertEquals(2, derivative.getCount());

        // Первая точка: производная = наклону
        assertEquals(3.0, derivative.getY(0), 1e-10);

        // Вторая точка: такая же как первая
        assertEquals(3.0, derivative.getY(1), 1e-10);
    }

    @Test
    void testDeriveWithDifferentStepSizes() {
        // Arrange
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        double[] xValues = {0.0, 0.5, 1.5, 2.0};
        double[] yValues = {0.0, 0.25, 2.25, 4.0}; // y = x²
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        // Act
        TabulatedFunction derivative = operator.derive(function);

        // Assert
        assertEquals(4, derivative.getCount());

        // Проверяем производные с разными шагами
        // Между 0.0 и 0.5: (0.25-0)/(0.5-0) = 0.5
        assertEquals(0.5, derivative.getY(0), 1e-10);

        // Между 0.5 и 1.5: (2.25-0.25)/(1.5-0.5) = 2.0
        assertEquals(2.0, derivative.getY(1), 1e-10);

        // Между 1.5 и 2.0: (4.0-2.25)/(2.0-1.5) = 3.5
        assertEquals(3.5, derivative.getY(2), 1e-10);

        // Последняя точка
        assertEquals(3.5, derivative.getY(3), 1e-10);
    }

    @Test
    void testDeriveUsingDirectAccessMethod() {
        // Arrange
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {1.0, 2.0, 4.0, 8.0}; // произвольная функция
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        // Act
        TabulatedFunction derivative1 = operator.derive(function);
        TabulatedFunction derivative2 = operator.deriveUsingDirectAccess(function);

        // Assert - оба метода должны давать одинаковый результат
        assertEquals(derivative1.getCount(), derivative2.getCount());

        for (int i = 0; i < derivative1.getCount(); i++) {
            assertEquals(derivative1.getX(i), derivative2.getX(i), 1e-10);
            assertEquals(derivative1.getY(i), derivative2.getY(i), 1e-10);
        }
    }

    @Test
    void testDerivePreservesXValues() {
        // Arrange
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        double[] xValues = {-2.0, -1.0, 0.0, 1.0, 2.0};
        double[] yValues = {4.0, 1.0, 0.0, 1.0, 4.0}; // y = x²
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        // Act
        TabulatedFunction derivative = operator.derive(function);

        // Assert - x-значения должны сохраниться
        assertEquals(5, derivative.getCount());
        for (int i = 0; i < derivative.getCount(); i++) {
            assertEquals(xValues[i], derivative.getX(i), 1e-10,
                    "X-значения должны остаться неизменными");
        }
    }

    @Test
    void testFactoryIntegration() {
        // Arrange
        TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
        TabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();

        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};

        TabulatedFunction function1 = arrayFactory.create(xValues, yValues);
        TabulatedFunction function2 = linkedListFactory.create(xValues, yValues);

        TabulatedDifferentialOperator operator1 = new TabulatedDifferentialOperator(arrayFactory);
        TabulatedDifferentialOperator operator2 = new TabulatedDifferentialOperator(linkedListFactory);

        // Act
        TabulatedFunction derivative1 = operator1.derive(function1);
        TabulatedFunction derivative2 = operator2.derive(function2);

        // Assert - производные должны иметь правильные типы
        assertTrue(derivative1 instanceof ArrayTabulatedFunction);
        assertTrue(derivative2 instanceof LinkedListTabulatedFunction);

        // И одинаковые значения
        assertEquals(derivative1.getCount(), derivative2.getCount());
        for (int i = 0; i < derivative1.getCount(); i++) {
            assertEquals(derivative1.getY(i), derivative2.getY(i), 1e-10);
        }
    }
}