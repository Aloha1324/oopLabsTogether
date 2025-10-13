package functions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.NoSuchElementException;

class LinkedListTabulatedFunctionXTest {

    private LinkedListTabulatedFunctionX function;
    private double[] xValues;
    private double[] yValues;

    @BeforeEach
    void setUp() {
        xValues = new double[]{1.0, 2.0, 3.0, 4.0};
        yValues = new double[]{1.0, 4.0, 9.0, 16.0};
        function = new LinkedListTabulatedFunctionX(xValues, yValues);
    }

    // Тесты конструкторов
    @Test
    void testConstructorWithArrays() {
        assertNotNull(function);
        assertEquals(4, function.getCount());
        assertEquals(1.0, function.leftBound());
        assertEquals(4.0, function.rightBound());
    }

    @Test
    void testConstructorWithArraysNullX() {
        assertThrows(IllegalArgumentException.class, () ->
                new LinkedListTabulatedFunctionX(null, yValues));
    }

    @Test
    void testConstructorWithArraysNullY() {
        assertThrows(IllegalArgumentException.class, () ->
                new LinkedListTabulatedFunctionX(xValues, null));
    }

    @Test
    void testConstructorWithArraysDifferentLengths() {
        double[] shortY = new double[]{1.0, 4.0};
        assertThrows(IllegalArgumentException.class, () ->
                new LinkedListTabulatedFunctionX(xValues, shortY));
    }

    @Test
    void testConstructorWithArraysLessThan2Points() {
        double[] singleX = new double[]{1.0};
        double[] singleY = new double[]{2.0};
        assertThrows(IllegalArgumentException.class, () ->
                new LinkedListTabulatedFunctionX(singleX, singleY));
    }

    @Test
    void testConstructorWithArraysEmpty() {
        double[] emptyX = new double[]{};
        double[] emptyY = new double[]{};
        assertThrows(IllegalArgumentException.class, () ->
                new LinkedListTabulatedFunctionX(emptyX, emptyY));
    }

    @Test
    void testConstructorWithFunction() {
        MathFunction source = x -> x * x;
        LinkedListTabulatedFunctionX func = new LinkedListTabulatedFunctionX(source, 0, 4, 5);

        assertEquals(5, func.getCount());
        assertEquals(0.0, func.leftBound());
        assertEquals(4.0, func.rightBound());
    }

    @Test
    void testConstructorWithFunctionNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new LinkedListTabulatedFunctionX(null, 0, 4, 5));
    }

    @Test
    void testConstructorWithFunctionLessThan2Points() {
        MathFunction source = x -> x * x;
        assertThrows(IllegalArgumentException.class, () ->
                new LinkedListTabulatedFunctionX(source, 0, 4, 1));
    }

    @Test
    void testConstructorWithFunctionZeroPoints() {
        MathFunction source = x -> x * x;
        assertThrows(IllegalArgumentException.class, () ->
                new LinkedListTabulatedFunctionX(source, 0, 4, 0));
    }

    @Test
    void testConstructorWithFunctionNegativePoints() {
        MathFunction source = x -> x * x;
        assertThrows(IllegalArgumentException.class, () ->
                new LinkedListTabulatedFunctionX(source, 0, 4, -1));
    }

    @Test
    void testConstructorWithFunctionReversedBounds() {
        MathFunction source = x -> x * x;
        LinkedListTabulatedFunctionX func = new LinkedListTabulatedFunctionX(source, 4, 0, 5);

        assertEquals(5, func.getCount());
        assertEquals(0.0, func.leftBound());
        assertEquals(4.0, func.rightBound());
    }

    @Test
    void testConstructorWithFunctionEqualBounds() {
        MathFunction source = x -> x * x;
        LinkedListTabulatedFunctionX func = new LinkedListTabulatedFunctionX(source, 2.0, 2.0, 3);

        assertEquals(3, func.getCount());
        assertEquals(2.0, func.getX(0));
        assertEquals(2.0, func.getX(1));
        assertEquals(2.0, func.getX(2));
        assertEquals(4.0, func.getY(0));
    }

    // Тесты методов доступа
    @Test
    void testGetX() {
        assertEquals(1.0, function.getX(0));
        assertEquals(2.0, function.getX(1));
        assertEquals(3.0, function.getX(2));
        assertEquals(4.0, function.getX(3));
    }

    @Test
    void testGetXInvalidIndex() {
        assertThrows(IllegalArgumentException.class, () -> function.getX(-1));
        assertThrows(IllegalArgumentException.class, () -> function.getX(4));
        assertThrows(IllegalArgumentException.class, () -> function.getX(10));
    }

    @Test
    void testGetY() {
        assertEquals(1.0, function.getY(0));
        assertEquals(4.0, function.getY(1));
        assertEquals(9.0, function.getY(2));
        assertEquals(16.0, function.getY(3));
    }

    @Test
    void testGetYInvalidIndex() {
        assertThrows(IllegalArgumentException.class, () -> function.getY(-1));
        assertThrows(IllegalArgumentException.class, () -> function.getY(4));
    }

    @Test
    void testSetY() {
        function.setY(1, 5.0);
        assertEquals(5.0, function.getY(1));

        function.setY(2, 10.0);
        assertEquals(10.0, function.getY(2));
    }

    @Test
    void testSetYInvalidIndex() {
        assertThrows(IllegalArgumentException.class, () -> function.setY(-1, 5.0));
        assertThrows(IllegalArgumentException.class, () -> function.setY(4, 5.0));
    }

    // Тесты границ
    @Test
    void testLeftBound() {
        assertEquals(1.0, function.leftBound());
    }

    @Test
    void testRightBound() {
        assertEquals(4.0, function.rightBound());
    }

    @Test
    void testLeftBoundEmptyList() {
        LinkedListTabulatedFunctionX emptyFunction = new LinkedListTabulatedFunctionX(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        emptyFunction.remove(0);
        emptyFunction.remove(0);
        assertThrows(IllegalStateException.class, () -> emptyFunction.leftBound());
    }

    @Test
    void testRightBoundEmptyList() {
        LinkedListTabulatedFunctionX emptyFunction = new LinkedListTabulatedFunctionX(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        emptyFunction.remove(0);
        emptyFunction.remove(0);
        assertThrows(IllegalStateException.class, () -> emptyFunction.rightBound());
    }

    // Тесты поиска индексов
    @Test
    void testIndexOfX() {
        assertEquals(0, function.indexOfX(1.0));
        assertEquals(1, function.indexOfX(2.0));
        assertEquals(2, function.indexOfX(3.0));
        assertEquals(3, function.indexOfX(4.0));
        assertEquals(-1, function.indexOfX(5.0));
        assertEquals(-1, function.indexOfX(0.5));
    }

    @Test
    void testIndexOfXWithPrecision() {
        // Тест с учетом точности
        assertEquals(1, function.indexOfX(2.0 + 1e-13));
        assertEquals(1, function.indexOfX(2.0 - 1e-13));
    }

    @Test
    void testIndexOfY() {
        assertEquals(0, function.indexOfY(1.0));
        assertEquals(1, function.indexOfY(4.0));
        assertEquals(2, function.indexOfY(9.0));
        assertEquals(3, function.indexOfY(16.0));
        assertEquals(-1, function.indexOfY(5.0));
        assertEquals(-1, function.indexOfY(0.0));
    }

    @Test
    void testIndexOfYWithPrecision() {
        // Тест с учетом точности
        assertEquals(1, function.indexOfY(4.0 + 1e-13));
        assertEquals(1, function.indexOfY(4.0 - 1e-13));
    }

    // Тесты floorIndexOfX
    @Test
    void testFloorIndexOfX() {
        assertEquals(0, function.floorIndexOfX(1.0));  // точно на узле
        assertEquals(0, function.floorIndexOfX(1.5));  // между 1 и 2
        assertEquals(1, function.floorIndexOfX(2.0));  // точно на узле
        assertEquals(1, function.floorIndexOfX(2.5));  // между 2 и 3
        assertEquals(2, function.floorIndexOfX(3.0));  // точно на узле
        assertEquals(2, function.floorIndexOfX(3.5));  // между 3 и 4
        assertEquals(3, function.floorIndexOfX(4.0));  // точно на узле
        assertEquals(3, function.floorIndexOfX(4.5));  // справа от последнего узла
    }

    @Test
    void testFloorIndexOfXLessThanLeftBound() {
        assertThrows(IllegalArgumentException.class, () -> function.floorIndexOfX(0.5));
        assertThrows(IllegalArgumentException.class, () -> function.floorIndexOfX(-1.0));
    }

    @Test
    void testFloorIndexOfXEmptyList() {
        LinkedListTabulatedFunctionX emptyFunction = new LinkedListTabulatedFunctionX(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        emptyFunction.remove(0);
        emptyFunction.remove(0);
        assertThrows(IllegalStateException.class, () -> emptyFunction.floorIndexOfX(1.0));
    }

    // Тесты интерполяции и экстраполяции
    @Test
    void testInterpolate() {
        // Интерполяция между точками
        double result = function.interpolate(2.5, 1);
        assertEquals(6.5, result, 1e-12); // (4 + 9)/2 = 6.5

        result = function.interpolate(1.5, 0);
        assertEquals(2.5, result, 1e-12); // (1 + 4)/2 = 2.5
    }

    @Test
    void testInterpolateHelperMethod() {
        double result = function.interpolate(1.5, 1.0, 2.0, 1.0, 4.0);
        assertEquals(2.5, result, 1e-12);
    }

    @Test
    void testExtrapolateLeft() {
        double result = function.extrapolateLeft(0.0);
        double expected = function.interpolate(0.0, 1.0, 2.0, 1.0, 4.0);
        assertEquals(expected, result, 1e-12);
    }

    @Test
    void testExtrapolateRight() {
        double result = function.extrapolateRight(5.0);
        double expected = function.interpolate(5.0, 3.0, 4.0, 9.0, 16.0);
        assertEquals(expected, result, 1e-12);
    }

    // Тесты метода apply
    @Test
    void testApplyExactX() {
        assertEquals(1.0, function.apply(1.0));
        assertEquals(4.0, function.apply(2.0));
        assertEquals(9.0, function.apply(3.0));
        assertEquals(16.0, function.apply(4.0));
    }

    @Test
    void testApplyInterpolation() {
        double result = function.apply(2.5);
        assertEquals(6.5, result, 1e-12);

        result = function.apply(1.5);
        assertEquals(2.5, result, 1e-12);
    }

    @Test
    void testApplyExtrapolationLeft() {
        double result = function.apply(0.0);
        double expected = function.extrapolateLeft(0.0);
        assertEquals(expected, result, 1e-12);
    }

    @Test
    void testApplyExtrapolationRight() {
        double result = function.apply(5.0);
        double expected = function.extrapolateRight(5.0);
        assertEquals(expected, result, 1e-12);
    }

    // Тесты удаления узлов
    @Test
    void testRemoveFirst() {
        function.remove(0);
        assertEquals(3, function.getCount());
        assertEquals(2.0, function.leftBound());
        assertEquals(4.0, function.rightBound());
        assertEquals(2.0, function.getX(0));
    }

    @Test
    void testRemoveLast() {
        function.remove(3);
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.leftBound());
        assertEquals(3.0, function.rightBound());
        assertEquals(3.0, function.getX(2));
    }

    @Test
    void testRemoveMiddle() {
        function.remove(1);
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0));
        assertEquals(3.0, function.getX(1));
        assertEquals(4.0, function.getX(2));
    }

    @Test
    void testRemoveInvalidIndex() {
        assertThrows(IllegalArgumentException.class, () -> function.remove(-1));
        assertThrows(IllegalArgumentException.class, () -> function.remove(4));
    }

    @Test
    void testRemoveAllNodes() {
        LinkedListTabulatedFunctionX smallFunction = new LinkedListTabulatedFunctionX(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0});

        smallFunction.remove(0);
        assertEquals(1, smallFunction.getCount());

        smallFunction.remove(0);
        assertEquals(0, smallFunction.getCount());

        // После удаления всех узлов методы должны бросать исключения
        assertThrows(IllegalStateException.class, () -> smallFunction.leftBound());
        assertThrows(IllegalStateException.class, () -> smallFunction.rightBound());
    }

    @Test
    void testRemoveSingleNodeList() {
        LinkedListTabulatedFunctionX singleNodeFunction = new LinkedListTabulatedFunctionX(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        singleNodeFunction.remove(0);
        singleNodeFunction.remove(0); // Теперь список пуст

        // Попытка удалить из пустого списка
        assertThrows(IllegalArgumentException.class, () -> singleNodeFunction.remove(0));
    }

    // Тесты для проверки циклической структуры списка через публичные методы
    @Test
    void testCircularStructureThroughPublicMethods() {
        // Проверяем, что после прохождения всех элементов возвращаемся к началу
        // через итератор
        Iterator<Point> iterator = function.iterator();
        Point firstPoint = iterator.next();

        // Проходим все элементы
        while (iterator.hasNext()) {
            iterator.next();
        }

        // Создаем новый итератор и проверяем, что первый элемент тот же
        Iterator<Point> newIterator = function.iterator();
        assertEquals(firstPoint, newIterator.next());
    }

    @Test
    void testCircularStructureAfterRemove() {
        function.remove(1); // Удаляем средний узел

        // Проверяем, что структура остается корректной через публичные методы
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0));
        assertEquals(3.0, function.getX(1));
        assertEquals(4.0, function.getX(2));

        // Проверяем границы
        assertEquals(1.0, function.leftBound());
        assertEquals(4.0, function.rightBound());
    }

    // Тесты для getNode с разными путями выполнения через косвенные методы
    @Test
    void testGetNodeFirstHalf() {
        // Индекс в первой половине - проверяем через getX
        assertEquals(2.0, function.getX(1));
    }

    @Test
    void testGetNodeSecondHalf() {
        // Индекс во второй половине - проверяем через getX
        assertEquals(3.0, function.getX(2));
    }

    // Тесты для addNode через конструктор
    @Test
    void testAddNodeToEmptyList() {
        LinkedListTabulatedFunctionX emptyFunc = new LinkedListTabulatedFunctionX(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        // Удаляем все узлы чтобы получить пустой список
        emptyFunc.remove(0);
        emptyFunc.remove(0);

        // Тестируем приватный метод addNode через конструктор
        LinkedListTabulatedFunctionX newFunc = new LinkedListTabulatedFunctionX(
                new double[]{5.0, 6.0}, new double[]{25.0, 36.0});
        assertEquals(2, newFunc.getCount());
    }

    // Тесты граничных случаев с двумя точками
    @Test
    void testTwoPointFunction() {
        LinkedListTabulatedFunctionX twoPointFunc = new LinkedListTabulatedFunctionX(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0});

        assertEquals(2, twoPointFunc.getCount());
        assertEquals(1.0, twoPointFunc.leftBound());
        assertEquals(2.0, twoPointFunc.rightBound());

        // Проверяем экстраполяцию для функции с двумя точками
        double leftExtrapolation = twoPointFunc.extrapolateLeft(0.0);
        double rightExtrapolation = twoPointFunc.extrapolateRight(3.0);

        assertEquals(-2.0, leftExtrapolation, 1e-12); // y = 3x - 2
        assertEquals(7.0, rightExtrapolation, 1e-12);  // y = 3x - 2
    }

    // Тесты для проверки работы после множественных операций
    @Test
    void testMultipleOperations() {
        // Создаем функцию
        LinkedListTabulatedFunctionX func = new LinkedListTabulatedFunctionX(
                new double[]{0.0, 1.0, 2.0}, new double[]{0.0, 1.0, 4.0});

        // Проверяем начальное состояние
        assertEquals(3, func.getCount());

        // Изменяем значение
        func.setY(1, 2.0);
        assertEquals(2.0, func.getY(1));

        // Удаляем узел
        func.remove(1);
        assertEquals(2, func.getCount());
        assertEquals(0.0, func.getX(0));
        assertEquals(2.0, func.getX(1));

        // Проверяем интерполяцию
        double result = func.apply(1.0);
        assertEquals(2.0, result, 1e-12); // линейная интерполяция между (0,0) и (2,4)
    }

    // Тесты для проверки точности вычислений
    @Test
    void testPrecision() {
        double[] preciseX = {1.0, 1.000000000001, 2.0};
        double[] preciseY = {1.0, 2.0, 3.0};
        LinkedListTabulatedFunctionX preciseFunc = new LinkedListTabulatedFunctionX(preciseX, preciseY);

        // Должен найти точное совпадение с учетом погрешности
        assertEquals(1, preciseFunc.indexOfX(1.000000000001));

        // Не должен найти из-за большой разницы
        assertEquals(-1, preciseFunc.indexOfX(1.0001));
    }

    // Тесты итератора
    @Test
    void testIterator() {
        Iterator<Point> iterator = function.iterator();

        assertTrue(iterator.hasNext());
        assertEquals(new Point(1.0, 1.0), iterator.next());

        assertTrue(iterator.hasNext());
        assertEquals(new Point(2.0, 4.0), iterator.next());

        assertTrue(iterator.hasNext());
        assertEquals(new Point(3.0, 9.0), iterator.next());

        assertTrue(iterator.hasNext());
        assertEquals(new Point(4.0, 16.0), iterator.next());

        assertFalse(iterator.hasNext());
    }

    @Test
    void testIteratorEmptyList() {
        LinkedListTabulatedFunctionX emptyFunction = new LinkedListTabulatedFunctionX(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        emptyFunction.remove(0);
        emptyFunction.remove(0);

        Iterator<Point> iterator = emptyFunction.iterator();
        assertFalse(iterator.hasNext());
    }

    @Test
    void testIteratorNoSuchElement() {
        Iterator<Point> iterator = function.iterator();

        // Проходим по всем элементам
        while (iterator.hasNext()) {
            iterator.next();
        }

        // Теперь должен бросать исключение
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void testIteratorForEachLoop() {
        int index = 0;
        for (Point point : function) {
            assertEquals(xValues[index], point.x, 1e-12);
            assertEquals(yValues[index], point.y, 1e-12);
            index++;
        }
        assertEquals(4, index);
    }

    @Test
    void testIteratorAfterModification() {
        function.setY(1, 5.0);

        Iterator<Point> iterator = function.iterator();
        assertEquals(new Point(1.0, 1.0), iterator.next());
        assertEquals(new Point(2.0, 5.0), iterator.next()); // Проверяем измененное значение
    }

    // Тесты для проверки покрытия всех веток кода
    @Test
    void testFloorIndexOfXEdgeCases() {
        // Тестируем граничные случаи для floorIndexOfX
        assertEquals(0, function.floorIndexOfX(1.0)); // точно первая точка
        assertEquals(3, function.floorIndexOfX(4.0)); // точно последняя точка
        assertEquals(3, function.floorIndexOfX(4.5)); // за последней точкой
    }

    @Test
    void testRemoveEdgeCases() {
        // Тестируем удаление при разных сценариях
        LinkedListTabulatedFunctionX func = new LinkedListTabulatedFunctionX(
                new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0});

        // Удаляем первый элемент
        func.remove(0);
        assertEquals(2, func.getCount());
        assertEquals(2.0, func.leftBound());

        // Удаляем последний элемент
        func.remove(1);
        assertEquals(1, func.getCount());
        assertEquals(2.0, func.rightBound());
    }

    @Test
    void testExtrapolationWithDifferentScenarios() {
        // Тестируем экстраполяцию с разными функциями
        LinkedListTabulatedFunctionX linearFunc = new LinkedListTabulatedFunctionX(
                new double[]{0.0, 1.0}, new double[]{0.0, 1.0});

        double leftResult = linearFunc.extrapolateLeft(-1.0);
        assertEquals(-1.0, leftResult, 1e-12);

        double rightResult = linearFunc.extrapolateRight(2.0);
        assertEquals(2.0, rightResult, 1e-12);
    }

    // Тесты для проверки итератора с помощью цикла while
    @Test
    void testIteratorWithWhileLoop() {
        Iterator<Point> iterator = function.iterator();
        int index = 0;

        // Используем цикл while для обхода всех элементов
        while (iterator.hasNext()) {
            Point point = iterator.next();
            assertEquals(xValues[index], point.x, 1e-12);
            assertEquals(yValues[index], point.y, 1e-12);
            index++;
        }

        // Проверяем, что прошли все точки
        assertEquals(4, index);
        // Проверяем, что итератор действительно закончился
        assertFalse(iterator.hasNext());
    }

    @Test
    void testIteratorWithWhileLoopOnEmptyList() {
        LinkedListTabulatedFunctionX emptyFunction = new LinkedListTabulatedFunctionX(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        emptyFunction.remove(0);
        emptyFunction.remove(0);

        Iterator<Point> iterator = emptyFunction.iterator();

        // Цикл while не должен выполняться ни разу
        int iterationCount = 0;
        while (iterator.hasNext()) {
            iterator.next();
            iterationCount++;
        }

        assertEquals(0, iterationCount);
        assertFalse(iterator.hasNext());
    }

    @Test
    void testIteratorWithWhileLoopAfterModification() {
        // Модифицируем данные перед созданием итератора
        function.setY(1, 5.0);
        function.setY(2, 10.0);

        Iterator<Point> iterator = function.iterator();
        int index = 0;

        // Проверяем модифицированные данные через while цикл
        while (iterator.hasNext()) {
            Point point = iterator.next();
            if (index == 1) {
                assertEquals(5.0, point.y, 1e-12); // Проверяем измененное значение
            } else if (index == 2) {
                assertEquals(10.0, point.y, 1e-12); // Проверяем измененное значение
            }
            index++;
        }

        assertEquals(4, index);
    }

    // Тесты для проверки итератора с помощью цикла for-each
    @Test
    void testIteratorWithForEachLoop() {
        int index = 0;

        // Используем цикл for-each для обхода всех элементов
        for (Point point : function) {
            assertEquals(xValues[index], point.x, 1e-12);
            assertEquals(yValues[index], point.y, 1e-12);
            index++;
        }

        // Проверяем, что прошли все точки
        assertEquals(4, index);
    }

    @Test
    void testIteratorWithForEachLoopOnEmptyList() {
        LinkedListTabulatedFunctionX emptyFunction = new LinkedListTabulatedFunctionX(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        emptyFunction.remove(0);
        emptyFunction.remove(0);

        // Цикл for-each не должен выполняться ни разу
        int iterationCount = 0;
        for (Point point : emptyFunction) {
            iterationCount++;
        }

        assertEquals(0, iterationCount);
    }

    @Test
    void testIteratorWithForEachLoopAfterModification() {
        // Модифицируем данные
        function.setY(0, 0.0);
        function.setY(3, 20.0);

        int index = 0;
        double[] expectedY = {0.0, 4.0, 9.0, 20.0}; // Ожидаемые значения после модификации

        // Проверяем модифицированные данные через for-each цикл
        for (Point point : function) {
            assertEquals(xValues[index], point.x, 1e-12);
            assertEquals(expectedY[index], point.y, 1e-12);
            index++;
        }

        assertEquals(4, index);
    }

    // Тесты для сравнения двух способов итерации
    @Test
    void testWhileLoopVsForEachLoopEquivalence() {
        // Собираем точки через while цикл
        java.util.List<Point> whileLoopPoints = new java.util.ArrayList<>();
        Iterator<Point> iterator = function.iterator();
        while (iterator.hasNext()) {
            whileLoopPoints.add(iterator.next());
        }

        // Собираем точки через for-each цикл
        java.util.List<Point> forEachLoopPoints = new java.util.ArrayList<>();
        for (Point point : function) {
            forEachLoopPoints.add(point);
        }

        // Проверяем, что оба способа дают одинаковый результат
        assertEquals(whileLoopPoints.size(), forEachLoopPoints.size());
        for (int i = 0; i < whileLoopPoints.size(); i++) {
            assertEquals(whileLoopPoints.get(i).x, forEachLoopPoints.get(i).x, 1e-12);
            assertEquals(whileLoopPoints.get(i).y, forEachLoopPoints.get(i).y, 1e-12);
        }
    }

    // Тесты для проверки независимости итераторов
    @Test
    void testMultipleIndependentIterators() {
        Iterator<Point> iterator1 = function.iterator();
        Iterator<Point> iterator2 = function.iterator();

        // Используем разные циклы для разных итераторов
        int count1 = 0;
        while (iterator1.hasNext()) {
            iterator1.next();
            count1++;
        }

        int count2 = 0;
        for (Point point : function) { // Создается новый итератор внутри for-each
            count2++;
        }

        // Оба должны пройти все элементы
        assertEquals(4, count1);
        assertEquals(4, count2);

        // Первый итератор должен быть исчерпан
        assertFalse(iterator1.hasNext());

        // Новый итератор через for-each должен работать корректно
        int count3 = 0;
        for (Point point : function) {
            count3++;
        }
        assertEquals(4, count3);
    }

    // Тесты для проверки порядка элементов в итераторе
    @Test
    void testIteratorOrder() {
        double[] expectedX = {1.0, 2.0, 3.0, 4.0};
        double[] expectedY = {1.0, 4.0, 9.0, 16.0};

        // Проверяем порядок через while цикл
        Iterator<Point> iterator = function.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Point point = iterator.next();
            assertEquals(expectedX[index], point.x, 1e-12, "X value mismatch at index " + index);
            assertEquals(expectedY[index], point.y, 1e-12, "Y value mismatch at index " + index);
            index++;
        }

        // Проверяем порядок через for-each цикл
        index = 0;
        for (Point point : function) {
            assertEquals(expectedX[index], point.x, 1e-12, "X value mismatch at index " + index);
            assertEquals(expectedY[index], point.y, 1e-12, "Y value mismatch at index " + index);
            index++;
        }
    }

    // Тесты для проверки итератора после удаления элементов
    @Test
    void testIteratorAfterRemoveWithWhileLoop() {
        // Удаляем некоторые элементы
        function.remove(1); // Удаляем второй элемент

        Iterator<Point> iterator = function.iterator();
        java.util.List<Point> points = new java.util.ArrayList<>();

        // Собираем оставшиеся точки через while цикл
        while (iterator.hasNext()) {
            points.add(iterator.next());
        }

        // Проверяем, что остались правильные точки
        assertEquals(3, points.size());
        assertEquals(1.0, points.get(0).x, 1e-12);
        assertEquals(3.0, points.get(1).x, 1e-12);
        assertEquals(4.0, points.get(2).x, 1e-12);
    }

    @Test
    void testIteratorAfterRemoveWithForEachLoop() {
        // Удаляем некоторые элементы
        function.remove(0); // Удаляем первый элемент
        function.remove(1); // Удаляем третий элемент (после первого удаления)

        java.util.List<Point> points = new java.util.ArrayList<>();

        // Собираем оставшиеся точки через for-each цикл
        for (Point point : function) {
            points.add(point);
        }

        // Проверяем, что остались правильные точки
        assertEquals(2, points.size());
        assertEquals(2.0, points.get(0).x, 1e-12);
        assertEquals(4.0, points.get(1).x, 1e-12);
    }

    // Тест для проверки, что итератор не модифицирует исходную коллекцию
    @Test
    void testIteratorDoesNotModifyCollection() {
        int initialCount = function.getCount();

        // Используем while цикл
        Iterator<Point> iterator = function.iterator();
        while (iterator.hasNext()) {
            Point point = iterator.next();
            // Просто читаем точки, не модифицируем
        }

        // Проверяем, что коллекция не изменилась
        assertEquals(initialCount, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-12);
        assertEquals(4.0, function.getY(1), 1e-12);

        // Используем for-each цикл
        for (Point point : function) {
            // Просто читаем точки, не модифицируем
        }

        // Проверяем, что коллекция не изменилась
        assertEquals(initialCount, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-12);
        assertEquals(4.0, function.getY(1), 1e-12);
    }

    // Тест для проверки работы итератора с разными размерами коллекций
    @Test
    void testIteratorWithDifferentSizedCollections() {
        // Тестируем с коллекцией из 2 элементов
        LinkedListTabulatedFunctionX twoElementFunction = new LinkedListTabulatedFunctionX(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0});

        int count = 0;
        for (Point point : twoElementFunction) {
            count++;
        }
        assertEquals(2, count);

        // Тестируем с коллекцией из 3 элементов
        LinkedListTabulatedFunctionX threeElementFunction = new LinkedListTabulatedFunctionX(
                new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0});

        count = 0;
        Iterator<Point> iterator = threeElementFunction.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        assertEquals(3, count);
    }
}