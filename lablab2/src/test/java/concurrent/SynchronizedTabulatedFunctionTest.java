package concurrent;

import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.Point;
import functions.TabulatedFunction;
import org.junit.Test;

import static org.junit.Assert.*;



import java.util.Iterator;
import java.util.NoSuchElementException;



public class SynchronizedTabulatedFunctionTest {

    @Test
    public void testGetCount() {
        double[] xValues = {1, 2, 3};
        double[] yValues = {10, 20, 30};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction syncArrayFunc = new SynchronizedTabulatedFunction(arrayFunc);

        TabulatedFunction linkedFunc = new LinkedListTabulatedFunction(xValues, yValues);
        TabulatedFunction syncLinkedFunc = new SynchronizedTabulatedFunction(linkedFunc);

        assertEquals(3, syncArrayFunc.getCount());
        assertEquals(3, syncLinkedFunc.getCount());
    }

    @Test
    public void testGetX() {
        double[] xValues = {1, 2, 3};
        double[] yValues = {10, 20, 30};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        assertEquals(1.0, syncFunc.getX(0), 0.0001);
        assertEquals(2.0, syncFunc.getX(1), 0.0001);
        assertEquals(3.0, syncFunc.getX(2), 0.0001);
    }

    @Test
    public void testGetY() {
        double[] xValues = {1, 2, 3};
        double[] yValues = {10, 20, 30};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        assertEquals(10.0, syncFunc.getY(0), 0.0001);
        assertEquals(20.0, syncFunc.getY(1), 0.0001);
        assertEquals(30.0, syncFunc.getY(2), 0.0001);
    }

    @Test
    public void testSetY() {
        double[] xValues = {1, 2, 3};
        double[] yValues = {10, 20, 30};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        syncFunc.setY(1, 25.0);
        assertEquals(25.0, syncFunc.getY(1), 0.0001);

        syncFunc.setY(2, 35.0);
        assertEquals(35.0, syncFunc.getY(2), 0.0001);
    }

    @Test
    public void testIndexOfX() {
        double[] xValues = {1, 2, 3};
        double[] yValues = {10, 20, 30};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        assertEquals(1, syncFunc.indexOfX(2.0));
        assertEquals(-1, syncFunc.indexOfX(5.0));
    }

    @Test
    public void testIndexOfY() {
        double[] xValues = {1, 2, 3};
        double[] yValues = {10, 20, 30};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        assertEquals(1, syncFunc.indexOfY(20.0));
        assertEquals(-1, syncFunc.indexOfY(50.0));
    }

    @Test
    public void testLeftBound() {
        double[] xValues = {1, 2, 3};
        double[] yValues = {10, 20, 30};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        assertEquals(1.0, syncFunc.leftBound(), 0.0001);
    }

    @Test
    public void testRightBound() {
        double[] xValues = {1, 2, 3};
        double[] yValues = {10, 20, 30};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        assertEquals(3.0, syncFunc.rightBound(), 0.0001);
    }

    @Test
    public void testApply() {
        double[] xValues = {1, 2, 3};
        double[] yValues = {10, 20, 30};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        assertEquals(20.0, syncFunc.apply(2.0), 0.0001);
        assertEquals(15.0, syncFunc.apply(1.5), 0.0001);
    }

    @Test
    public void testIterator() {
        double[] xValues = {1, 2, 3};
        double[] yValues = {10, 20, 30};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        int count = 0;
        for (Point point : syncFunc) {
            assertNotNull(point);
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    public void testWithLinkedListTabulatedFunction() {
        double[] xValues = {0, 1, 2};
        double[] yValues = {0, 1, 4};

        TabulatedFunction linkedFunc = new LinkedListTabulatedFunction(xValues, yValues);
        TabulatedFunction syncFunc = new SynchronizedTabulatedFunction(linkedFunc);

        assertEquals(3, syncFunc.getCount());
        assertEquals(1.0, syncFunc.apply(1.0), 0.0001);
        assertEquals(2.5, syncFunc.apply(1.5), 0.0001);
    }


    @Test
    void testIteratorHasNext() {
        TabulatedFunction innerFunc = new LinkedListTabulatedFunction(0, 5, 3);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(innerFunc);

        Iterator<Point> iterator = syncFunc.iterator();

        assertTrue(iterator.hasNext(), "Iterator should have next element");

        // Пройдем по всем элементам
        iterator.next();
        assertTrue(iterator.hasNext(), "Iterator should have second element");

        iterator.next();
        assertTrue(iterator.hasNext(), "Iterator should have third element");

        iterator.next();
        assertFalse(iterator.hasNext(), "Iterator should not have more elements after last");
    }

    @Test
    void testIteratorNext() {
        TabulatedFunction innerFunc = new LinkedListTabulatedFunction(0, 10, 3);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(innerFunc);

        Iterator<Point> iterator = syncFunc.iterator();

        Point first = iterator.next();
        assertEquals(0.0, first.x, 1e-9, "First point x should be 0");
        assertEquals(0.0, first.y, 1e-9, "First point y should be 0");

        Point second = iterator.next();
        assertEquals(5.0, second.x, 1e-9, "Second point x should be 5");
        assertEquals(5.0, second.y, 1e-9, "Second point y should be 5");

        Point third = iterator.next();
        assertEquals(10.0, third.x, 1e-9, "Third point x should be 10");
        assertEquals(10.0, third.y, 1e-9, "Third point y should be 10");
    }

    @Test
    void testIteratorNextThrowsException() {
        TabulatedFunction innerFunc = new LinkedListTabulatedFunction(0, 2, 2);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(innerFunc);

        Iterator<Point> iterator = syncFunc.iterator();

        iterator.next();
        iterator.next();

        assertThrows(NoSuchElementException.class, iterator::next,
                "Should throw NoSuchElementException when no more elements");
    }

    @Test
    void testIteratorRemoveThrowsException() {
        TabulatedFunction innerFunc = new LinkedListTabulatedFunction(0, 2, 2);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(innerFunc);

        Iterator<Point> iterator = syncFunc.iterator();
        iterator.next();

        assertThrows(UnsupportedOperationException.class, iterator::remove,
                "Should throw UnsupportedOperationException for remove operation");
    }

    @Test
    void testIteratorWithSingleElement() {
        TabulatedFunction innerFunc = new LinkedListTabulatedFunction(1, 5, 1);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(innerFunc);

        Iterator<Point> iterator = syncFunc.iterator();

        assertTrue(iterator.hasNext(), "Iterator should have one element");
        Point point = iterator.next();
        assertEquals(1.0, point.x, 1e-9, "Single point x should be 1");
        assertEquals(5.0, point.y, 1e-9, "Single point y should be 5");
        assertFalse(iterator.hasNext(), "Iterator should not have more elements");
    }

    @Test
    void testIteratorWithEmptyFunction() {
        // Создаем функцию с 0 точками (если такая возможность есть)
        // Если нет, то тестируем с минимальным количеством точек (1)
        TabulatedFunction innerFunc = new LinkedListTabulatedFunction(0, 0, 1);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(innerFunc);

        Iterator<Point> iterator = syncFunc.iterator();

        // Для функции с 1 точкой
        assertTrue(iterator.hasNext(), "Iterator should have at least one element");
        iterator.next();

        // Если бы была возможность создать пустую функцию:
        // assertFalse(iterator.hasNext(), "Iterator should be empty for empty function");
    }

    @Test
    void testIteratorIsolationFromModifications() {
        TabulatedFunction innerFunc = new LinkedListTabulatedFunction(0, 4, 3);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(innerFunc);

        // Получаем итератор
        Iterator<Point> iterator = syncFunc.iterator();

        // Модифицируем исходную функцию
        syncFunc.setY(0, 100);
        syncFunc.setY(1, 200);
        syncFunc.setY(2, 300);

        // Итератор должен работать со старыми значениями (копией)
        Point first = iterator.next();
        assertEquals(0.0, first.x, 1e-9, "First point x should be original 0");
        assertEquals(0.0, first.y, 1e-9, "First point y should be original 0, not modified 100");

        Point second = iterator.next();
        assertEquals(2.0, second.x, 1e-9, "Second point x should be original 2");
        assertEquals(2.0, second.y, 1e-9, "Second point y should be original 2, not modified 200");

        Point third = iterator.next();
        assertEquals(4.0, third.x, 1e-9, "Third point x should be original 4");
        assertEquals(4.0, third.y, 1e-9, "Third point y should be original 4, not modified 300");
    }

    @Test
    void testMultipleIteratorsIndependent() {
        TabulatedFunction innerFunc = new LinkedListTabulatedFunction(0, 2, 2);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(innerFunc);

        Iterator<Point> iterator1 = syncFunc.iterator();
        Iterator<Point> iterator2 = syncFunc.iterator();

        // Оба итератора должны работать независимо
        Point point1 = iterator1.next();
        Point point2 = iterator2.next();

        assertEquals(point1.x, point2.x, 1e-9, "Both iterators should return same x for first element");
        assertEquals(point1.y, point2.y, 1e-9, "Both iterators should return same y for first element");

        // Пройдем один итератор до конца
        iterator1.next();
        assertFalse(iterator1.hasNext(), "First iterator should be exhausted");
        assertTrue(iterator2.hasNext(), "Second iterator should still have elements");

        iterator2.next();
        assertFalse(iterator2.hasNext(), "Second iterator should be exhausted after consuming all elements");
    }


}