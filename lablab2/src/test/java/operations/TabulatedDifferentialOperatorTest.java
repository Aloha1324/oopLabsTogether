package operations;

import functions.*;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class TabulatedDifferentialOperatorTest {

    static class TestSteppingDifferentialOperator extends SteppingDifferentialOperator {
        public TestSteppingDifferentialOperator(double step) {
            super(step);
        }

        @Override
        public MathFunction derive(MathFunction function) {
            return new MathFunction() {
                @Override
                public double apply(double x) {
                    return function.apply(x) * 2;
                }
            };
        }
    }

    // Внутренний класс для тестирования функции с одной точкой
    static class SinglePointTabulatedFunction implements TabulatedFunction {
        private final double x;
        private final double y;

        public SinglePointTabulatedFunction(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public double getX(int index) {
            if (index != 0) throw new IllegalArgumentException("Index must be 0 for single point function");
            return x;
        }

        @Override
        public double getY(int index) {
            if (index != 0) throw new IllegalArgumentException("Index must be 0 for single point function");
            return y;
        }

        @Override
        public void setY(int index, double value) {
            throw new UnsupportedOperationException("Not supported for single point function");
        }

        @Override
        public int indexOfX(double x) {
            return (Math.abs(this.x - x) < 1e-10) ? 0 : -1;
        }

        @Override
        public int indexOfY(double y) {
            return (Math.abs(this.y - y) < 1e-10) ? 0 : -1;
        }

        @Override
        public double leftBound() {
            return x;
        }

        @Override
        public double rightBound() {
            return x;
        }

        @Override
        public double apply(double x) {
            if (Math.abs(this.x - x) < 1e-10) {
                return y;
            }
            throw new IllegalArgumentException("x value is not in the function domain");
        }

        @Override
        public Iterator<Point> iterator() {
            return new Iterator<Point>() {
                private boolean hasNext = true;

                @Override
                public boolean hasNext() {
                    return hasNext;
                }

                @Override
                public Point next() {
                    if (!hasNext) {
                        throw new NoSuchElementException("No more points");
                    }
                    hasNext = false;
                    return new Point(x, y);
                }
            };
        }
    }

    // Специальная фабрика для тестирования, которая поддерживает одну точку
    static class SinglePointTabulatedFunctionFactory extends ArrayTabulatedFunctionFactory {
        @Override
        public TabulatedFunction create(double[] xValues, double[] yValues) {
            if (xValues.length == 1) {
                return new SinglePointTabulatedFunction(xValues[0], yValues[0]);
            }
            // Для двух и более точек используем родительскую реализацию
            return super.create(xValues, yValues);
        }
    }

    @Test
    void testDefaultConstructor() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        assertNotNull(operator.getFactory());
        assertTrue(operator.getFactory() instanceof ArrayTabulatedFunctionFactory);
    }

    @Test
    void testConstructorWithFactory() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(factory);
        assertNotNull(operator.getFactory());
        assertSame(factory, operator.getFactory());
        assertTrue(operator.getFactory() instanceof LinkedListTabulatedFunctionFactory);
    }

    @Test
    void testSetFactory() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        TabulatedFunctionFactory newFactory = new LinkedListTabulatedFunctionFactory();
        operator.setFactory(newFactory);
        assertSame(newFactory, operator.getFactory());
        assertTrue(operator.getFactory() instanceof LinkedListTabulatedFunctionFactory);
    }

    @Test
    void testDeriveLinearFunctionWithArrayFactory() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(
                new ArrayTabulatedFunctionFactory());
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 2.0, 4.0, 6.0};
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        TabulatedFunction derivative = operator.derive(function);

        assertNotNull(derivative);
        assertTrue(derivative instanceof ArrayTabulatedFunction);
        assertEquals(4, derivative.getCount());

        for (int i = 0; i < derivative.getCount(); i++) {
            assertEquals(2.0, derivative.getY(i), 1e-10);
        }
    }

    @Test
    void testDeriveLinearFunctionWithLinkedListFactory() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(
                new LinkedListTabulatedFunctionFactory());
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {5.0, 7.0, 9.0, 11.0};
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        TabulatedFunction derivative = operator.derive(function);

        assertNotNull(derivative);
        assertTrue(derivative instanceof LinkedListTabulatedFunction);
        assertEquals(4, derivative.getCount());

        for (int i = 0; i < derivative.getCount(); i++) {
            assertEquals(2.0, derivative.getY(i), 1e-10);
        }
    }

    @Test
    void testDeriveQuadraticFunction() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        TabulatedFunction derivative = operator.derive(function);

        assertEquals(4, derivative.getCount());
        assertEquals(1.0, derivative.getY(0), 1e-10);
        assertEquals(3.0, derivative.getY(1), 1e-10);
        assertEquals(5.0, derivative.getY(2), 1e-10);
        assertEquals(5.0, derivative.getY(3), 1e-10);
    }

    @Test
    void testDeriveConstantFunction() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {5.0, 5.0, 5.0, 5.0};
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        TabulatedFunction derivative = operator.derive(function);

        assertEquals(4, derivative.getCount());
        for (int i = 0; i < derivative.getCount(); i++) {
            assertEquals(0.0, derivative.getY(i), 1e-10);
        }
    }

    @Test
    void testDeriveTwoPointFunction() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        double[] xValues = {1.0, 3.0};
        double[] yValues = {2.0, 8.0};
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        TabulatedFunction derivative = operator.derive(function);

        assertEquals(2, derivative.getCount());
        assertEquals(3.0, derivative.getY(0), 1e-10);
        assertEquals(3.0, derivative.getY(1), 1e-10);
    }

    @Test
    void testDeriveSinglePointFunction() {
        // Используем специальную фабрику для тестирования одной точки
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(new SinglePointTabulatedFunctionFactory());
        double[] xValues = {1.0};
        double[] yValues = {5.0};
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        TabulatedFunction derivative = operator.derive(function);

        assertEquals(1, derivative.getCount());
        assertEquals(1.0, derivative.getX(0), 1e-10);
        assertEquals(0.0, derivative.getY(0), 1e-10);
    }

    @Test
    void testDeriveWithDifferentStepSizes() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        double[] xValues = {0.0, 0.5, 1.5, 2.0};
        double[] yValues = {0.0, 0.25, 2.25, 4.0};
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        TabulatedFunction derivative = operator.derive(function);

        assertEquals(4, derivative.getCount());
        assertEquals(0.5, derivative.getY(0), 1e-10);
        assertEquals(2.0, derivative.getY(1), 1e-10);
        assertEquals(3.5, derivative.getY(2), 1e-10);
        assertEquals(3.5, derivative.getY(3), 1e-10);
    }

    @Test
    void testDeriveUsingDirectAccessMethod() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {1.0, 2.0, 4.0, 8.0};
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        TabulatedFunction derivative1 = operator.derive(function);
        TabulatedFunction derivative2 = operator.deriveUsingDirectAccess(function);

        assertEquals(derivative1.getCount(), derivative2.getCount());
        for (int i = 0; i < derivative1.getCount(); i++) {
            assertEquals(derivative1.getX(i), derivative2.getX(i), 1e-10);
            assertEquals(derivative1.getY(i), derivative2.getY(i), 1e-10);
        }
    }

    @Test
    void testDeriveUsingDirectAccessWithSinglePoint() {
        // Используем специальную фабрику для тестирования одной точки
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(new SinglePointTabulatedFunctionFactory());
        double[] xValues = {2.0};
        double[] yValues = {3.0};
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        TabulatedFunction derivative = operator.deriveUsingDirectAccess(function);

        assertEquals(1, derivative.getCount());
        assertEquals(2.0, derivative.getX(0), 1e-10);
        assertEquals(0.0, derivative.getY(0), 1e-10);
    }

    @Test
    void testDerivePreservesXValues() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        double[] xValues = {-2.0, -1.0, 0.0, 1.0, 2.0};
        double[] yValues = {4.0, 1.0, 0.0, 1.0, 4.0};
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        TabulatedFunction derivative = operator.derive(function);

        assertEquals(5, derivative.getCount());
        for (int i = 0; i < derivative.getCount(); i++) {
            assertEquals(xValues[i], derivative.getX(i), 1e-10);
        }
    }

    @Test
    void testFactoryIntegration() {
        TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
        TabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();

        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};

        TabulatedFunction function1 = arrayFactory.create(xValues, yValues);
        TabulatedFunction function2 = linkedListFactory.create(xValues, yValues);

        TabulatedDifferentialOperator operator1 = new TabulatedDifferentialOperator(arrayFactory);
        TabulatedDifferentialOperator operator2 = new TabulatedDifferentialOperator(linkedListFactory);

        TabulatedFunction derivative1 = operator1.derive(function1);
        TabulatedFunction derivative2 = operator2.derive(function2);

        assertTrue(derivative1 instanceof ArrayTabulatedFunction);
        assertTrue(derivative2 instanceof LinkedListTabulatedFunction);
        assertEquals(derivative1.getCount(), derivative2.getCount());
        for (int i = 0; i < derivative1.getCount(); i++) {
            assertEquals(derivative1.getY(i), derivative2.getY(i), 1e-10);
        }
    }

    @Test
    void testSteppingOperatorConstructorWithValidStep() {
        TestSteppingDifferentialOperator operator = new TestSteppingDifferentialOperator(0.1);
        assertEquals(0.1, operator.getStep(), 1e-10);
    }

    @Test
    void testSteppingOperatorConstructorWithZeroStep() {
        assertThrows(IllegalArgumentException.class, () ->
                new TestSteppingDifferentialOperator(0.0));
    }

    @Test
    void testSteppingOperatorConstructorWithNegativeStep() {
        assertThrows(IllegalArgumentException.class, () ->
                new TestSteppingDifferentialOperator(-0.1));
    }

    @Test
    void testSteppingOperatorConstructorWithNaNStep() {
        assertThrows(IllegalArgumentException.class, () ->
                new TestSteppingDifferentialOperator(Double.NaN));
    }

    @Test
    void testSteppingOperatorConstructorWithPositiveInfinityStep() {
        assertThrows(IllegalArgumentException.class, () ->
                new TestSteppingDifferentialOperator(Double.POSITIVE_INFINITY));
    }

    @Test
    void testSteppingOperatorConstructorWithNegativeInfinityStep() {
        assertThrows(IllegalArgumentException.class, () ->
                new TestSteppingDifferentialOperator(Double.NEGATIVE_INFINITY));
    }

    @Test
    void testSteppingOperatorGetStep() {
        TestSteppingDifferentialOperator operator = new TestSteppingDifferentialOperator(0.5);
        double step = operator.getStep();
        assertEquals(0.5, step, 1e-10);
    }

    @Test
    void testSteppingOperatorSetStepWithValidValue() {
        TestSteppingDifferentialOperator operator = new TestSteppingDifferentialOperator(0.1);
        operator.setStep(0.05);
        assertEquals(0.05, operator.getStep(), 1e-10);
    }

    @Test
    void testSteppingOperatorSetStepWithZero() {
        TestSteppingDifferentialOperator operator = new TestSteppingDifferentialOperator(0.1);
        assertThrows(IllegalArgumentException.class, () -> operator.setStep(0.0));
        assertEquals(0.1, operator.getStep(), 1e-10);
    }

    @Test
    void testSteppingOperatorSetStepWithNegativeValue() {
        TestSteppingDifferentialOperator operator = new TestSteppingDifferentialOperator(0.1);
        assertThrows(IllegalArgumentException.class, () -> operator.setStep(-0.1));
        assertEquals(0.1, operator.getStep(), 1e-10);
    }

    @Test
    void testSteppingOperatorSetStepWithNaN() {
        TestSteppingDifferentialOperator operator = new TestSteppingDifferentialOperator(0.1);
        assertThrows(IllegalArgumentException.class, () -> operator.setStep(Double.NaN));
        assertEquals(0.1, operator.getStep(), 1e-10);
    }

    @Test
    void testSteppingOperatorSetStepWithPositiveInfinity() {
        TestSteppingDifferentialOperator operator = new TestSteppingDifferentialOperator(0.1);
        assertThrows(IllegalArgumentException.class, () -> operator.setStep(Double.POSITIVE_INFINITY));
        assertEquals(0.1, operator.getStep(), 1e-10);
    }

    @Test
    void testSteppingOperatorSetStepWithNegativeInfinity() {
        TestSteppingDifferentialOperator operator = new TestSteppingDifferentialOperator(0.1);
        assertThrows(IllegalArgumentException.class, () -> operator.setStep(Double.NEGATIVE_INFINITY));
        assertEquals(0.1, operator.getStep(), 1e-10);
    }

    @Test
    void testSteppingOperatorMultipleStepChanges() {
        TestSteppingDifferentialOperator operator = new TestSteppingDifferentialOperator(0.1);
        operator.setStep(0.2);
        operator.setStep(0.3);
        operator.setStep(0.4);
        assertEquals(0.4, operator.getStep(), 1e-10);
    }

    @Test
    void testSteppingOperatorDeriveMethod() {
        TestSteppingDifferentialOperator operator = new TestSteppingDifferentialOperator(0.1);
        MathFunction testFunction = x -> x * x;
        MathFunction derivative = operator.derive(testFunction);
        assertNotNull(derivative);
        assertEquals(32.0, derivative.apply(4.0), 1e-10);
    }

    @Test
    void testSteppingOperatorExceptionMessages() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> new TestSteppingDifferentialOperator(-1.0));
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Step must be positive finite number"));
    }

    @Test
    void testLeftSteppingDifferentialOperator() {
        LeftSteppingDifferentialOperator operator = new LeftSteppingDifferentialOperator(0.1);
        MathFunction sqrFunction = x -> x * x;
        MathFunction derivative = operator.derive(sqrFunction);
        assertNotNull(derivative);
        assertEquals(3.9, derivative.apply(2.0), 0.1);
    }

    @Test
    void testRightSteppingDifferentialOperator() {
        RightSteppingDifferentialOperator operator = new RightSteppingDifferentialOperator(0.1);
        MathFunction sqrFunction = x -> x * x;
        MathFunction derivative = operator.derive(sqrFunction);
        assertNotNull(derivative);
        assertEquals(4.1, derivative.apply(2.0), 0.1);
    }

    @Test
    void testMiddleSteppingDifferentialOperator() {
        MiddleSteppingDifferentialOperator operator = new MiddleSteppingDifferentialOperator(0.1);
        MathFunction sqrFunction = x -> x * x;
        MathFunction derivative = operator.derive(sqrFunction);
        assertNotNull(derivative);
        assertEquals(4.0, derivative.apply(2.0), 0.1);
    }

    @Test
    void testOperatorsWithDifferentSteps() {
        double[] steps = {0.01, 0.001, 0.0001};
        MathFunction linearFunction = x -> 3 * x + 2;

        for (double step : steps) {
            LeftSteppingDifferentialOperator leftOp = new LeftSteppingDifferentialOperator(step);
            RightSteppingDifferentialOperator rightOp = new RightSteppingDifferentialOperator(step);
            MiddleSteppingDifferentialOperator middleOp = new MiddleSteppingDifferentialOperator(step);

            MathFunction leftDerivative = leftOp.derive(linearFunction);
            MathFunction rightDerivative = rightOp.derive(linearFunction);
            MathFunction middleDerivative = middleOp.derive(linearFunction);

            assertEquals(3.0, leftDerivative.apply(5.0), 1e-10);
            assertEquals(3.0, rightDerivative.apply(5.0), 1e-10);
            assertEquals(3.0, middleDerivative.apply(5.0), 1e-10);
        }
    }

    @Test
    void testDeriveWithNonUniformXValues() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        double[] xValues = {0.0, 0.5, 1.2, 2.0};
        double[] yValues = {0.0, 0.25, 1.44, 4.0};
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        TabulatedFunction derivative = operator.derive(function);

        assertEquals(4, derivative.getCount());
        assertEquals(0.5, derivative.getY(0), 1e-10);
        assertEquals((1.44 - 0.25) / (1.2 - 0.5), derivative.getY(1), 1e-10);
        assertEquals((4.0 - 1.44) / (2.0 - 1.2), derivative.getY(2), 1e-10);
        assertEquals(derivative.getY(2), derivative.getY(3), 1e-10);
    }

    @Test
    void testDeriveWithComplexXValues() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        double[] xValues = {-2.0, -1.0, 0.5, 1.0, 2.5};
        double[] yValues = {4.0, 1.0, 0.25, 1.0, 6.25};
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        TabulatedFunction derivative = operator.derive(function);

        assertEquals(5, derivative.getCount());
        assertEquals((1.0 - 4.0) / (-1.0 - (-2.0)), derivative.getY(0), 1e-10);
        assertEquals((0.25 - 1.0) / (0.5 - (-1.0)), derivative.getY(1), 1e-10);
        assertEquals((1.0 - 0.25) / (1.0 - 0.5), derivative.getY(2), 1e-10);
        assertEquals((6.25 - 1.0) / (2.5 - 1.0), derivative.getY(3), 1e-10);
        assertEquals(derivative.getY(3), derivative.getY(4), 1e-10);
    }

    @Test
    void testDeriveUsingDirectAccessWithNonUniformX() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        double[] xValues = {0.0, 1.0, 3.0, 6.0};
        double[] yValues = {0.0, 1.0, 9.0, 36.0};
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        TabulatedFunction derivative = operator.deriveUsingDirectAccess(function);

        assertEquals(4, derivative.getCount());
        assertEquals(1.0, derivative.getY(0), 1e-10);
        assertEquals(4.0, derivative.getY(1), 1e-10);
        assertEquals(9.0, derivative.getY(2), 1e-10);
        assertEquals(9.0, derivative.getY(3), 1e-10);
    }

    @Test
    void testDeriveWithMinimumPoints() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        TabulatedFunction derivative = operator.derive(function);

        assertEquals(2, derivative.getCount());
        assertEquals(3.0, derivative.getY(0), 1e-10);
        assertEquals(3.0, derivative.getY(1), 1e-10);
    }

    @Test
    void testDeriveWithThreePoints() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        TabulatedFunction function = operator.getFactory().create(xValues, yValues);

        TabulatedFunction derivative = operator.derive(function);

        assertEquals(3, derivative.getCount());
        assertEquals(1.0, derivative.getY(0), 1e-10);
        assertEquals(3.0, derivative.getY(1), 1e-10);
        assertEquals(3.0, derivative.getY(2), 1e-10);
    }

    // Дополнительный тест для проверки итератора
    @Test
    void testSinglePointFunctionIterator() {
        SinglePointTabulatedFunction function = new SinglePointTabulatedFunction(1.0, 2.0);
        Iterator<Point> iterator = function.iterator();

        assertTrue(iterator.hasNext());
        Point point = iterator.next();
        assertEquals(1.0, point.x, 1e-10);
        assertEquals(2.0, point.y, 1e-10);

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }
}