package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArrayTabulatedFunctionInsertTest {

    @Test
    void testInsertAtBeginning() {
        double[] xValues = {2.0, 3.0, 4.0};
        double[] yValues = {20.0, 30.0, 40.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Вставляем в начало
        function.insert(1.0, 10.0);

        assertEquals(4, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(4.0, function.getX(3), 1e-10);
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(20.0, function.getY(1), 1e-10);
    }

    @Test
    void testInsertAtEnd() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Вставляем в конец
        function.insert(4.0, 40.0);

        assertEquals(4, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(4.0, function.getX(3), 1e-10);
        assertEquals(40.0, function.getY(3), 1e-10);
    }

    @Test
    void testInsertInMiddle() {
        double[] xValues = {1.0, 3.0, 5.0};
        double[] yValues = {10.0, 30.0, 50.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Вставляем в середину
        function.insert(2.0, 20.0);
        function.insert(4.0, 40.0);

        assertEquals(5, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(4.0, function.getX(3), 1e-10);
        assertEquals(5.0, function.getX(4), 1e-10);
        assertEquals(20.0, function.getY(1), 1e-10);
        assertEquals(40.0, function.getY(3), 1e-10);
    }

    @Test
    void testInsertExistingX() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Вставляем существующий x - должно заменить y
        function.insert(2.0, 25.0);

        assertEquals(3, function.getCount()); // Количество не должно измениться
        assertEquals(25.0, function.getY(1), 1e-10); // y должен обновиться
        assertEquals(10.0, function.getY(0), 1e-10); // другие y не должны измениться
        assertEquals(30.0, function.getY(2), 1e-10);
    }

    @Test
    void testInsertMaintainsSorting() {
        double[] xValues = {1.0, 5.0};
        double[] yValues = {10.0, 50.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Вставляем точки в разном порядке
        function.insert(3.0, 30.0);
        function.insert(2.0, 20.0);
        function.insert(4.0, 40.0);

        // Проверяем, что массив остался отсортированным
        assertEquals(5, function.getCount());
        for (int i = 0; i < function.getCount() - 1; i++) {
            assertTrue(function.getX(i) < function.getX(i + 1),
                    "Array should remain sorted after insertions");
        }
    }

    @Test
    void testInsertAndApply() {
        double[] xValues = {1.0, 3.0};
        double[] yValues = {1.0, 3.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Вставляем точку в середину
        function.insert(2.0, 2.0);

        // Проверяем, что интерполяция работает корректно
        assertEquals(1.0, function.apply(1.0), 1e-10);
        assertEquals(2.0, function.apply(2.0), 1e-10);
        assertEquals(3.0, function.apply(3.0), 1e-10);
        assertEquals(1.5, function.apply(1.5), 1e-10);
        assertEquals(2.5, function.apply(2.5), 1e-10);
    }

    @Test
    void testInsertBoundaryValues() {
        double[] xValues = {2.0, 3.0};
        double[] yValues = {20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Вставляем на границы
        function.insert(1.0, 10.0); // левая граница
        function.insert(4.0, 40.0); // правая граница

        assertEquals(1.0, function.leftBound(), 1e-10);
        assertEquals(4.0, function.rightBound(), 1e-10);
        assertEquals(4, function.getCount());
    }

    @Test
    void testInsertSameAsExistingWithTolerance() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Вставляем значение, очень близкое к существующему (в пределах погрешности)
        function.insert(2.0 + 1e-13, 25.0);

        // Должно заменить существующее значение, а не добавить новое
        assertEquals(3, function.getCount());
        assertEquals(25.0, function.getY(1), 1e-10);
    }

    @Test
    void testMultipleInsertions() {
        double[] xValues = {0.0, 10.0};
        double[] yValues = {0.0, 100.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Вставляем много точек
        for (int i = 1; i <= 9; i++) {
            function.insert(i, i * 10.0);
        }

        assertEquals(11, function.getCount());

        // Проверяем, что все точки на месте и в правильном порядке
        for (int i = 0; i <= 10; i++) {
            assertEquals(i, function.getX(i), 1e-10);
            assertEquals(i * 10.0, function.getY(i), 1e-10);
        }
    }

    @Test
    void testInsertPreservesFunctionality() {
        double[] xValues = {1.0, 3.0};
        double[] yValues = {2.0, 6.0}; // Линейная функция y = 2x
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Проверяем исходную функциональность
        assertEquals(2.0, function.apply(1.0), 1e-10);
        assertEquals(4.0, function.apply(2.0), 1e-10); // Интерполяция: (2+6)/2 = 4
        assertEquals(6.0, function.apply(3.0), 1e-10);

        // Вставляем точку x=2.0, y=4.0 (точно по линии)
        function.insert(2.0, 4.0);

        // Проверяем, что функциональность сохранилась
        assertEquals(2.0, function.apply(1.0), 1e-10);
        assertEquals(4.0, function.apply(2.0), 1e-10);
        assertEquals(6.0, function.apply(3.0), 1e-10);

        // Проверяем интерполяцию между точками
        assertEquals(3.0, function.apply(1.5), 1e-10); // Между 1.0 и 2.0: (2+4)/2 = 3
        assertEquals(5.0, function.apply(2.5), 1e-10); // Между 2.0 и 3.0: (4+6)/2 = 5
    }
}