package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArrayTabulatedFunctionRemovableTest {

    @Test
    public void testRemoveFromBeginning() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0, 3.0, 4.0},
                new double[]{1.0, 4.0, 9.0, 16.0}
        );

        function.remove(0);

        assertEquals(3, function.getCount(), 1e-10,
                "После удаления первого элемента количество элементов должно быть 3");
        assertEquals(2.0, function.getX(0), 1e-10,
                "X координата первого элемента после удаления должна быть 2.0");
        assertEquals(3.0, function.getX(1), 1e-10,
                "X координата второго элемента после удаления должна быть 3.0");
        assertEquals(4.0, function.getX(2), 1e-10,
                "X координата третьего элемента после удаления должна быть 4.0");
    }

    @Test
    public void testRemoveFromEnd() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0, 3.0, 4.0},
                new double[]{1.0, 4.0, 9.0, 16.0}
        );

        function.remove(3);

        assertEquals(3, function.getCount(), 1e-10,
                "После удаления последнего элемента количество элементов должно быть 3");
        assertEquals(1.0, function.getX(0), 1e-10,
                "Первый элемент должен остаться неизменным после удаления последнего");
        assertEquals(2.0, function.getX(1), 1e-10,
                "Второй элемент должен остаться неизменным после удаления последнего");
        assertEquals(3.0, function.getX(2), 1e-10,
                "Новый последний элемент должен быть бывшим предпоследним");
    }

    @Test
    public void testRemoveFromMiddle() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0, 3.0, 4.0},
                new double[]{1.0, 4.0, 9.0, 16.0}
        );

        function.remove(1);

        assertEquals(3, function.getCount(), 1e-10,
                "После удаления элемента из середины количество элементов должно быть 3");
        assertEquals(1.0, function.getX(0), 1e-10,
                "Первый элемент должен остаться неизменным после удаления из середины");
        assertEquals(3.0, function.getX(1), 1e-10,
                "Второй элемент должен быть бывшим третьим после удаления из середины");
        assertEquals(4.0, function.getX(2), 1e-10,
                "Третий элемент должен быть бывшим четвертым после удаления из середины");
    }

    @Test
    public void testRemoveInvalidIndex() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0, 3.0},
                new double[]{1.0, 4.0, 9.0}
        );

        assertThrows(IllegalArgumentException.class, () -> function.remove(-1),
                "Должно выбрасываться исключение при попытке удаления с отрицательным индексом");

        assertThrows(IllegalArgumentException.class, () -> function.remove(3),
                "Должно выбрасываться исключение при попытке удаления с индексом превышающим размер массива");
    }

    @Test
    public void testRemoveFromMinimumSizeArray() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0},
                new double[]{1.0, 4.0}
        );

        assertThrows(IllegalStateException.class, () -> function.remove(0),
                "Невозможно удалить элемент из массива с минимальным количеством точек (2)");

        assertThrows(IllegalStateException.class, () -> function.remove(1),
                "Невозможно удалить элемент из массива с минимальным количеством точек (2)");
    }

    @Test
    public void testRemoveUpdatesCount() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0, 3.0, 4.0},
                new double[]{1.0, 4.0, 9.0, 16.0}
        );

        assertEquals(4, function.getCount(), 1e-10,
                "Изначально количество элементов должно быть 4");

        function.remove(1);
        assertEquals(3, function.getCount(), 1e-10,
                "После первого удаления количество элементов должно быть 3");

        function.remove(0);
        assertEquals(2, function.getCount(), 1e-10,
                "После второго удаления количество элементов должно быть 2");

        assertThrows(IllegalStateException.class, () -> function.remove(0),
                "Нельзя удалить элемент если после удаления останется меньше 2 точек");
    }

    @Test
    public void testRemoveMaintainsArrayStructure() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0, 3.0, 4.0, 5.0},
                new double[]{1.0, 4.0, 9.0, 16.0, 25.0}
        );

        function.remove(2);

        assertEquals(1.0, function.getX(0), 1e-10,
                "X координата первого элемента должна остаться 1.0 после удаления");
        assertEquals(2.0, function.getX(1), 1e-10,
                "X координата второго элемента должна остаться 2.0 после удаления");
        assertEquals(4.0, function.getX(2), 1e-10,
                "X координата третьего элемента должна быть 4.0 после удаления");
        assertEquals(5.0, function.getX(3), 1e-10,
                "X координата четвертого элемента должна быть 5.0 после удаления");

        assertEquals(1.0, function.getY(0), 1e-10,
                "Y координата первого элемента должна остаться 1.0 после удаления");
        assertEquals(4.0, function.getY(1), 1e-10,
                "Y координата второго элемента должна остаться 4.0 после удаления");
        assertEquals(16.0, function.getY(2), 1e-10,
                "Y координата третьего элемента должна быть 16.0 после удаления");
        assertEquals(25.0, function.getY(3), 1e-10,
                "Y координата четвертого элемента должна быть 25.0 после удаления");
    }

    @Test
    public void testRemoveMultipleElements() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0, 3.0, 4.0, 5.0},
                new double[]{1.0, 4.0, 9.0, 16.0, 25.0}
        );

        function.remove(1);
        assertEquals(4, function.getCount(), 1e-10,
                "После первого удаления количество элементов должно быть 4");
        assertEquals(3.0, function.getX(1), 1e-10,
                "Второй элемент должен стать бывшим третьим после первого удаления");

        function.remove(2);
        assertEquals(3, function.getCount(), 1e-10,
                "После второго удаления количество элементов должно быть 3");
        assertEquals(5.0, function.getX(2), 1e-10,
                "Последний элемент должен быть бывшим пятым после второго удаления");
    }

    @Test
    public void testRemoveBoundaryValues() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{0.0, 1.0, 2.0, 3.0},
                new double[]{10.0, 20.0, 30.0, 40.0}
        );

        function.remove(0);
        assertEquals(1.0, function.leftBound(), 1e-10,
                "Левая граница должна обновиться после удаления первого элемента");

        function.remove(function.getCount() - 1);
        assertEquals(2.0, function.rightBound(), 1e-10,
                "Правая граница должна обновиться после удаления последнего элемента");
    }

    @Test
    public void testRemoveWithMathFunctionConstructor() {
        MathFunction sqr = new SqrFunction();
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                sqr, 0.0, 4.0, 5
        );

        assertEquals(5, function.getCount(), 1e-10,
                "Изначально количество элементов должно быть 5");

        function.remove(2);
        assertEquals(4, function.getCount(), 1e-10,
                "После удаления количество элементов должно быть 4");

        // Исправленная строка - убрана delta из assertTrue
        assertTrue(function.getCount() >= 2,
                "После удаления должно остаться минимум 2 точки");
    }

    @Test
    public void testRemovePreservesDataConsistency() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0, 3.0, 4.0},
                new double[]{10.0, 20.0, 30.0, 40.0}
        );

        function.remove(1);

        // Проверяем соответствие X и Y значений после удаления
        assertEquals(10.0, function.getY(0), 1e-10,
                "Y значение первого элемента должно соответствовать X значению после удаления");
        assertEquals(30.0, function.getY(1), 1e-10,
                "Y значение второго элемента должно соответствовать X значению после удаления");
        assertEquals(40.0, function.getY(2), 1e-10,
                "Y значение третьего элемента должно соответствовать X значению после удаления");
    }
}