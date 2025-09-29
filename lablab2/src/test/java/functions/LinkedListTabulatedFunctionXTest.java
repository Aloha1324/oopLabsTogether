package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для LinkedListTabulatedFunctionX
 * Проверяет корректность работы всех методов класса
 */
class LinkedListTabulatedFunctionXTest {

    // Тест конструктора из массивов значений
    @Test
    void testConstructorFromArrays() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Проверяем корректность создания функции
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(20.0, function.getY(1), 1e-10);
        assertEquals(30.0, function.getY(2), 1e-10);
    }

    // Тест конструктора дискретизации функции
    @Test
    void testConstructorFromFunction() {
        // Создаем тождественную функцию f(x) = x
        MathFunction identity = new MathFunction() {
            @Override
            public double apply(double x) {
                return x;
            }
        };

        // Создаем табулированную функцию на интервале [0, 4] с 5 точками
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(identity, 0.0, 4.0, 5);

        // Проверяем корректность дискретизации
        assertEquals(5, function.getCount());
        assertEquals(0.0, function.leftBound(), 1e-10);
        assertEquals(4.0, function.rightBound(), 1e-10);
        assertEquals(0.0, function.getX(0), 1e-10);
        assertEquals(1.0, function.getX(1), 1e-10);
        assertEquals(2.0, function.getX(2), 1e-10);
        assertEquals(3.0, function.getX(3), 1e-10);
        assertEquals(4.0, function.getX(4), 1e-10);
        assertEquals(2.0, function.getY(2), 1e-10);
    }

    // Тест конструктора для вырожденного интервала (одна точка)
    @Test
    void testConstructorFromFunctionSinglePoint() {
        // Создаем квадратичную функцию f(x) = x²
        MathFunction square = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * x;
            }
        };

        // Создаем функцию с одной точкой x=5.0, но 3 узлами
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(square, 5.0, 5.0, 3);

        // Все узлы должны иметь одинаковые значения
        assertEquals(3, function.getCount());
        assertEquals(5.0, function.getX(0), 1e-10);
        assertEquals(5.0, function.getX(1), 1e-10);
        assertEquals(5.0, function.getX(2), 1e-10);
        assertEquals(25.0, function.getY(0), 1e-10);
        assertEquals(25.0, function.getY(1), 1e-10);
        assertEquals(25.0, function.getY(2), 1e-10);
    }

    // Тест конструктора с обратными границами
    @Test
    void testConstructorWithReversedBounds() {
        MathFunction linear = new MathFunction() {
            @Override
            public double apply(double x) {
                return 2 * x + 1;
            }
        };

        // Границы переданы в обратном порядке
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(linear, 5.0, 1.0, 5);

        // Должны автоматически поменяться местами
        assertEquals(1.0, function.leftBound(), 1e-10);
        assertEquals(5.0, function.rightBound(), 1e-10);
    }

    // Тест установки и получения значений y
    @Test
    void testGetSetY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        function.setY(1, 25.0);
        assertEquals(25.0, function.getY(1), 1e-10);

        // Проверяем, что другие значения не изменились
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(30.0, function.getY(2), 1e-10);
    }

    // Тест поиска индекса по значению x
    @Test
    void testIndexOfX() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        assertEquals(0, function.indexOfX(1.0));
        assertEquals(1, function.indexOfX(2.0));
        assertEquals(2, function.indexOfX(3.0));
        assertEquals(-1, function.indexOfX(5.0)); // Несуществующий x
        assertEquals(-1, function.indexOfX(0.0)); // Несуществующий x
    }

    // Тест поиска индекса по значению y
    @Test
    void testIndexOfY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        assertEquals(0, function.indexOfY(10.0));
        assertEquals(1, function.indexOfY(20.0));
        assertEquals(2, function.indexOfY(30.0));
        assertEquals(-1, function.indexOfY(50.0)); // Несуществующий y
        assertEquals(-1, function.indexOfY(15.0)); // Несуществующий y
    }

    // Тест поиска индекса интервала для заданного x
    @Test
    void testFloorIndexOfX() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        assertEquals(0, function.floorIndexOfX(0.5));  // x меньше всех - индекс 0
        assertEquals(0, function.floorIndexOfX(1.5));  // x между 1 и 2 - индекс 0
        assertEquals(1, function.floorIndexOfX(2.0));  // x равно существующему - индекс 1
        assertEquals(1, function.floorIndexOfX(2.5));  // x между 2 и 3 - индекс 1
        assertEquals(2, function.floorIndexOfX(3.5));  // x между 3 и 4 - индекс 2
        assertEquals(3, function.floorIndexOfX(4.0));  // x равно последнему - индекс 3
        assertEquals(3, function.floorIndexOfX(5.0));  // x больше всех - последний индекс
    }

    // Тест интерполяции внутри интервала
    @Test
    void testApplyInterpolation() {
        double[] xValues = {0.0, 2.0};
        double[] yValues = {0.0, 4.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        // В точке x=1.0 должно быть 2.0 (линейная интерполяция)
        assertEquals(2.0, function.apply(1.0), 1e-10);

        // Проверяем другие точки интерполяции
        assertEquals(1.0, function.apply(0.5), 1e-10);
        assertEquals(3.0, function.apply(1.5), 1e-10);
    }

    // Тест экстраполяции за границами интервала
    @Test
    void testApplyExtrapolation() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 2.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Экстраполяция слева
        assertEquals(0.0, function.apply(0.0), 1e-10);
        assertEquals(-1.0, function.apply(-1.0), 1e-10);

        // Экстраполяция справа
        assertEquals(3.0, function.apply(3.0), 1e-10);
        assertEquals(4.0, function.apply(4.0), 1e-10);
    }

    // Тест точного значения в узле таблицы
    @Test
    void testApplyExactValue() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Для существующих x должно вернуться точное значение
        assertEquals(10.0, function.apply(1.0), 1e-10);
        assertEquals(20.0, function.apply(2.0), 1e-10);
        assertEquals(30.0, function.apply(3.0), 1e-10);
    }

    // Тест функции с одной точкой
    @Test
    void testSinglePointFunction() {
        double[] xValues = {5.0};
        double[] yValues = {10.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Для функции с одной точкой все значения должны быть одинаковыми
        assertEquals(10.0, function.apply(0.0), 1e-10);  // Экстраполяция слева
        assertEquals(10.0, function.apply(5.0), 1e-10);  // Точное значение
        assertEquals(10.0, function.apply(10.0), 1e-10); // Экстраполяция справа
        assertEquals(10.0, function.apply(-5.0), 1e-10); // Экстраполяция слева
    }

    // Тест границ функции
    @Test
    void testBounds() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        assertEquals(1.0, function.leftBound(), 1e-10); // Левая граница
        assertEquals(3.0, function.rightBound(), 1e-10); // Правая граница
    }

    // Тест невалидных параметров конструктора
    @Test
    void testInvalidConstructor() {
        // Тест с разными длинами массивов
        double[] xValues = {1.0};
        double[] yValues = {10.0, 20.0};

        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunctionX(xValues, yValues);
        });

        // Тест с null функцией
        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunctionX(null, 0, 1, 2);
        });

        // Тест с null массивами
        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunctionX(null, new double[]{1.0});
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunctionX(new double[]{1.0}, null);
        });

        // Тест с пустыми массивами
        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunctionX(new double[0], new double[0]);
        });

        // Тест с некорректным количеством точек
        MathFunction func = x -> x;
        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunctionX(func, 0, 1, 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunctionX(func, 0, 1, -1);
        });
    }

    // Тест получения несуществующего индекса
    @Test
    void testInvalidIndexAccess() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        assertThrows(IndexOutOfBoundsException.class, () -> {
            function.getX(-1);
        });

        assertThrows(IndexOutOfBoundsException.class, () -> {
            function.getX(3);
        });

        assertThrows(IndexOutOfBoundsException.class, () -> {
            function.getY(-1);
        });

        assertThrows(IndexOutOfBoundsException.class, () -> {
            function.setY(5, 100.0);
        });
    }

    // Тест граничных случаев floorIndexOfX
    @Test
    void testFloorIndexOfXEdgeCases() {
        // Функция с одним узлом
        double[] singleX = {5.0};
        double[] singleY = {10.0};
        LinkedListTabulatedFunctionX singleFunction = new LinkedListTabulatedFunctionX(singleX, singleY);

        assertEquals(0, singleFunction.floorIndexOfX(0.0)); // Меньше
        assertEquals(0, singleFunction.floorIndexOfX(5.0)); // Равно
        assertEquals(0, singleFunction.floorIndexOfX(10.0)); // Больше

        // Функция с двумя узлами
        double[] twoX = {1.0, 3.0};
        double[] twoY = {10.0, 30.0};
        LinkedListTabulatedFunctionX twoFunction = new LinkedListTabulatedFunctionX(twoX, twoY);

        assertEquals(0, twoFunction.floorIndexOfX(0.0)); // Меньше первого
        assertEquals(0, twoFunction.floorIndexOfX(1.0)); // Равно первому
        assertEquals(0, twoFunction.floorIndexOfX(2.0)); // Между узлами
        assertEquals(1, twoFunction.floorIndexOfX(3.0)); // Равно второму
        assertEquals(1, twoFunction.floorIndexOfX(4.0)); // Больше второго
    }

    // Тест комплексной интерполяции и экстраполяции
    @Test
    void testComplexInterpolationExtrapolation() {
        double[] xValues = {0.0, 2.0, 4.0};
        double[] yValues = {0.0, 4.0, 8.0}; // Линейная функция y = 2x
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Точные значения
        assertEquals(0.0, function.apply(0.0), 1e-10);
        assertEquals(4.0, function.apply(2.0), 1e-10);
        assertEquals(8.0, function.apply(4.0), 1e-10);

        // Интерполяция
        assertEquals(2.0, function.apply(1.0), 1e-10);  // Между 0 и 2: 2*1 = 2.0
        assertEquals(6.0, function.apply(3.0), 1e-10);  // Между 2 и 4: 2*3 = 6.0

        // Экстраполяция
        assertEquals(-2.0, function.apply(-1.0), 1e-10); // Слева: 2*(-1) = -2.0
        assertEquals(10.0, function.apply(5.0), 1e-10);  // Справа: 2*5 = 10.0
    }
}