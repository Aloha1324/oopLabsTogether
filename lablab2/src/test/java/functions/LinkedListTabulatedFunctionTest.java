package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LinkedListTabulatedFunctionTest {

    @Test
    public void testInsertIntoEmptyList() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction();
        function.insert(2.0, 5.0);

        assertEquals(1, function.getCount(),
                "После вставки в пустой список количество элементов должно быть 1");
        assertEquals(2.0, function.getX(0), 1e-10,
                "X-координата вставленного элемента не соответствует ожидаемой");
        assertEquals(5.0, function.getY(0), 1e-10,
                "Y-координата вставленного элемента не соответствует ожидаемой");
    }

    @Test
    public void testInsertAtBeginning() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{3.0, 4.0}, new double[]{9.0, 16.0}
        );

        function.insert(1.0, 1.0);

        assertEquals(3, function.getCount(),
                "После вставки в начало количество элементов должно увеличиться на 1");
        assertEquals(1.0, function.getX(0), 1e-10,
                "Новый элемент должен стать головой списка");
        assertEquals(3.0, function.getX(1), 1e-10,
                "Прежний первый элемент должен сместиться на вторую позицию");
        assertEquals(4.0, function.getX(2), 1e-10,
                "Последний элемент должен остаться на своей позиции");
    }

    @Test
    public void testInsertAtEnd() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0}
        );

        function.insert(3.0, 9.0);

        assertEquals(3, function.getCount(),
                "После вставки в конец количество элементов должно увеличиться на 1");
        assertEquals(3.0, function.getX(2), 1e-10,
                "Новый элемент должен стать хвостом списка");
        assertEquals(9.0, function.getY(2), 1e-10,
                "Y-координата нового элемента в хвосте не соответствует ожидаемой");
    }

    @Test
    public void testInsertInMiddle() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1.0, 3.0, 4.0}, new double[]{1.0, 9.0, 16.0}
        );

        function.insert(2.0, 4.0);

        assertEquals(4, function.getCount(),
                "После вставки в середину количество элементов должно увеличиться на 1");
        assertEquals(2.0, function.getX(1), 1e-10,
                "Новый элемент должен быть вставлен на правильную позицию в середине");
        assertEquals(4.0, function.getY(1), 1e-10,
                "Y-координата элемента в середине не соответствует ожидаемой");
    }

    @Test
    public void testInsertDuplicateX() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0}
        );

        function.insert(2.0, 20.0);

        assertEquals(3, function.getCount(),
                "При вставке с существующим X количество элементов не должно изменяться");
        assertEquals(20.0, function.getY(1), 1e-10,
                "Y-координата должна обновиться при вставке с существующим X");
    }

    @Test
    public void testInsertUpdatesCount() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction();

        assertEquals(0, function.getCount(),
                "Изначально количество элементов должно быть 0");

        function.insert(1.0, 1.0);
        assertEquals(1, function.getCount(),
                "После первой вставки количество элементов должно быть 1");

        function.insert(2.0, 4.0);
        assertEquals(2, function.getCount(),
                "После второй вставки количество элементов должно быть 2");

        function.insert(1.0, 10.0);
        assertEquals(2, function.getCount(),
                "При вставке с существующим X количество элементов не должно изменяться");
    }

    @Test
    public void testInsertMaintainsOrdering() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction();

        function.insert(5.0, 25.0);
        function.insert(1.0, 1.0);
        function.insert(3.0, 9.0);
        function.insert(2.0, 4.0);
        function.insert(4.0, 16.0);

        assertEquals(5, function.getCount(),
                "После всех вставок должно быть 5 элементов");

        // Проверка упорядоченности по X
        assertEquals(1.0, function.getX(0), 1e-10,
                "Первый элемент должен иметь наименьший X");
        assertEquals(2.0, function.getX(1), 1e-10,
                "Второй элемент должен быть упорядочен по X");
        assertEquals(3.0, function.getX(2), 1e-10,
                "Третий элемент должен быть упорядочен по X");
        assertEquals(4.0, function.getX(3), 1e-10,
                "Четвертый элемент должен быть упорядочен по X");
        assertEquals(5.0, function.getX(4), 1e-10,
                "Пятый элемент должен иметь наибольший X");
    }

    @Test
    public void testInsertWithNegativeValues() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction();

        function.insert(-2.0, 4.0);
        function.insert(-1.0, 1.0);
        function.insert(-3.0, 9.0);

        assertEquals(3, function.getCount(),
                "После вставки отрицательных значений должно быть 3 элемента");
        assertEquals(-3.0, function.getX(0), 1e-10,
                "Элементы с отрицательными X должны быть упорядочены правильно");
        assertEquals(-2.0, function.getX(1), 1e-10,
                "Элементы с отрицательными X должны быть упорядочены правильно");
        assertEquals(-1.0, function.getX(2), 1e-10,
                "Элементы с отрицательными X должны быть упорядочены правильно");
    }

    @Test
    public void testInsertWithSameXMultipleTimes() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction();

        function.insert(1.0, 10.0);
        function.insert(1.0, 20.0);
        function.insert(1.0, 30.0);

        assertEquals(1, function.getCount(),
                "Многократная вставка с одинаковым X не должна увеличивать количество элементов");
        assertEquals(30.0, function.getY(0), 1e-10,
                "Должно сохраняться последнее вставленное значение Y");
    }

    @Test
    public void testInsertPreservesListStructure() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1.0, 3.0}, new double[]{1.0, 9.0}
        );

        function.insert(2.0, 4.0);

        // Проверка целостности структуры списка
        assertEquals(1.0, function.getX(0), 1e-10,
                "Первый элемент должен сохранить свое значение X");
        assertEquals(2.0, function.getX(1), 1e-10,
                "Второй элемент должен быть новым вставленным элементом");
        assertEquals(3.0, function.getX(2), 1e-10,
                "Третий элемент должен сохранить свое значение X");

        assertEquals(1.0, function.getY(0), 1e-10,
                "Первый элемент должен сохранить свое значение Y");
        assertEquals(4.0, function.getY(1), 1e-10,
                "Второй элемент должен иметь новое значение Y");
        assertEquals(9.0, function.getY(2), 1e-10,
                "Третий элемент должен сохранить свое значение Y");
    }
}