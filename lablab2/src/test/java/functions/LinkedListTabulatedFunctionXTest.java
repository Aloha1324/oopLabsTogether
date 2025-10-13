package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LinkedListTabulatedFunctionXTest {

    static class TestableFunction extends LinkedListTabulatedFunctionX {
        public TestableFunction(double[] xValues, double[] yValues) {
            super(xValues, yValues);
        }

        public double testInterpolate(double x, int floorIndex) {
            return interpolate(x, floorIndex);
        }

        public int testFloorIndexOfX(double x) {
            return floorIndexOfX(x);
        }

        public double testExtrapolateLeft(double x) {
            return extrapolateLeft(x);
        }

        public double testExtrapolateRight(double x) {
            return extrapolateRight(x);
        }
    }

    @Test
    void testConstructors() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(20.0, function.getY(1), 1e-10);

        MathFunction identity = x -> x;
        LinkedListTabulatedFunctionX func2 = new LinkedListTabulatedFunctionX(identity, 0.0, 4.0, 5);
        assertEquals(5, func2.getCount());
        assertEquals(0.0, func2.leftBound(), 1e-10);
        assertEquals(4.0, func2.rightBound(), 1e-10);

        MathFunction square = x -> x * x;
        LinkedListTabulatedFunctionX func3 = new LinkedListTabulatedFunctionX(square, 5.0, 5.0, 3);
        assertEquals(3, func3.getCount());
        assertEquals(25.0, func3.getY(1), 1e-10);
    }

    @Test
    void testInvalidConstructors() {
        double[] xValues = {1.0};
        double[] yValues = {10.0, 20.0};
        MathFunction func = x -> x;

        assertThrows(IllegalArgumentException.class, () -> new LinkedListTabulatedFunctionX(xValues, yValues));
        assertThrows(IllegalArgumentException.class, () -> new LinkedListTabulatedFunctionX(null, 0, 1, 2));
        assertThrows(IllegalArgumentException.class, () -> new LinkedListTabulatedFunctionX(null, new double[]{1.0}));
        assertThrows(IllegalArgumentException.class, () -> new LinkedListTabulatedFunctionX(new double[0], new double[0]));
        assertThrows(IllegalArgumentException.class, () -> new LinkedListTabulatedFunctionX(func, 0, 1, 0));
    }

    @Test
    void testBasicOperations() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        function.setY(1, 25.0);
        assertEquals(25.0, function.getY(1), 1e-10);

        assertEquals(0, function.indexOfX(1.0));
        assertEquals(1, function.indexOfY(25.0));
        assertEquals(-1, function.indexOfX(5.0));

        assertEquals(1.0, function.leftBound(), 1e-10);
        assertEquals(3.0, function.rightBound(), 1e-10);
    }

    @Test
    void testFloorIndexOfXAllBranches() {
        double[] xValues = {1.0, 3.0, 5.0, 7.0};
        double[] yValues = {10.0, 30.0, 50.0, 70.0};
        TestableFunction function = new TestableFunction(xValues, yValues);

        assertEquals(0, function.testFloorIndexOfX(0.5), "x меньше головы");
        assertEquals(0, function.testFloorIndexOfX(2.0), "x между 1 и 3");
        assertEquals(1, function.testFloorIndexOfX(4.0), "x между 3 и 5");
        assertEquals(2, function.testFloorIndexOfX(6.0), "x между 5 и 7");
        assertEquals(0, function.testFloorIndexOfX(1.0), "x равен первому узлу");
        assertEquals(1, function.testFloorIndexOfX(3.0), "x равен второму узлу");
        assertEquals(3, function.testFloorIndexOfX(7.0), "x равен последнему узлу");
        assertEquals(3, function.testFloorIndexOfX(8.0), "x больше последнего узла");

        TestableFunction emptyFunction = new TestableFunction(new double[]{1.0}, new double[]{10.0});
        emptyFunction.remove(0);
        assertThrows(IllegalStateException.class, () -> emptyFunction.testFloorIndexOfX(1.0));
    }

    @Test
    void testFloorIndexOfXEdgeCases() {
        double[] twoX = {1.0, 3.0};
        double[] twoY = {10.0, 30.0};
        TestableFunction twoPointFunc = new TestableFunction(twoX, twoY);

        assertEquals(0, twoPointFunc.testFloorIndexOfX(0.0));
        assertEquals(0, twoPointFunc.testFloorIndexOfX(1.0));
        assertEquals(0, twoPointFunc.testFloorIndexOfX(2.0));
        assertEquals(1, twoPointFunc.testFloorIndexOfX(3.0));
        assertEquals(1, twoPointFunc.testFloorIndexOfX(4.0));

        // Для одинаковых x логика метода floorIndexOfX работает по-другому
        // При x=2.0 и узлах [2.0, 2.0, 2.0]:
        // - x >= current.x (2.0 >= 2.0) - true
        // - x < current.next.x (2.0 < 2.0) - false
        // Поэтому условие не выполняется и метод доходит до return count - 1
        double[] sameX = {2.0, 2.0, 2.0};
        double[] sameY = {10.0, 20.0, 30.0};
        TestableFunction sameXFunc = new TestableFunction(sameX, sameY);

        assertEquals(0, sameXFunc.testFloorIndexOfX(1.0));  // x < head.x → 0
        assertEquals(2, sameXFunc.testFloorIndexOfX(2.0));  // Все условия в цикле false → count-1
        assertEquals(2, sameXFunc.testFloorIndexOfX(3.0));  // x > всех узлов → count-1
    }

    @Test
    void testIndexOfYAllBranches() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        assertEquals(0, function.indexOfY(10.0), "Точное совпадение с первым y");
        assertEquals(1, function.indexOfY(20.0), "Точное совпадение со вторым y");
        assertEquals(2, function.indexOfY(30.0), "Точное совпадение с третьим y");

        assertEquals(0, function.indexOfY(10.0 + 1e-13), "Совпадение с погрешностью +");
        assertEquals(1, function.indexOfY(20.0 - 1e-13), "Совпадение с погрешностью -");
        assertEquals(2, function.indexOfY(30.0 + 0.5e-12), "Совпадение с половиной погрешности");

        assertEquals(-1, function.indexOfY(15.0), "Нет совпадения - середина");
        assertEquals(-1, function.indexOfY(5.0), "Нет совпадения - меньше");
        assertEquals(-1, function.indexOfY(35.0), "Нет совпадения - больше");
        assertEquals(-1, function.indexOfY(10.0000000001), "Нет совпадения - чуть больше погрешности");

        LinkedListTabulatedFunctionX emptyFunction = new LinkedListTabulatedFunctionX(
                new double[]{1.0}, new double[]{10.0});
        emptyFunction.remove(0);
        assertEquals(-1, emptyFunction.indexOfY(10.0), "Пустой список");
    }

    @Test
    void testIndexOfXAllBranches() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        assertEquals(0, function.indexOfX(1.0), "Точное совпадение с первым x");
        assertEquals(1, function.indexOfX(2.0), "Точное совпадение со вторым x");
        assertEquals(2, function.indexOfX(3.0), "Точное совпадение с третьим x");

        assertEquals(0, function.indexOfX(1.0 + 1e-13), "Совпадение с погрешностью +");
        assertEquals(1, function.indexOfX(2.0 - 1e-13), "Совпадение с погрешностью -");

        assertEquals(-1, function.indexOfX(1.5), "Нет совпадения - середина");
        assertEquals(-1, function.indexOfX(0.5), "Нет совпадения - меньше");
        assertEquals(-1, function.indexOfX(3.5), "Нет совпадения - больше");
        assertEquals(-1, function.indexOfX(1.0000000001), "Нет совпадения - чуть больше погрешности");

        LinkedListTabulatedFunctionX emptyFunction = new LinkedListTabulatedFunctionX(
                new double[]{1.0}, new double[]{10.0});
        emptyFunction.remove(0);
        assertEquals(-1, emptyFunction.indexOfX(1.0), "Пустой список");
    }

    @Test
    void testPrecisionInIndexSearch() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 2.0, 3.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        assertEquals(0, function.indexOfX(1.0 + 0.9e-12));
        assertEquals(1, function.indexOfY(2.0 - 0.9e-12));

        assertEquals(-1, function.indexOfX(1.0 + 1.1e-12));
        assertEquals(-1, function.indexOfY(2.0 - 1.1e-12));
    }

    @Test
    void testApplyOperations() {
        double[] xValues = {0.0, 2.0};
        double[] yValues = {0.0, 4.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        assertEquals(0.0, function.apply(0.0), 1e-10);
        assertEquals(4.0, function.apply(2.0), 1e-10);
        assertEquals(2.0, function.apply(1.0), 1e-10);
        assertEquals(-2.0, function.apply(-1.0), 1e-10);
        assertEquals(6.0, function.apply(3.0), 1e-10);
    }

    @Test
    void testSinglePointFunction() {
        double[] xValues = {5.0};
        double[] yValues = {10.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        assertEquals(10.0, function.apply(0.0), 1e-10);
        assertEquals(10.0, function.apply(5.0), 1e-10);
        assertEquals(10.0, function.apply(10.0), 1e-10);
    }

    @Test
    void testRemoveOperations() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        function.remove(1);
        assertEquals(3, function.getCount());
        assertEquals(3.0, function.getX(1), 1e-10);

        function.remove(0);
        assertEquals(2, function.getCount());
        assertEquals(3.0, function.leftBound(), 1e-10);

        function.remove(1);
        assertEquals(1, function.getCount());
        assertEquals(3.0, function.rightBound(), 1e-10);

        function.remove(0);
        assertEquals(0, function.getCount());
        assertThrows(IllegalStateException.class, () -> function.leftBound());
    }

    @Test
    void testInvalidIndexAccess() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        assertThrows(IndexOutOfBoundsException.class, () -> function.getX(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> function.getX(3));
        assertThrows(IllegalArgumentException.class, () -> function.remove(-1));
        assertThrows(IllegalArgumentException.class, () -> function.remove(3));
    }

    @Test
    void testInterpolateMethod() {
        TestableFunction singleFunc = new TestableFunction(new double[]{2.0}, new double[]{5.0});
        double result1 = singleFunc.testInterpolate(3.0, 0);
        assertEquals(5.0, result1, 1e-12);

        double[] multipleX = {1.0, 2.0, 3.0, 4.0};
        double[] multipleY = {1.0, 4.0, 9.0, 16.0};
        TestableFunction multiFunc = new TestableFunction(multipleX, multipleY);

        assertEquals(2.5, multiFunc.testInterpolate(1.5, 0), 1e-12);
        assertEquals(6.5, multiFunc.testInterpolate(2.5, 1), 1e-12);
        assertEquals(12.5, multiFunc.testInterpolate(3.5, 2), 1e-12);
        assertEquals(1.0, multiFunc.testInterpolate(1.0, 0), 1e-12);
        assertEquals(4.0, multiFunc.testInterpolate(2.0, 0), 1e-12);
    }

    @Test
    void testExtrapolateMethods() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 2.0};
        TestableFunction function = new TestableFunction(xValues, yValues);

        assertEquals(0.0, function.testExtrapolateLeft(0.0), 1e-10);
        assertEquals(-1.0, function.testExtrapolateLeft(-1.0), 1e-10);
        assertEquals(3.0, function.testExtrapolateRight(3.0), 1e-10);
        assertEquals(4.0, function.testExtrapolateRight(4.0), 1e-10);

        TestableFunction singleFunc = new TestableFunction(new double[]{5.0}, new double[]{10.0});
        assertEquals(10.0, singleFunc.testExtrapolateLeft(0.0), 1e-10);
        assertEquals(10.0, singleFunc.testExtrapolateRight(10.0), 1e-10);
    }

    @Test
    void testInterpolateAfterRemoval() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0, 16.0};
        TestableFunction function = new TestableFunction(xValues, yValues);

        function.remove(2);
        double result = function.testInterpolate(2.0, 1);
        assertEquals(5.0, result, 1e-12);

        function.remove(0);
        double result2 = function.testInterpolate(2.0, 0);
        assertEquals(5.0, result2, 1e-12);
    }

    @Test
    void testGetNodeOptimization() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0, 50.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(4.0, function.getX(3), 1e-10);
        assertEquals(5.0, function.getX(4), 1e-10);
    }

    @Test
    void testMultipleRemovals() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0, 50.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        function.remove(2);
        function.remove(0);
        function.remove(2);

        assertEquals(2, function.getCount());
        assertEquals(2.0, function.getX(0), 1e-10);
        assertEquals(4.0, function.getX(1), 1e-10);
        assertEquals(20.0, function.getY(0), 1e-10);
        assertEquals(40.0, function.getY(1), 1e-10);
    }

    @Test
    void testEmptyListBehavior() {
        double[] xValues = {1.0};
        double[] yValues = {10.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);
        function.remove(0);

        assertEquals(-1, function.indexOfX(1.0));
        assertEquals(-1, function.indexOfY(10.0));
        assertEquals(0, function.getCount());

        assertThrows(IllegalStateException.class, () -> function.leftBound());
        assertThrows(IllegalStateException.class, () -> function.rightBound());

        TestableFunction testFunc = new TestableFunction(new double[]{1.0}, new double[]{10.0});
        testFunc.remove(0);
        assertThrows(IllegalStateException.class, () -> testFunc.testFloorIndexOfX(1.0));
    }

    @Test
    void testComplexScenarios() {
        double[] xValues = {0.0, 1.0, 3.0, 6.0};
        double[] yValues = {0.0, 2.0, 8.0, 18.0};
        TestableFunction function = new TestableFunction(xValues, yValues);

        assertEquals(4, function.getCount());
        assertEquals(1.0, function.testInterpolate(0.5, 0), 1e-10);
        assertEquals(5.0, function.testInterpolate(2.0, 1), 1e-10);
        assertEquals(13.0, function.testInterpolate(4.5, 2), 1e-10);

        function.remove(3);
        assertEquals(3, function.getCount());
        assertEquals(5.0, function.testInterpolate(2.0, 1), 1e-10);
    }
}