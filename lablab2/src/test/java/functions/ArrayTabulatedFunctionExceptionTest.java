package functions;

import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;
import exceptions.InterpolationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Iterator;

/**
 * Тестовый класс для проверки исключений и функционала ArrayTabulatedFunction
 */
public class ArrayTabulatedFunctionExceptionTest {

    // Тесты для статических методов проверки AbstractTabulatedFunction

    @Test
    public void testCheckLengthIsTheSameThrowsException() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0};
        assertThrows(DifferentLengthOfArraysException.class, () -> {
            AbstractTabulatedFunction.checkLengthIsTheSame(xValues, yValues);
        });
    }

    @Test
    public void testCheckLengthIsTheSameWithEmptyArrays() {
        double[] xValues = {};
        double[] yValues = {};
        assertDoesNotThrow(() -> {
            AbstractTabulatedFunction.checkLengthIsTheSame(xValues, yValues);
        });
    }

    @Test
    public void testCheckLengthIsTheSameWithNullArrays() {
        assertThrows(NullPointerException.class, () -> {
            AbstractTabulatedFunction.checkLengthIsTheSame(null, new double[]{1.0});
        });
        assertThrows(NullPointerException.class, () -> {
            AbstractTabulatedFunction.checkLengthIsTheSame(new double[]{1.0}, null);
        });
    }

    @Test
    public void testCheckLengthIsTheSameDoesNotThrowForSameLength() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        assertDoesNotThrow(() -> {
            AbstractTabulatedFunction.checkLengthIsTheSame(xValues, yValues);
        });
    }

    @Test
    public void testCheckSortedThrowsExceptionForUnsortedArray() {
        double[] xValues = {1.0, 3.0, 2.0};
        assertThrows(ArrayIsNotSortedException.class, () -> {
            AbstractTabulatedFunction.checkSorted(xValues);
        });
    }

    @Test
    public void testCheckSortedThrowsExceptionForEqualValues() {
        double[] xValues = {1.0, 2.0, 2.0};
        assertThrows(ArrayIsNotSortedException.class, () -> {
            AbstractTabulatedFunction.checkSorted(xValues);
        });
    }

    @Test
    public void testCheckSortedWithSingleElement() {
        double[] xValues = {1.0};
        assertDoesNotThrow(() -> {
            AbstractTabulatedFunction.checkSorted(xValues);
        });
    }

    @Test
    public void testCheckSortedWithEmptyArray() {
        double[] xValues = {};
        assertDoesNotThrow(() -> {
            AbstractTabulatedFunction.checkSorted(xValues);
        });
    }

    @Test
    public void testCheckSortedWithNullArray() {
        assertThrows(NullPointerException.class, () -> {
            AbstractTabulatedFunction.checkSorted(null);
        });
    }

    @Test
    public void testCheckSortedDoesNotThrowForSortedArray() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        assertDoesNotThrow(() -> {
            AbstractTabulatedFunction.checkSorted(xValues);
        });
    }

    // Тесты для конструкторов ArrayTabulatedFunction

    @Test
    public void testConstructorThrowsDifferentLengthException() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0};
        assertThrows(DifferentLengthOfArraysException.class, () -> {
            new ArrayTabulatedFunction(xValues, yValues);
        });
    }

    @Test
    public void testConstructorThrowsArrayNotSortedException() {
        double[] xValues = {1.0, 3.0, 2.0};
        double[] yValues = {1.0, 4.0, 9.0};
        assertThrows(ArrayIsNotSortedException.class, () -> {
            new ArrayTabulatedFunction(xValues, yValues);
        });
    }

    @Test
    public void testConstructorThrowsIllegalArgumentExceptionForSinglePoint() {
        double[] singleX = {1.0};
        double[] singleY = {1.0};
        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunction(singleX, singleY);
        });
    }

    @Test
    public void testConstructorThrowsIllegalArgumentExceptionForEmptyArrays() {
        double[] emptyX = {};
        double[] emptyY = {};
        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunction(emptyX, emptyY);
        });
    }

    @Test
    public void testConstructorWithNullArrays() {
        assertThrows(NullPointerException.class, () -> {
            new ArrayTabulatedFunction(null, new double[]{1.0, 2.0});
        });
        assertThrows(NullPointerException.class, () -> {
            new ArrayTabulatedFunction(new double[]{1.0, 2.0}, null);
        });
    }

    @Test
    public void testFunctionConstructorThrowsIllegalArgumentException() {
        MathFunction func = x -> x * x;
        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunction(func, 0.0, 1.0, 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunction(func, 2.0, 1.0, 5);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunction(func, 1.0, 1.0, 5);
        });
    }

    @Test
    public void testFunctionConstructorWithNullFunction() {
        assertThrows(NullPointerException.class, () -> {
            new ArrayTabulatedFunction(null, 0.0, 1.0, 5);
        });
    }

    // Тесты для методов интерполяции

    @Test
    public void testInterpolateThrowsInterpolationException() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        assertThrows(InterpolationException.class, () -> {
            function.interpolate(-0.5, 0);
        });
        assertThrows(InterpolationException.class, () -> {
            function.interpolate(1.5, 0);
        });
    }

    @Test
    public void testInterpolateDoesNotThrowForValidX() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        assertDoesNotThrow(() -> {
            function.interpolate(0.5, 0);
        });
        assertDoesNotThrow(() -> {
            function.interpolate(0.0, 0);
        });
        assertDoesNotThrow(() -> {
            function.interpolate(1.0, 0);
        });
    }

    @Test
    public void testInterpolateThrowsIllegalArgumentExceptionForInvalidIndex() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        assertThrows(IllegalArgumentException.class, () -> {
            function.interpolate(0.5, -1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            function.interpolate(0.5, 2);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            function.interpolate(0.5, 3);
        });
    }

    // Тесты для методов доступа

    @Test
    public void testGetXThrowsIllegalArgumentException() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        assertThrows(IllegalArgumentException.class, () -> {
            function.getX(-1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            function.getX(3);
        });
    }

    @Test
    public void testGetYThrowsIllegalArgumentException() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        assertThrows(IllegalArgumentException.class, () -> {
            function.getY(-1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            function.getY(3);
        });
    }

    @Test
    public void testSetYThrowsIllegalArgumentException() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        assertThrows(IllegalArgumentException.class, () -> {
            function.setY(-1, 5.0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            function.setY(3, 5.0);
        });
    }

    // Проверка порядка проверок в конструкторе

    @Test
    public void testConstructorOrderOfChecks() {
        double[] singleX = {1.0};
        double[] singleY = {1.0};
        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunction(singleX, singleY);
        });
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0};
        assertThrows(DifferentLengthOfArraysException.class, () -> {
            new ArrayTabulatedFunction(xValues, yValues);
        });
        double[] unsortedX = {1.0, 3.0, 2.0};
        double[] unsortedY = {1.0, 4.0, 9.0};
        assertThrows(ArrayIsNotSortedException.class, () -> {
            new ArrayTabulatedFunction(unsortedX, unsortedY);
        });
    }

    // Тесты для метода apply с пустой функцией

    @Test
    public void testApplyThrowsExceptionForEmptyFunction() {
        ArrayTabulatedFunction emptyFunction = createEmptyFunctionForTesting();
        assertThrows(IllegalStateException.class, () -> {
            emptyFunction.apply(1.0);
        });
    }

    private ArrayTabulatedFunction createEmptyFunctionForTesting() {
        return new ArrayTabulatedFunction(new double[]{0.0, 1.0}, new double[]{0.0, 1.0}) {
            @Override
            public int getCount() {
                return 0;
            }
        };
    }

    @Test
    public void testExtrapolationWithMinimalPoints() {
        double[] xValues = {0.0, 1.0};
        double[] yValues = {0.0, 1.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        assertDoesNotThrow(() -> {
            function.apply(-0.5);
        });
        assertDoesNotThrow(() -> {
            function.apply(1.5);
        });
    }

    @Test
    public void testFloorIndexOfXWithNormalFunction() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        assertEquals(0, function.floorIndexOfX(-1.0));
        assertEquals(1, function.floorIndexOfX(1.5));
        assertEquals(2, function.floorIndexOfX(2.0));
        assertEquals(2, function.floorIndexOfX(4.0));
    }

    @Test
    public void testFloorIndexOfXWithExactMatch() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        assertEquals(0, function.floorIndexOfX(0.0));
        assertEquals(1, function.floorIndexOfX(1.0));
        assertEquals(2, function.floorIndexOfX(2.0));
    }

    @Test
    public void testApplyWithVariousScenarios() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        assertDoesNotThrow(() -> function.apply(-1.0));
        assertDoesNotThrow(() -> function.apply(3.0));
        assertEquals(1.0, function.apply(1.0));
        assertDoesNotThrow(() -> function.apply(0.5));
    }

    @Test
    public void testApplyWithExactXMatch() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        assertEquals(0.0, function.apply(0.0));
        assertEquals(1.0, function.apply(1.0));
        assertEquals(4.0, function.apply(2.0));
    }

    @Test
    public void testInsertFunctionality() {
        double[] xValues = {0.0, 2.0};
        double[] yValues = {0.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        assertDoesNotThrow(() -> function.insert(1.0, 1.0));
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getY(1));
        assertDoesNotThrow(() -> function.insert(1.0, 2.0));
        assertEquals(3, function.getCount());
        assertEquals(2.0, function.getY(1));
    }

    @Test
    public void testInsertAtBeginning() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        assertDoesNotThrow(() -> function.insert(0.0, 0.0));
        assertEquals(3, function.getCount());
        assertEquals(0.0, function.getX(0));
        assertEquals(0.0, function.getY(0));
    }

    @Test
    public void testInsertAtEnd() {
        double[] xValues = {0.0, 1.0};
        double[] yValues = {0.0, 1.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        assertDoesNotThrow(() -> function.insert(2.0, 4.0));
        assertEquals(3, function.getCount());
        assertEquals(2.0, function.getX(2));
        assertEquals(4.0, function.getY(2));
    }

    @Test
    public void testEqualsAndHashCode() {
        double[] xValues1 = {0.0, 1.0, 2.0};
        double[] yValues1 = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function1 = new ArrayTabulatedFunction(xValues1, yValues1);
        double[] xValues2 = {0.0, 1.0, 2.0};
        double[] yValues2 = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function2 = new ArrayTabulatedFunction(xValues2, yValues2);
        double[] xValues3 = {0.0, 1.0, 3.0};
        double[] yValues3 = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function3 = new ArrayTabulatedFunction(xValues3, yValues3);
        assertEquals(function1, function2);
        assertNotEquals(function1, function3);
        assertNotEquals(function1, null);
        assertNotEquals(function1, "not a function");
        assertEquals(function1.hashCode(), function2.hashCode());
    }

    @Test
    public void testToString() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        String result = function.toString();
        assertNotNull(result);
        assertTrue(result.contains("ArrayTabulatedFunction"));
        assertTrue(result.contains("size = 3"));
    }

    @Test
    public void testClone() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        ArrayTabulatedFunction cloned = function.clone();
        assertNotSame(function, cloned);
        assertEquals(function, cloned);
        cloned.setY(1, 2.0);
        assertNotEquals(function.getY(1), cloned.getY(1));
    }

    // Тесты для методов iterator

    @Test
    public void testIteratorUsingWhileLoop() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        Iterator<Point> iterator = function.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Point point = iterator.next();
            assertNotNull(point);
            assertEquals(xValues[index], point.x);
            assertEquals(yValues[index], point.y);
            index++;
        }
        assertEquals(xValues.length, index);
    }

    @Test
    public void testIteratorUsingForEachLoop() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        int index = 0;
        for (Point point : function) {
            assertNotNull(point);
            assertEquals(xValues[index], point.x);
            assertEquals(yValues[index], point.y);
            index++;
        }
        assertEquals(xValues.length, index);
    }

    @Test
    public void testIteratorThrowsExceptionOnNextWithoutHasNext() {
        double[] xValues = {0.0};
        double[] yValues = {0.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        Iterator<Point> iterator = function.iterator();
        assertTrue(iterator.hasNext());
        assertNotNull(iterator.next());
        assertFalse(iterator.hasNext());
        assertThrows(java.util.NoSuchElementException.class, iterator::next);
    }
}
