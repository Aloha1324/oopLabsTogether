package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LinkedListTabulatedFunctionXRemoveTest {

    @Test
    void testRemoveFirstElement() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Удаляем первый элемент
        function.remove(0);

        assertEquals(3, function.getCount());
        assertEquals(2.0, function.getX(0), 1e-10);
        assertEquals(3.0, function.getX(1), 1e-10);
        assertEquals(4.0, function.getX(2), 1e-10);
        assertEquals(20.0, function.getY(0), 1e-10);
        assertEquals(30.0, function.getY(1), 1e-10);
        assertEquals(40.0, function.getY(2), 1e-10);
    }

    @Test
    void testRemoveLastElement() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Удаляем последний элемент
        function.remove(3);

        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(20.0, function.getY(1), 1e-10);
        assertEquals(30.0, function.getY(2), 1e-10);
    }

    @Test
    void testRemoveMiddleElement() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Удаляем элемент в середине
        function.remove(1);

        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(3.0, function.getX(1), 1e-10);
        assertEquals(4.0, function.getX(2), 1e-10);
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(30.0, function.getY(1), 1e-10);
        assertEquals(40.0, function.getY(2), 1e-10);
    }

    @Test
    void testRemoveSingleElement() {
        double[] xValues = {5.0};
        double[] yValues = {50.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Удаляем единственный элемент
        function.remove(0);

        assertEquals(0, function.getCount());
        assertThrows(IllegalStateException.class, () -> function.leftBound());
        assertThrows(IllegalStateException.class, () -> function.rightBound());
    }

    @Test
    void testRemoveMultipleElements() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0, 50.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Удаляем несколько элементов в разном порядке
        function.remove(2); // Удаляем 3.0
        function.remove(0); // Удаляем 1.0
        function.remove(1); // Удаляем 4.0 (после предыдущих удалений)

        assertEquals(2, function.getCount());
        assertEquals(2.0, function.getX(0), 1e-10);
        assertEquals(5.0, function.getX(1), 1e-10);
        assertEquals(20.0, function.getY(0), 1e-10);
        assertEquals(50.0, function.getY(1), 1e-10);
    }

    @Test
    void testRemoveInvalidIndex() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Пытаемся удалить с невалидными индексами
        assertThrows(IllegalArgumentException.class, () -> function.remove(-1));
        assertThrows(IllegalArgumentException.class, () -> function.remove(3));
        assertThrows(IllegalArgumentException.class, () -> function.remove(10));

        // Проверяем, что список не изменился
        assertEquals(3, function.getCount());
    }

    @Test
    void testRemoveAndFunctionalityPreserved() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0}; // Примерно x²
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Проверяем исходную функциональность
        assertEquals(1.0, function.apply(1.0), 1e-10);
        assertEquals(6.5, function.apply(2.5), 1e-10); // Интерполяция между 2 и 3

        // Удаляем точку x=3.0
        function.remove(2);

        // Проверяем, что функциональность сохранилась
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.apply(1.0), 1e-10);
        assertEquals(4.0, function.apply(2.0), 1e-10);
        assertEquals(16.0, function.apply(4.0), 1e-10);

        // Проверяем новую интерполяцию между 2 и 4
        // Для x=3.0: y = 4 + (16-4)/(4-2) * (3-2) = 4 + 12/2 * 1 = 4 + 6 = 10
        assertEquals(10.0, function.apply(3.0), 1e-10);
    }

    @Test
    void testRemoveUpdatesBounds() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        assertEquals(1.0, function.leftBound(), 1e-10);
        assertEquals(4.0, function.rightBound(), 1e-10);

        // Удаляем левую границу
        function.remove(0);
        assertEquals(2.0, function.leftBound(), 1e-10);
        assertEquals(4.0, function.rightBound(), 1e-10);

        // Удаляем правую границу
        function.remove(2);
        assertEquals(2.0, function.leftBound(), 1e-10);
        assertEquals(3.0, function.rightBound(), 1e-10);
    }

    @Test
    void testRemoveAllElements() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {10.0, 20.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        function.remove(0);
        function.remove(0);

        assertEquals(0, function.getCount());

        // Проверяем, что основные методы корректно работают с пустым списком
        assertThrows(IndexOutOfBoundsException.class, () -> function.getX(0));
        assertThrows(IllegalStateException.class, () -> function.apply(1.0));
    }

    @Test
    void testRemoveMaintainsCircularStructure() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunctionX function = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Удаляем средний элемент
        function.remove(1);

        // Проверяем, что циклическая структура сохранилась через публичные методы
        assertEquals(2, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(3.0, function.getX(1), 1e-10);

        // Проверяем границы
        assertEquals(1.0, function.leftBound(), 1e-10);
        assertEquals(3.0, function.rightBound(), 1e-10);

        // Проверяем, что apply работает корректно
        assertEquals(10.0, function.apply(1.0), 1e-10);
        assertEquals(30.0, function.apply(3.0), 1e-10);
    }
}