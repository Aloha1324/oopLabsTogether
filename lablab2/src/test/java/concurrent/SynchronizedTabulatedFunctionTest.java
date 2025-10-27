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
    public void testIteratorAfterFix() {
        double[] xValues = {1, 2, 3};
        double[] yValues = {10, 20, 30};


        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction syncArrayFunc = new SynchronizedTabulatedFunction(arrayFunc);


        int count = 0;
        double[] expectedX = {1, 2, 3};
        double[] expectedY = {10, 20, 30};

        for (Point point : syncArrayFunc) {
            assertNotNull(point);
            assertEquals(expectedX[count], point.x, 0.0001);
            assertEquals(expectedY[count], point.y, 0.0001);
            count++;
        }
        assertEquals(3, count);


        TabulatedFunction linkedFunc = new LinkedListTabulatedFunction(xValues, yValues);
        TabulatedFunction syncLinkedFunc = new SynchronizedTabulatedFunction(linkedFunc);

        count = 0;
        for (Point point : syncLinkedFunc) {
            assertNotNull(point);
            assertEquals(expectedX[count], point.x, 0.0001);
            assertEquals(expectedY[count], point.y, 0.0001);
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    public void testIteratorHasNext() {
        double[] xValues = {1, 2};
        double[] yValues = {10, 20};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        Iterator<Point> iterator = syncFunc.iterator();

        assertTrue(iterator.hasNext());
        iterator.next();
        assertTrue(iterator.hasNext());
        iterator.next();
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testIteratorNext() {
        double[] xValues = {1, 2, 3};
        double[] yValues = {10, 20, 30};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        Iterator<Point> iterator = syncFunc.iterator();

        Point point1 = iterator.next();
        assertEquals(1.0, point1.x, 0.0001);
        assertEquals(10.0, point1.y, 0.0001);

        Point point2 = iterator.next();
        assertEquals(2.0, point2.x, 0.0001);
        assertEquals(20.0, point2.y, 0.0001);

        Point point3 = iterator.next();
        assertEquals(3.0, point3.x, 0.0001);
        assertEquals(30.0, point3.y, 0.0001);
    }

    @Test
    public void testIteratorNoSuchElementException() {
        double[] xValues = {1, 2}; // минимум 2 точки
        double[] yValues = {10, 20};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        Iterator<Point> iterator = syncFunc.iterator();

        iterator.next();
        iterator.next();

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testIteratorRemove() {
        double[] xValues = {1, 2, 3};
        double[] yValues = {10, 20, 30};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        Iterator<Point> iterator = syncFunc.iterator();


        iterator.next();

        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Test
    public void testIteratorConcurrentSafety() {
        double[] xValues = {1, 2, 3, 4, 5};
        double[] yValues = {10, 20, 30, 40, 50};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);


        syncFunc.doSynchronously(func -> {
            int count = 0;
            for (Point point : func) {
                assertNotNull(point);
                count++;
            }
            assertEquals(5, count);
            return null;
        });
    }

    @Test
    public void testIteratorWith2Points() {
        // мин кол-во точек - 2
        double[] xValues = {1, 2};
        double[] yValues = {10, 20};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        Iterator<Point> iterator = syncFunc.iterator();

        assertTrue(iterator.hasNext());
        Point point1 = iterator.next();
        assertEquals(1.0, point1.x, 0.0001);
        assertEquals(10.0, point1.y, 0.0001);

        assertTrue(iterator.hasNext());
        Point point2 = iterator.next();
        assertEquals(2.0, point2.x, 0.0001);
        assertEquals(20.0, point2.y, 0.0001);

        assertFalse(iterator.hasNext());
    }



}