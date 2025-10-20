package functions;

import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;
import exceptions.InterpolationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

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

    // Тесты для итератора

    @Test
    public void testIteratorThrowsExceptionOnNextWithoutHasNext() {
        double[] xValues = {0.0, 1.0};
        double[] yValues = {0.0, 1.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        Iterator<Point> iterator = function.iterator();

        // Получаем все элементы
        assertTrue(iterator.hasNext());
        iterator.next(); // Первый элемент
        assertTrue(iterator.hasNext());
        iterator.next(); // Второй элемент

        // Теперь итератор должен быть пустым
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testIteratorRemoveNotSupported() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        Iterator<Point> iterator = function.iterator();

        // Метод remove должен бросать UnsupportedOperationException
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }
}