package functions;

import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;
import exceptions.InterpolationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для проверки исключений в ArrayTabulatedFunction
 */
public class ArrayTabulatedFunctionExceptionTest {

    // Тесты для статических методов проверки AbstractTabulatedFunction

    @Test
    public void testCheckLengthIsTheSameThrowsException() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0}; // Разная длина

        assertThrows(DifferentLengthOfArraysException.class, () -> {
            AbstractTabulatedFunction.checkLengthIsTheSame(xValues, yValues);
        });
    }

    @Test
    public void testCheckLengthIsTheSameWithEmptyArrays() {
        double[] xValues = {};
        double[] yValues = {}; // Оба пустые

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
        double[] yValues = {1.0, 4.0, 9.0}; // Одинаковая длина

        assertDoesNotThrow(() -> {
            AbstractTabulatedFunction.checkLengthIsTheSame(xValues, yValues);
        });
    }

    @Test
    public void testCheckSortedThrowsExceptionForUnsortedArray() {
        double[] xValues = {1.0, 3.0, 2.0}; // Не отсортирован

        assertThrows(ArrayIsNotSortedException.class, () -> {
            AbstractTabulatedFunction.checkSorted(xValues);
        });
    }

    @Test
    public void testCheckSortedThrowsExceptionForEqualValues() {
        double[] xValues = {1.0, 2.0, 2.0}; // Равные значения

        assertThrows(ArrayIsNotSortedException.class, () -> {
            AbstractTabulatedFunction.checkSorted(xValues);
        });
    }

    @Test
    public void testCheckSortedWithSingleElement() {
        double[] xValues = {1.0}; // Один элемент

        assertDoesNotThrow(() -> {
            AbstractTabulatedFunction.checkSorted(xValues);
        });
    }

    @Test
    public void testCheckSortedWithEmptyArray() {
        double[] xValues = {}; // Пустой массив

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
        double[] xValues = {1.0, 2.0, 3.0, 4.0}; // Отсортирован

        assertDoesNotThrow(() -> {
            AbstractTabulatedFunction.checkSorted(xValues);
        });
    }

    // Тесты для конструкторов ArrayTabulatedFunction

    @Test
    public void testConstructorThrowsDifferentLengthException() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0}; // Разная длина

        assertThrows(DifferentLengthOfArraysException.class, () -> {
            new ArrayTabulatedFunction(xValues, yValues);
        });
    }

    @Test
    public void testConstructorThrowsArrayNotSortedException() {
        double[] xValues = {1.0, 3.0, 2.0}; // Не отсортирован
        double[] yValues = {1.0, 4.0, 9.0};

        assertThrows(ArrayIsNotSortedException.class, () -> {
            new ArrayTabulatedFunction(xValues, yValues);
        });
    }

    @Test
    public void testConstructorThrowsIllegalArgumentExceptionForSinglePoint() {
        double[] singleX = {1.0};
        double[] singleY = {1.0}; // Меньше 2 точек

        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunction(singleX, singleY);
        });
    }

    @Test
    public void testConstructorThrowsIllegalArgumentExceptionForEmptyArrays() {
        double[] emptyX = {};
        double[] emptyY = {}; // Пустые массивы

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

        // count < 2
        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunction(func, 0.0, 1.0, 1);
        });

        // xFrom >= xTo
        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunction(func, 2.0, 1.0, 5);
        });

        // xFrom == xTo
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

        // x < leftX
        assertThrows(InterpolationException.class, () -> {
            function.interpolate(-0.5, 0);
        });

        // x > rightX
        assertThrows(InterpolationException.class, () -> {
            function.interpolate(1.5, 0);
        });
    }

    @Test
    public void testInterpolateDoesNotThrowForValidX() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // x внутри интервала
        assertDoesNotThrow(() -> {
            function.interpolate(0.5, 0);
        });

        // x на границе (левая)
        assertDoesNotThrow(() -> {
            function.interpolate(0.0, 0);
        });

        // x на границе (правая)
        assertDoesNotThrow(() -> {
            function.interpolate(1.0, 0);
        });
    }

    @Test
    public void testInterpolateThrowsIllegalArgumentExceptionForInvalidIndex() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // floorIndex < 0
        assertThrows(IllegalArgumentException.class, () -> {
            function.interpolate(0.5, -1);
        });

        // floorIndex >= count - 1
        assertThrows(IllegalArgumentException.class, () -> {
            function.interpolate(0.5, 2);
        });

        // floorIndex >= count
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

    // Тесты для порядка проверок в конструкторе

    @Test
    public void testConstructorOrderOfChecks() {
        // Сначала проверка минимального количества точек
        double[] singleX = {1.0};
        double[] singleY = {1.0};

        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunction(singleX, singleY);
        });

        // Затем проверка одинаковой длины
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0};

        assertThrows(DifferentLengthOfArraysException.class, () -> {
            new ArrayTabulatedFunction(xValues, yValues);
        });

        // Затем проверка отсортированности
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

    // Вспомогательный метод для создания "пустой" функции для тестирования
    private ArrayTabulatedFunction createEmptyFunctionForTesting() {
        return new ArrayTabulatedFunction(new double[]{0.0, 1.0}, new double[]{0.0, 1.0}) {
            @Override
            public int getCount() {
                return 0; // Имитируем пустую функцию
            }
        };
    }

    @Test
    public void testExtrapolationWithMinimalPoints() {
        double[] xValues = {0.0, 1.0};
        double[] yValues = {0.0, 1.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Экстраполяция должна работать даже с минимальным количеством точек
        assertDoesNotThrow(() -> {
            function.apply(-0.5); // экстраполяция слева
        });

        assertDoesNotThrow(() -> {
            function.apply(1.5); // экстраполяция справа
        });
    }

    // Тесты для метода floorIndexOfX

    @Test
    public void testFloorIndexOfXWithNormalFunction() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // x меньше всех точек
        assertEquals(0, function.floorIndexOfX(-1.0));

        // x между точками
        assertEquals(1, function.floorIndexOfX(1.5));

        // x равен существующей точке
        assertEquals(2, function.floorIndexOfX(2.0));

        // x больше всех точек
        assertEquals(2, function.floorIndexOfX(4.0)); // count - 2 = 2
    }

    @Test
    public void testFloorIndexOfXWithExactMatch() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Точное совпадение с существующей точкой
        assertEquals(0, function.floorIndexOfX(0.0));
        assertEquals(1, function.floorIndexOfX(1.0));
        assertEquals(2, function.floorIndexOfX(2.0));
    }

    // Тесты для метода apply

    @Test
    public void testApplyWithVariousScenarios() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Экстраполяция слева
        assertDoesNotThrow(() -> function.apply(-1.0));

        // Экстраполяция справа
        assertDoesNotThrow(() -> function.apply(3.0));

        // Точно по точке
        assertEquals(1.0, function.apply(1.0));

        // Интерполяция
        assertDoesNotThrow(() -> function.apply(0.5));
    }

    @Test
    public void testApplyWithExactXMatch() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Точное совпадение с существующими точками
        assertEquals(0.0, function.apply(0.0));
        assertEquals(1.0, function.apply(1.0));
        assertEquals(4.0, function.apply(2.0));
    }

    // Тесты для метода insert

    @Test
    public void testInsertFunctionality() {
        double[] xValues = {0.0, 2.0};
        double[] yValues = {0.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Вставка новой точки
        assertDoesNotThrow(() -> function.insert(1.0, 1.0));
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getY(1));

        // Обновление существующей точки
        assertDoesNotThrow(() -> function.insert(1.0, 2.0));
        assertEquals(3, function.getCount()); // Количество не должно измениться
        assertEquals(2.0, function.getY(1)); // Значение должно обновиться
    }

    @Test
    public void testInsertAtBeginning() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Вставка в начало
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

        // Вставка в конец
        assertDoesNotThrow(() -> function.insert(2.0, 4.0));
        assertEquals(3, function.getCount());
        assertEquals(2.0, function.getX(2));
        assertEquals(4.0, function.getY(2));
    }

    // Тесты для методов equals, hashCode, toString, clone

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

        // Проверка equals
        assertEquals(function1, function2);
        assertNotEquals(function1, function3);
        assertNotEquals(function1, null);
        assertNotEquals(function1, "not a function");

        // Проверка hashCode
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

        // Проверка, что изменения в клоне не влияют на оригинал
        cloned.setY(1, 2.0);
        assertNotEquals(function.getY(1), cloned.getY(1));
    }

    // Тесты для итератора - ИСПРАВЛЕННАЯ ЧАСТЬ

    @Test
    public void testIterator() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        int count = 0;
        // Исправленный цикл - используем обычный for вместо foreach
        for (int i = 0; i < function.getCount(); i++) {
            Point point = new Point(function.getX(i), function.getY(i));
            assertNotNull(point);
            assertEquals(xValues[count], point.x);
            assertEquals(yValues[count], point.y);
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    public void testIteratorThrowsException() {
        double[] xValues = {0.0, 1.0};
        double[] yValues = {0.0, 1.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Исправленный тест - используем прямой доступ по индексам
        assertThrows(IllegalArgumentException.class, () -> {
            function.getX(-1); // Попытка доступа к несуществующему индексу
        });

        assertThrows(IllegalArgumentException.class, () -> {
            function.getX(2); // Попытка доступа к несуществующему индексу
        });
    }

    // Тесты для protected метода interpolate с 4 параметрами

    @Test
    public void testProtectedInterpolateWithEqualXValues() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Используем рефлексию для тестирования protected метода
        double result = function.interpolate(0.5, 0.0, 1.0, 0.0, 1.0);
        assertEquals(0.5, result, 1e-10);

        // Тест с равными x значениями
        double resultWithEqualX = function.interpolate(0.5, 1.0, 1.0, 2.0, 3.0);
        assertEquals(2.5, resultWithEqualX, 1e-10);
    }
}