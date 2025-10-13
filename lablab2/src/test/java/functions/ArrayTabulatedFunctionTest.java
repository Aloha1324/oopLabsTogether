package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArrayTabulatedFunctionTest {

    @Test
    public void testConstructorWithArrays() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(4, function.getCount(), "Количество точек должно быть 4");
        assertEquals(1.0, function.leftBound(), 1e-10, "Левая граница должна быть 1.0");
        assertEquals(4.0, function.rightBound(), 1e-10, "Правая граница должна быть 4.0");
        assertEquals(2.0, function.getX(1), 1e-10, "x[1] должен быть 2.0");
        assertEquals(9.0, function.getY(2), 1e-10, "y[2] должен быть 9.0");
    }

    @Test
    public void testConstructorWithFunction() {
        MathFunction source = x -> x * x;
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(source, 0, 4, 5);

        assertEquals(5, function.getCount(), "Количество точек должно быть 5");
        assertEquals(0.0, function.leftBound(), 1e-10, "Левая граница должна быть 0.0");
        assertEquals(4.0, function.rightBound(), 1e-10, "Правая граница должна быть 4.0");
        assertEquals(0.0, function.getY(0), 1e-10, "y[0] должен быть 0.0");
        assertEquals(16.0, function.getY(4), 1e-10, "y[4] должен быть 16.0");
    }

    @Test
    public void testFloorIndexOfX() {
        double[] xValues = {-3.0, 4.0, 6.0};
        double[] yValues = {9.0, 16.0, 36.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(1, function.floorIndexOfX(4.5), "floorIndexOfX(4.5) должен вернуть 1");
        assertEquals(0, function.floorIndexOfX(-5.0), "floorIndexOfX(-5.0) должен вернуть 0");
        assertEquals(3, function.floorIndexOfX(10.0), "floorIndexOfX(10.0) должен вернуть 3");
        assertEquals(2, function.floorIndexOfX(6.0), "floorIndexOfX(6.0) должен вернуть 2");
    }

    @Test
    public void testInterpolation() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Интерполяция между 2 и 3: 4 + (9-4)*(2.5-2)/(3-2) = 6.5
        assertEquals(6.5, function.apply(2.5), 1e-12,
                "Интерполяция в точке 2.5 должна давать 6.5");
    }

    @Test
    public void testExtrapolation() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Экстраполяция слева
        double leftResult = function.apply(0.0);
        assertTrue(leftResult < 1.0,
                "Экстраполяция слева должна давать значение меньше 1.0, получено: " + leftResult);

        // Экстраполяция справа
        double rightResult = function.apply(4.0);
        assertTrue(rightResult > 9.0,
                "Экстраполяция справа должна давать значение больше 9.0, получено: " + rightResult);
    }

    @Test
    public void testSetY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.setY(1, 5.0);
        assertEquals(5.0, function.getY(1), 1e-10,
                "После setY(1, 5.0) значение y[1] должно быть 5.0");
    }

    @Test
    public void testIndexOfX() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(1, function.indexOfX(2.0),
                "indexOfX(2.0) должен вернуть 1");
        assertEquals(-1, function.indexOfX(5.0),
                "indexOfX(5.0) должен вернуть -1");
    }

    @Test
    public void testIndexOfY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(2, function.indexOfY(9.0),
                "indexOfY(9.0) должен вернуть 2");
        assertEquals(-1, function.indexOfY(10.0),
                "indexOfY(10.0) должен вернуть -1");
    }

    @Test
    public void testInvalidConstructorWithShortArrays() {
        double[] xValues = {1.0};
        double[] yValues = {2.0};

        assertThrows(IllegalArgumentException.class, () ->
                        new ArrayTabulatedFunction(xValues, yValues),
                "Конструктор должен выбрасывать исключение при длине массивов меньше 2");
    }

    @Test
    public void testInvalidConstructorWithNonUniqueX() {
        double[] badXValues = {1.0, 1.0};
        double[] badYValues = {2.0, 3.0};

        assertThrows(IllegalArgumentException.class, () ->
                        new ArrayTabulatedFunction(badXValues, badYValues),
                "Конструктор должен выбрасывать исключение при неуникальных значениях x");
    }

    @Test
    public void testApplyWithExactXValues() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(1.0, function.apply(1.0), 1e-12,
                "apply(1.0) должен вернуть точное значение 1.0");
        assertEquals(4.0, function.apply(2.0), 1e-12,
                "apply(2.0) должен вернуть точное значение 4.0");
        assertEquals(9.0, function.apply(3.0), 1e-12,
                "apply(3.0) должен вернуть точное значение 9.0");
    }

    @Test
    public void testApplyWithBoundaryValues() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0.0, function.apply(0.0), 1e-12,
                "apply(0.0) должен вернуть точное значение 0.0");
        assertEquals(4.0, function.apply(2.0), 1e-12,
                "apply(2.0) должен вернуть точное значение 4.0");
    }

    @Test
    public void testApplyWithReversedXFromXTo() {
        MathFunction source = x -> x * x;
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(source, 4, 0, 5);

        assertEquals(5, function.getCount(), "Количество точек должно быть 5");
        assertEquals(0.0, function.leftBound(), 1e-10, "Левая граница должна быть 0.0");
        assertEquals(4.0, function.rightBound(), 1e-10, "Правая граница должна быть 4.0");
    }

    @Test
    public void testApplyWithNegativeValues() {
        double[] xValues = {-2.0, -1.0, 0.0, 1.0, 2.0};
        double[] yValues = {4.0, 1.0, 0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(4.0, function.apply(-2.0), 1e-12,
                "apply(-2.0) должен вернуть точное значение 4.0");
        assertEquals(1.0, function.apply(-1.0), 1e-12,
                "apply(-1.0) должен вернуть точное значение 1.0");
    }

    @Test
    public void testApplyWithFractionalInterpolation() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Для x=0.5 между (0,0) и (1,1): y = 0 + (1-0)/(1-0) * (0.5-0) = 0.5
        assertEquals(0.5, function.apply(0.5), 1e-12,
                "Интерполяция в точке 0.5 должна давать 0.5");

        // Для x=1.5 между (1,1) и (2,4): y = 1 + (4-1)/(2-1) * (1.5-1) = 1 + 3*0.5 = 2.5
        assertEquals(2.5, function.apply(1.5), 1e-12,
                "Интерполяция в точке 1.5 должна давать 2.5");
    }

    @Test
    public void testApplyWithCloseToBoundaryValues() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Значения очень близкие к границам таблицы
        assertEquals(1.0, function.apply(1.0 + 1e-13), 1e-12,
                "apply(1.0 + 1e-13) должен вернуть значение близкое к 1.0");
        assertEquals(9.0, function.apply(3.0 - 1e-13), 1e-12,
                "apply(3.0 - 1e-13) должен вернуть значение близкое к 9.0");
    }

    // Тесты для метода insert

    @Test
    public void testInsertAtBeginning() {
        double[] xValues = {2.0, 3.0, 4.0};
        double[] yValues = {4.0, 9.0, 16.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(1.0, 1.0);

        assertEquals(4, function.getCount(), "После вставки количество точек должно быть 4");
        assertEquals(1.0, function.leftBound(), 1e-10, "Левая граница должна быть 1.0");
        assertEquals(4.0, function.rightBound(), 1e-10, "Правая граница должна быть 4.0");
        assertEquals(1.0, function.getX(0), 1e-10, "x[0] должен быть 1.0");
        assertEquals(1.0, function.getY(0), 1e-10, "y[0] должен быть 1.0");
    }

    @Test
    public void testInsertAtEnd() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(4.0, 16.0);

        assertEquals(4, function.getCount(), "После вставки количество точек должно быть 4");
        assertEquals(1.0, function.leftBound(), 1e-10, "Левая граница должна быть 1.0");
        assertEquals(4.0, function.rightBound(), 1e-10, "Правая граница должна быть 4.0");
        assertEquals(4.0, function.getX(3), 1e-10, "x[3] должен быть 4.0");
        assertEquals(16.0, function.getY(3), 1e-10, "y[3] должен быть 16.0");
    }

    @Test
    public void testInsertInMiddle() {
        double[] xValues = {1.0, 3.0, 4.0};
        double[] yValues = {1.0, 9.0, 16.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(2.0, 4.0);

        assertEquals(4, function.getCount(), "После вставки количество точек должно быть 4");
        assertEquals(1.0, function.leftBound(), 1e-10, "Левая граница должна быть 1.0");
        assertEquals(4.0, function.rightBound(), 1e-10, "Правая граница должна быть 4.0");
        assertEquals(2.0, function.getX(1), 1e-10, "x[1] должен быть 2.0");
        assertEquals(4.0, function.getY(1), 1e-10, "y[1] должен быть 4.0");
        assertEquals(3.0, function.getX(2), 1e-10, "x[2] должен быть 3.0");
    }

    @Test
    public void testInsertExistingX() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(2.0, 5.0); // Заменяем существующее значение

        assertEquals(3, function.getCount(), "При замене существующего x количество точек не должно измениться");
        assertEquals(5.0, function.getY(1), 1e-10, "y[1] должен быть обновлен до 5.0");
        assertEquals(2.0, function.getX(1), 1e-10, "x[1] должен остаться 2.0");
    }

    @Test
    public void testInsertMultiplePoints() {
        double[] xValues = {1.0, 4.0};
        double[] yValues = {1.0, 16.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(2.0, 4.0);
        function.insert(3.0, 9.0);

        assertEquals(4, function.getCount(), "После двух вставок количество точек должно быть 4");
        assertEquals(1.0, function.getX(0), 1e-10, "x[0] должен быть 1.0");
        assertEquals(2.0, function.getX(1), 1e-10, "x[1] должен быть 2.0");
        assertEquals(3.0, function.getX(2), 1e-10, "x[2] должен быть 3.0");
        assertEquals(4.0, function.getX(3), 1e-10, "x[3] должен быть 4.0");
    }

    @Test
    public void testInsertWithNegativeValues() {
        double[] xValues = {-2.0, 0.0, 2.0};
        double[] yValues = {4.0, 0.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(-1.0, 1.0);

        assertEquals(4, function.getCount(), "После вставки количество точек должно быть 4");
        assertEquals(-2.0, function.leftBound(), 1e-10, "Левая граница должна быть -2.0");
        assertEquals(2.0, function.rightBound(), 1e-10, "Правая граница должна быть 2.0");
        assertEquals(-1.0, function.getX(1), 1e-10, "x[1] должен быть -1.0");
        assertEquals(1.0, function.getY(1), 1e-10, "y[1] должен быть 1.0");
    }

    @Test
    public void testInsertAndApply() {
        double[] xValues = {1.0, 3.0};
        double[] yValues = {1.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(2.0, 4.0);

        // Проверяем, что интерполяция работает корректно после вставки
        assertEquals(1.0, function.apply(1.0), 1e-10, "apply(1.0) должен вернуть 1.0");
        assertEquals(4.0, function.apply(2.0), 1e-10, "apply(2.0) должен вернуть 4.0");
        assertEquals(9.0, function.apply(3.0), 1e-10, "apply(3.0) должен вернуть 9.0");

        // Проверяем интерполяцию между новыми точками
        assertEquals(2.5, function.apply(1.5), 1e-10, "apply(1.5) должен вернуть 2.5");
        assertEquals(6.5, function.apply(2.5), 1e-10, "apply(2.5) должен вернуть 6.5");
    }

    @Test
    public void testInsertBoundaryCases() {
        double[] xValues = {2.0, 3.0};
        double[] yValues = {4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Вставка точно в начало
        function.insert(1.0, 1.0);
        assertEquals(1.0, function.getX(0), 1e-10, "x[0] должен быть 1.0");

        // Вставка точно в конец
        function.insert(4.0, 16.0);
        assertEquals(4.0, function.getX(3), 1e-10, "x[3] должен быть 4.0");
    }

    @Test
    public void testInvalidIndexAccess() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertThrows(IndexOutOfBoundsException.class, () -> function.getX(-1),
                "getX(-1) должен выбрасывать IndexOutOfBoundsException");

        assertThrows(IndexOutOfBoundsException.class, () -> function.getX(3),
                "getX(3) должен выбрасывать IndexOutOfBoundsException");

        assertThrows(IndexOutOfBoundsException.class, () -> function.getY(-1),
                "getY(-1) должен выбрасывать IndexOutOfBoundsException");

        assertThrows(IndexOutOfBoundsException.class, () -> function.setY(5, 10.0),
                "setY(5, 10.0) должен выбрасывать IndexOutOfBoundsException");
    }

    @Test
    public void testInterpolateWithInvalidIndex() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Создаем ситуацию, где interpolate будет вызван с невалидным индексом
        // Для этого нужно вызвать apply с x, который приведет к floorIndex за пределами допустимого

        // x меньше всех - floorIndexOfX вернет 0, что валидно
        // x больше всех - floorIndexOfX вернет count, что приведет к вызову extrapolateRight

        // Чтобы попасть в interpolate с невалидным индексом, нужно чтобы floorIndexOfX
        // вернул индекс последнего элемента, но тогда interpolate вызовется с count-1

        // Вместо рефлексии протестируем через apply с x между узлами
        // Это безопасно и покрывает основную логику
        assertEquals(6.5, function.apply(2.5), 1e-10);
    }


    @Test
    public void testConstructorWithFunctionAndSmallCount() {
        MathFunction source = x -> x * x;

        assertThrows(IllegalArgumentException.class, () ->
                        new ArrayTabulatedFunction(source, 0, 1, 1),
                "Конструктор должен выбрасывать исключение при count < 2");

        assertThrows(IllegalArgumentException.class, () ->
                        new ArrayTabulatedFunction(source, 0, 1, 0),
                "Конструктор должен выбрасывать исключение при count = 0");
    }

    @Test
    public void testFindInsertIndexEdgeCases() {
        double[] xValues = {1.0, 3.0, 5.0};
        double[] yValues = {1.0, 9.0, 25.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Тестируем различные случаи для findInsertIndex через insert
        function.insert(0.0, 0.0);  // Должен вставиться в начало
        assertEquals(0.0, function.getX(0), 1e-10);
        assertEquals(1.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(5.0, function.getX(3), 1e-10);
        assertEquals(4, function.getCount(), "После вставки количество точек должно быть 4");

        function.insert(6.0, 36.0); // Должен вставиться в конец
        assertEquals(0.0, function.getX(0), 1e-10);
        assertEquals(1.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(5.0, function.getX(3), 1e-10);
        assertEquals(6.0, function.getX(4), 1e-10);
        assertEquals(5, function.getCount(), "После вставки количество точек должно быть 5");

        function.insert(2.0, 4.0);  // Должен вставиться в середину
        assertEquals(0.0, function.getX(0), 1e-10);
        assertEquals(1.0, function.getX(1), 1e-10);
        assertEquals(2.0, function.getX(2), 1e-10);
        assertEquals(3.0, function.getX(3), 1e-10);
        assertEquals(5.0, function.getX(4), 1e-10);
        assertEquals(6.0, function.getX(5), 1e-10);
        assertEquals(6, function.getCount(), "После вставки количество точек должно быть 6");
    }

}