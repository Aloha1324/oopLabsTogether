package functions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class LinkedListTabulatedFunctionXRemoveTest {

    private LinkedListTabulatedFunctionX function;
    private double[] xValues;
    private double[] yValues;

    @BeforeEach
    void setUp() {
        xValues = new double[]{1.0, 2.0, 3.0, 4.0};
        yValues = new double[]{10.0, 20.0, 30.0, 40.0};
        function = new LinkedListTabulatedFunctionX(xValues, yValues);
    }

    // Тест 1: Проверка валидации индекса - отрицательный индекс
    @Test
    void testRemoveWithNegativeIndex() {
        assertThrows(IllegalArgumentException.class, () -> function.remove(-1));
        assertEquals(4, function.getCount()); // Проверяем, что список не изменился
    }

    // Тест 2: Проверка валидации индекса - индекс равен размеру
    @Test
    void testRemoveWithIndexEqualToCount() {
        assertThrows(IllegalArgumentException.class, () -> function.remove(4));
        assertEquals(4, function.getCount());
    }

    // Тест 3: Проверка валидации индекса - индекс больше размера
    @Test
    void testRemoveWithIndexGreaterThanCount() {
        assertThrows(IllegalArgumentException.class, () -> function.remove(10));
        assertEquals(4, function.getCount());
    }

    // Тест 5: Удаление первого элемента (index == 0, count > 1)
    @Test
    void testRemoveFirstElement() {
        function.remove(0);

        assertEquals(3, function.getCount());
        // Проверяем новые значения
        assertEquals(2.0, function.getX(0), 1e-10);
        assertEquals(3.0, function.getX(1), 1e-10);
        assertEquals(4.0, function.getX(2), 1e-10);
        assertEquals(20.0, function.getY(0), 1e-10);
        assertEquals(30.0, function.getY(1), 1e-10);
        assertEquals(40.0, function.getY(2), 1e-10);

        // Проверяем обновление границ
        assertEquals(2.0, function.leftBound(), 1e-10);
        assertEquals(4.0, function.rightBound(), 1e-10);
    }

    // Тест 6: Удаление последнего элемента (index == count-1, count > 1)
    @Test
    void testRemoveLastElement() {
        function.remove(3);

        assertEquals(3, function.getCount());
        // Проверяем новые значения
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(20.0, function.getY(1), 1e-10);
        assertEquals(30.0, function.getY(2), 1e-10);

        // Проверяем обновление границ
        assertEquals(1.0, function.leftBound(), 1e-10);
        assertEquals(3.0, function.rightBound(), 1e-10);
    }

    // Тест 7: Удаление среднего элемента (index != 0 && index != count-1, count > 1)
    @Test
    void testRemoveMiddleElement() {
        function.remove(1);

        assertEquals(3, function.getCount());
        // Проверяем новые значения
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(3.0, function.getX(1), 1e-10);
        assertEquals(4.0, function.getX(2), 1e-10);
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(30.0, function.getY(1), 1e-10);
        assertEquals(40.0, function.getY(2), 1e-10);

        // Проверяем, что границы не изменились
        assertEquals(1.0, function.leftBound(), 1e-10);
        assertEquals(4.0, function.rightBound(), 1e-10);
    }

    // Тест 8: Удаление второго элемента (index == 1, проверка особого случая)
    @Test
    void testRemoveSecondElement() {
        function.remove(1);

        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(3.0, function.getX(1), 1e-10);
        assertEquals(4.0, function.getX(2), 1e-10);
    }

    // Тест 9: Удаление предпоследнего элемента (index == count-2)
    @Test
    void testRemoveSecondLastElement() {
        function.remove(2);

        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(4.0, function.getX(2), 1e-10);
    }

    // Тест 10: Множественные удаления в разном порядке
    @Test
    void testRemoveMultipleElementsSequentially() {
        // Удаляем в разном порядке
        function.remove(2); // Удаляем 3.0 (средний)
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(4.0, function.getX(2), 1e-10);

        function.remove(0); // Удаляем 1.0 (первый)
        assertEquals(2, function.getCount());
        assertEquals(2.0, function.getX(0), 1e-10);
        assertEquals(4.0, function.getX(1), 1e-10);

        function.remove(1); // Удаляем 4.0 (последний)
        assertEquals(1, function.getCount());
        assertEquals(2.0, function.getX(0), 1e-10);
        assertEquals(20.0, function.getY(0), 1e-10);
    }

    // Тест 11: Проверка сохранения циклической структуры после удаления
    @Test
    void testRemoveMaintainsCircularStructure() {
        // Проверяем исходную структуру через итератор
        int initialCount = 0;
        for (Point point : function) {
            initialCount++;
        }
        assertEquals(4, initialCount);

        // Удаляем элемент
        function.remove(1);

        // Проверяем, что структура сохранилась через итератор
        int finalCount = 0;
        for (Point point : function) {
            finalCount++;
        }
        assertEquals(3, finalCount);

        // Проверяем конкретные значения через итератор
        java.util.Iterator<Point> iterator = function.iterator();
        assertEquals(new Point(1.0, 10.0), iterator.next());
        assertEquals(new Point(3.0, 30.0), iterator.next());
        assertEquals(new Point(4.0, 40.0), iterator.next());
        assertFalse(iterator.hasNext());
    }

    // Тест 12: Проверка работы методов после удаления
    @Test
    void testFunctionalityAfterRemove() {
        // Проверяем исходные значения
        assertEquals(10.0, function.apply(1.0), 1e-10);
        assertEquals(25.0, function.apply(2.5), 1e-10); // Интерполяция между 2 и 3

        // Удаляем точку x=2.0
        function.remove(1);

        // Проверяем новые значения
        assertEquals(3, function.getCount());
        assertEquals(10.0, function.apply(1.0), 1e-10);
        assertEquals(30.0, function.apply(3.0), 1e-10);
        assertEquals(40.0, function.apply(4.0), 1e-10);

        // Проверяем новую интерполяцию
        // Для x=2.5: y = 10 + (30-10)/(3-1) * (2.5-1) = 10 + 20/2 * 1.5 = 10 + 15 = 25
        assertEquals(25.0, function.apply(2.5), 1e-10);
    }

    // Тест 13: Удаление всех элементов последовательно
    @Test
    void testRemoveAllElements() {
        LinkedListTabulatedFunctionX smallFunction = new LinkedListTabulatedFunctionX(
                new double[]{1.0, 2.0}, new double[]{10.0, 20.0});

        // Удаляем первый элемент
        smallFunction.remove(0);
        assertEquals(1, smallFunction.getCount());
        assertEquals(2.0, smallFunction.getX(0), 1e-10);
        assertEquals(20.0, smallFunction.getY(0), 1e-10);

        // Удаляем последний элемент
        smallFunction.remove(0);
        assertEquals(0, smallFunction.getCount());

        // Проверяем, что методы корректно работают с пустым списком
        assertThrows(IllegalArgumentException.class, () -> smallFunction.getX(0));
        assertThrows(IllegalStateException.class, () -> smallFunction.leftBound());
        assertThrows(IllegalStateException.class, () -> smallFunction.rightBound());
        assertThrows(IllegalStateException.class, () -> smallFunction.apply(1.0));
    }

    // Тест 14: Проверка обновления счетчика count
    @Test
    void testRemoveUpdatesCountCorrectly() {
        assertEquals(4, function.getCount());

        function.remove(0);
        assertEquals(3, function.getCount());

        function.remove(0);
        assertEquals(2, function.getCount());

        function.remove(0);
        assertEquals(1, function.getCount());

        function.remove(0);
        assertEquals(0, function.getCount());
    }

    // Тест 15: Проверка целостности данных после удаления
    @Test
    void testDataIntegrityAfterRemove() {
        // Создаем функцию с уникальными значениями
        double[] uniqueX = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] uniqueY = {10.0, 20.0, 30.0, 40.0, 50.0};
        LinkedListTabulatedFunctionX uniqueFunction = new LinkedListTabulatedFunctionX(uniqueX, uniqueY);

        // Удаляем элемент и проверяем целостность оставшихся данных
        uniqueFunction.remove(2); // Удаляем 3.0

        // Проверяем, что оставшиеся данные не повреждены
        assertEquals(1.0, uniqueFunction.getX(0), 1e-10);
        assertEquals(2.0, uniqueFunction.getX(1), 1e-10);
        assertEquals(4.0, uniqueFunction.getX(2), 1e-10);
        assertEquals(5.0, uniqueFunction.getX(3), 1e-10);

        assertEquals(10.0, uniqueFunction.getY(0), 1e-10);
        assertEquals(20.0, uniqueFunction.getY(1), 1e-10);
        assertEquals(40.0, uniqueFunction.getY(2), 1e-10);
        assertEquals(50.0, uniqueFunction.getY(3), 1e-10);
    }

    // Тест 16: Проверка поиска после удаления
    @Test
    void testIndexOfAfterRemove() {
        // Проверяем исходные индексы
        assertEquals(1, function.indexOfX(2.0));
        assertEquals(1, function.indexOfY(20.0));

        // Удаляем элемент
        function.remove(1); // Удаляем x=2.0, y=20.0

        // Проверяем, что поиск работает корректно
        assertEquals(-1, function.indexOfX(2.0)); // Удаленный элемент не должен находиться
        assertEquals(-1, function.indexOfY(20.0));
        assertEquals(0, function.indexOfX(1.0)); // Оставшиеся элементы должны находиться
        assertEquals(1, function.indexOfX(3.0));
        assertEquals(2, function.indexOfX(4.0));
    }

    // Тест 17: Удаление с последующей вставкой (косвенная проверка целостности структуры)
    @Test
    void testRemoveAndThenUseFunction() {
        // Используем различные методы функции
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(25.0, function.apply(2.5), 1e-10);

        // Удаляем элемент
        function.remove(1);

        // Продолжаем использовать функцию
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(30.0, function.getY(1), 1e-10);
        assertEquals(25.0, function.apply(2.5), 1e-10); // Интерполяция должна работать

        // Проверяем граничные значения
        assertEquals(1.0, function.leftBound(), 1e-10);
        assertEquals(4.0, function.rightBound(), 1e-10);
    }

    // Тест 18: Особый случай - удаление из списка с двумя элементами
    @Test
    void testRemoveFromTwoElementList() {
        LinkedListTabulatedFunctionX twoElementList = new LinkedListTabulatedFunctionX(
                new double[]{1.0, 2.0}, new double[]{10.0, 20.0});

        // Удаляем первый элемент
        twoElementList.remove(0);
        assertEquals(1, twoElementList.getCount());
        assertEquals(2.0, twoElementList.getX(0), 1e-10);
        assertEquals(20.0, twoElementList.getY(0), 1e-10);

        // Создаем новый список и удаляем последний элемент
        LinkedListTabulatedFunctionX twoElementList2 = new LinkedListTabulatedFunctionX(
                new double[]{1.0, 2.0}, new double[]{10.0, 20.0});

        twoElementList2.remove(1);
        assertEquals(1, twoElementList2.getCount());
        assertEquals(1.0, twoElementList2.getX(0), 1e-10);
        assertEquals(10.0, twoElementList2.getY(0), 1e-10);
    }

    // Тест 19: Проверка экстраполяции после удаления граничных элементов
    @Test
    void testExtrapolationAfterRemoveBoundaryElements() {
        // Удаляем левую границу
        function.remove(0);
        double leftExtrapolation = function.apply(0.0); // Экстраполяция слева
        // Для точек (2,20) и (3,30): y = 10x
        assertEquals(0.0, leftExtrapolation, 1e-10);

        // Удаляем правую границу
        function.remove(2); // Теперь остались только (2,20) и (3,30)
        double rightExtrapolation = function.apply(5.0); // Экстраполяция справа
        // Для точек (2,20) и (3,30): y = 10x
        assertEquals(50.0, rightExtrapolation, 1e-10);
    }
}