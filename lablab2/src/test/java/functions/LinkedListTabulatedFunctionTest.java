package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Iterator;
import exceptions.*;
import java.io.*;

public class LinkedListTabulatedFunctionTest {

    @Test
    public void testInsertInMiddleWithNonNullNext() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1.0, 3.0, 4.0}, new double[]{1.0, 9.0, 16.0}
        );
        function.insert(2.0, 4.0);

        assertEquals(4, function.getCount());
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(4.0, function.getY(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(9.0, function.getY(2), 1e-10);
    }

    @Test
    public void testInsertIntoEmptyList() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction();
        function.insert(2.0, 5.0);
        assertEquals(1, function.getCount());
        assertEquals(2.0, function.getX(0), 1e-10);
        assertEquals(5.0, function.getY(0), 1e-10);
    }

    @Test
    public void testInsertAtBeginning() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{3.0, 4.0}, new double[]{9.0, 16.0}
        );
        function.insert(1.0, 1.0);
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(3.0, function.getX(1), 1e-10);
        assertEquals(4.0, function.getX(2), 1e-10);
    }

    @Test
    public void testInsertAtEnd() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0}
        );
        function.insert(3.0, 9.0);
        assertEquals(3, function.getCount());
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(9.0, function.getY(2), 1e-10);
    }

    @Test
    public void testInsertAtEndWithNullCurrent() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0}
        );
        function.insert(3.0, 9.0);
        assertEquals(3, function.getCount());
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(9.0, function.getY(2), 1e-10);
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
    }

    @Test
    public void testInsertInMiddle() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1.0, 3.0, 4.0}, new double[]{1.0, 9.0, 16.0}
        );
        function.insert(2.0, 4.0);
        assertEquals(4, function.getCount());
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(4.0, function.getY(1), 1e-10);
    }

    @Test
    public void testInsertDuplicateX() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0}
        );
        function.insert(2.0, 20.0);
        assertEquals(3, function.getCount());
        assertEquals(20.0, function.getY(1), 1e-10);
    }

    @Test
    public void testInsertUpdatesCount() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction();
        assertEquals(0, function.getCount());
        function.insert(1.0, 1.0);
        assertEquals(1, function.getCount());
        function.insert(2.0, 4.0);
        assertEquals(2, function.getCount());
        function.insert(1.0, 10.0);
        assertEquals(2, function.getCount());
    }

    @Test
    public void testInsertMaintainsOrdering() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction();
        function.insert(5.0, 25.0);
        function.insert(1.0, 1.0);
        function.insert(3.0, 9.0);
        function.insert(2.0, 4.0);
        function.insert(4.0, 16.0);
        assertEquals(5, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(4.0, function.getX(3), 1e-10);
        assertEquals(5.0, function.getX(4), 1e-10);
    }

    @Test
    public void testInsertWithNegativeValues() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction();
        function.insert(-2.0, 4.0);
        function.insert(-1.0, 1.0);
        function.insert(-3.0, 9.0);
        assertEquals(3, function.getCount());
        assertEquals(-3.0, function.getX(0), 1e-10);
        assertEquals(-2.0, function.getX(1), 1e-10);
        assertEquals(-1.0, function.getX(2), 1e-10);
    }

    @Test
    public void testInsertWithSameXMultipleTimes() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction();
        function.insert(1.0, 10.0);
        function.insert(1.0, 20.0);
        function.insert(1.0, 30.0);
        assertEquals(1, function.getCount());
        assertEquals(30.0, function.getY(0), 1e-10);
    }

    @Test
    public void testInsertPreservesListStructure() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1.0, 3.0}, new double[]{1.0, 9.0}
        );
        function.insert(2.0, 4.0);
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(1.0, function.getY(0), 1e-10);
        assertEquals(4.0, function.getY(1), 1e-10);
        assertEquals(9.0, function.getY(2), 1e-10);
    }

    @Test
    public void testInsertIntoFunctionFromMathFunction() {
        MathFunction square = x -> x * x;
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(square, 0.0, 2.0, 3);
        assertEquals(3, function.getCount());
        function.insert(1.5, 2.25);
        assertEquals(4, function.getCount());
        assertEquals(1.5, function.getX(2), 1e-10);
    }

    @Test
    public void testEmptyFunctionBounds() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction();
        assertThrows(IllegalStateException.class, () -> function.leftBound());
        assertThrows(IllegalStateException.class, () -> function.rightBound());
    }

    @Test
    public void testIterator() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0}
        );
        Iterator<Point> iterator = function.iterator();
        assertNotNull(iterator);
        assertTrue(iterator.hasNext());
        Point firstPoint = iterator.next();
        assertEquals(1.0, firstPoint.x, 1e-10);
        assertEquals(1.0, firstPoint.y, 1e-10);
        assertTrue(iterator.hasNext());
        Point secondPoint = iterator.next();
        assertEquals(2.0, secondPoint.x, 1e-10);
        assertEquals(4.0, secondPoint.y, 1e-10);
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testConstructorWithArrays() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.leftBound(), 1e-10);
        assertEquals(3.0, function.rightBound(), 1e-10);
    }

    @Test
    public void testConstructorWithMathFunction() {
        MathFunction square = x -> x * x;
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(square, 0.0, 2.0, 3);
        assertEquals(3, function.getCount());
        assertEquals(0.0, function.leftBound(), 1e-10);
        assertEquals(2.0, function.rightBound(), 1e-10);
    }

    @Test
    public void testGetX() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
    }

    @Test
    public void testGetY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(1.0, function.getY(0), 1e-10);
        assertEquals(4.0, function.getY(1), 1e-10);
        assertEquals(9.0, function.getY(2), 1e-10);
    }

    @Test
    public void testSetY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        function.setY(1, 5.0);
        assertEquals(5.0, function.getY(1), 1e-10);
    }

    @Test
    public void testIndexOfX() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(0, function.indexOfX(1.0));
        assertEquals(1, function.indexOfX(2.0));
        assertEquals(-1, function.indexOfX(4.0));
    }

    @Test
    public void testIndexOfY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(0, function.indexOfY(1.0));
        assertEquals(1, function.indexOfY(4.0));
        assertEquals(-1, function.indexOfY(5.0));
    }

    @Test
    public void testApply() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(1.0, function.apply(1.0), 1e-10);
        assertEquals(4.0, function.apply(2.0), 1e-10);
        assertEquals(9.0, function.apply(3.0), 1e-10);
    }

    @Test
    public void testFloorIndexOfXWithInsufficientData() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction();
        assertThrows(IllegalStateException.class, () -> function.floorIndexOfX(1.0));
        function.insert(1.0, 2.0);
        assertThrows(IllegalStateException.class, () -> function.floorIndexOfX(1.0));
    }

    @Test
    public void testFloorIndexOfXLeftExtrapolation() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(0, function.floorIndexOfX(0.5));
    }

    @Test
    public void testFloorIndexOfXRightExtrapolation() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(1, function.floorIndexOfX(3.0));
        assertEquals(1, function.floorIndexOfX(4.0));
    }

    @Test
    public void testFloorIndexOfXInterpolation() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(1, function.floorIndexOfX(2.5));
        assertEquals(2, function.floorIndexOfX(3.5));
    }

    @Test
    public void testExtrapolateLeft() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        double result = function.apply(0.0);
        assertEquals(-2.0, result, 1e-10);
    }

    @Test
    public void testExtrapolateRight() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        double result = function.apply(4.0);
        assertEquals(14.0, result, 1e-10);
    }

    @Test
    public void testInterpolate() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(2.5, function.apply(1.5), 1e-10);
        assertEquals(6.5, function.apply(2.5), 1e-10);
    }

    @Test
    public void testInterpolateWithInvalidIndex() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertThrows(IllegalArgumentException.class, () -> function.interpolate(2.0, -1));
        assertThrows(IllegalArgumentException.class, () -> function.interpolate(2.0, 2));
    }

    @Test
    public void testInterpolateOutsideInterval() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertThrows(InterpolationException.class, () -> function.interpolate(0.5, 0));
        assertThrows(InterpolationException.class, () -> function.interpolate(2.5, 0));
    }

    @Test
    public void testInterpolateMethod() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        double result = function.interpolate(1.5, 1.0, 2.0, 1.0, 4.0);
        assertEquals(2.5, result, 1e-10);
    }

    @Test
    public void testInterpolateWithEqualX() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        double result = function.interpolate(1.5, 1.0, 1.0, 1.0, 4.0);
        assertEquals(2.5, result, 1e-10);
    }

    @Test
    public void testClone() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction original = new LinkedListTabulatedFunction(xValues, yValues);
        LinkedListTabulatedFunction cloned = original.clone();
        assertNotSame(original, cloned);
        assertEquals(original.getCount(), cloned.getCount());
        for (int i = 0; i < original.getCount(); i++) {
            assertEquals(original.getX(i), cloned.getX(i), 1e-10);
            assertEquals(original.getY(i), cloned.getY(i), 1e-10);
        }
    }

    @Test
    public void testCloneEmpty() {
        LinkedListTabulatedFunction original = new LinkedListTabulatedFunction();
        LinkedListTabulatedFunction cloned = original.clone();
        assertNotSame(original, cloned);
        assertEquals(0, cloned.getCount());
    }

    @Test
    public void testEquals() {
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function1 = new LinkedListTabulatedFunction(xValues1, yValues1);
        double[] xValues2 = {1.0, 2.0, 3.0};
        double[] yValues2 = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function2 = new LinkedListTabulatedFunction(xValues2, yValues2);
        double[] xValues3 = {1.0, 2.0, 4.0};
        double[] yValues3 = {1.0, 4.0, 16.0};
        LinkedListTabulatedFunction function3 = new LinkedListTabulatedFunction(xValues3, yValues3);
        assertTrue(function1.equals(function2));
        assertFalse(function1.equals(function3));
        assertFalse(function1.equals(null));
        assertFalse(function1.equals("string"));
    }

    @Test
    public void testHashCode() {
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function1 = new LinkedListTabulatedFunction(xValues1, yValues1);
        double[] xValues2 = {1.0, 2.0, 3.0};
        double[] yValues2 = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function2 = new LinkedListTabulatedFunction(xValues2, yValues2);
        assertEquals(function1.hashCode(), function2.hashCode());
    }

    @Test
    public void testToString() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        String result = function.toString();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testIteratorForEach() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        int index = 0;
        for (Point point : function) {
            assertNotNull(point);
            assertEquals(xValues[index], point.x, 1e-10);
            assertEquals(yValues[index], point.y, 1e-10);
            index++;
        }
        assertEquals(xValues.length, index);
    }

    @Test
    public void testIteratorRemove() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        Iterator<Point> iterator = function.iterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, () -> iterator.remove());
    }

    @Test
    public void testIteratorNoSuchElement() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction();
        Iterator<Point> iterator = function.iterator();
        assertThrows(java.util.NoSuchElementException.class, () -> iterator.next());
    }

    @Test
    public void testInvalidIndexAccess() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertThrows(IllegalArgumentException.class, () -> function.getX(-1));
        assertThrows(IllegalArgumentException.class, () -> function.getX(2));
        assertThrows(IllegalArgumentException.class, () -> function.getY(-1));
        assertThrows(IllegalArgumentException.class, () -> function.setY(-1, 5.0));
    }

    @Test
    public void testApplyWithExactXValue() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(1.0, function.apply(1.0), 1e-10);
        assertEquals(4.0, function.apply(2.0), 1e-10);
        assertEquals(9.0, function.apply(3.0), 1e-10);
    }

    @Test
    public void testApplyWithNegativeValues() {
        double[] xValues = {-2.0, -1.0, 0.0, 1.0, 2.0};
        double[] yValues = {4.0, 1.0, 0.0, 1.0, 4.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(4.0, function.apply(-2.0), 1e-10);
        assertEquals(1.0, function.apply(-1.0), 1e-10);
        assertEquals(0.0, function.apply(0.0), 1e-10);
        assertEquals(1.0, function.apply(1.0), 1e-10);
        assertEquals(4.0, function.apply(2.0), 1e-10);
    }

    @Test
    public void testApplyWithFractionalInterpolation() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(0.5, function.apply(0.5), 1e-10);
        assertEquals(2.5, function.apply(1.5), 1e-10);
    }

    @Test
    public void testApplyWithCloseToBoundaryValues() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(1.0, function.apply(1.0 + 1e-13), 1e-8);
        assertEquals(9.0, function.apply(3.0 - 1e-13), 1e-8);
    }

    @Test
    public void testApplyWithExactBoundaryValues() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(0.0, function.apply(0.0), 1e-12);
        assertEquals(1.0, function.apply(1.0), 1e-12);
        assertEquals(4.0, function.apply(2.0), 1e-12);
        assertEquals(9.0, function.apply(3.0), 1e-12);
    }

    @Test
    public void testApplyWithInterpolationBetweenAllPoints() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(0.25, function.apply(0.25), 1e-10);
        assertEquals(0.5, function.apply(0.5), 1e-10);
        assertEquals(0.75, function.apply(0.75), 1e-10);
        assertEquals(1.75, function.apply(1.25), 1e-10);
        assertEquals(2.5, function.apply(1.5), 1e-10);
        assertEquals(3.25, function.apply(1.75), 1e-10);
        assertEquals(6.5, function.apply(2.5), 1e-10);
        assertEquals(7.75, function.apply(2.75), 1e-10);
    }

    @Test
    public void testConstructorWithNullArrays() {
        assertThrows(NullPointerException.class, () -> new LinkedListTabulatedFunction(null, new double[]{1.0, 2.0}));
        assertThrows(NullPointerException.class, () -> new LinkedListTabulatedFunction(new double[]{1.0, 2.0}, null));
    }

    @Test
    public void testConstructorWithNullFunction() {
        assertThrows(NullPointerException.class, () -> new LinkedListTabulatedFunction(null, 0.0, 1.0, 3));
    }

    @Test
    public void testConstructorWithInvalidCount() {
        MathFunction square = x -> x * x;
        assertThrows(IllegalArgumentException.class, () -> new LinkedListTabulatedFunction(square, 0.0, 1.0, 1));
    }

    @Test
    public void testConstructorWithInvalidRange() {
        MathFunction square = x -> x * x;
        assertThrows(IllegalArgumentException.class, () -> new LinkedListTabulatedFunction(square, 2.0, 1.0, 3));
    }

    @Test
    public void testConstructorWithDifferentArrayLengths() {
        assertThrows(DifferentLengthOfArraysException.class, () -> new LinkedListTabulatedFunction(new double[]{1.0, 2.0}, new double[]{1.0}));
    }

    @Test
    public void testConstructorWithUnsortedArray() {
        assertThrows(ArrayIsNotSortedException.class, () -> new LinkedListTabulatedFunction(new double[]{2.0, 1.0}, new double[]{4.0, 1.0}));
    }

    @Test
    public void testConstructorWithSinglePoint() {
        assertThrows(IllegalArgumentException.class, () -> new LinkedListTabulatedFunction(new double[]{1.0}, new double[]{2.0}));
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction original = new LinkedListTabulatedFunction(xValues, yValues);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        LinkedListTabulatedFunction deserialized = (LinkedListTabulatedFunction) ois.readObject();
        ois.close();
        assertEquals(original.getCount(), deserialized.getCount());
        for (int i = 0; i < original.getCount(); i++) {
            assertEquals(original.getX(i), deserialized.getX(i), 1e-10);
            assertEquals(original.getY(i), deserialized.getY(i), 1e-10);
        }
    }

    @Test
    public void testSerializationEmpty() throws IOException, ClassNotFoundException {
        LinkedListTabulatedFunction original = new LinkedListTabulatedFunction();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        LinkedListTabulatedFunction deserialized = (LinkedListTabulatedFunction) ois.readObject();
        ois.close();
        assertEquals(original.getCount(), deserialized.getCount());
        assertEquals(0, deserialized.getCount());
    }

    @Test
    public void testSerialVersionUID() throws Exception {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction();
        java.lang.reflect.Field field = LinkedListTabulatedFunction.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        long serialVersionUID = field.getLong(null);
        assertEquals(123456789L, serialVersionUID);
    }

    @Test
    public void testNodeSerialVersionUID() throws Exception {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction();
        java.lang.reflect.Field field = LinkedListTabulatedFunction.class.getDeclaredClasses()[0].getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        long serialVersionUID = field.getLong(null);
        assertEquals(987654321L, serialVersionUID);
    }

    @Test
    public void testDefaultConstructor() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction();
        assertEquals(0, function.getCount());
        assertThrows(IllegalStateException.class, () -> function.leftBound());
        assertThrows(IllegalStateException.class, () -> function.rightBound());
    }

    @Test
    public void testGetNode() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
    }

    @Test
    public void testFloorIndexOfXWithExactXValues() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(0, function.floorIndexOfX(1.0));
        assertEquals(1, function.floorIndexOfX(2.0));
        assertEquals(2, function.floorIndexOfX(3.0));
        assertEquals(2, function.floorIndexOfX(3.5));
        assertEquals(2, function.floorIndexOfX(4.0));
    }

    @Test
    public void testApplyWithAllBranches() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        assertEquals(-2.0, function.apply(0.0), 1e-10);
        assertEquals(14.0, function.apply(4.0), 1e-10);
        assertEquals(2.5, function.apply(1.5), 1e-10);
        assertEquals(6.5, function.apply(2.5), 1e-10);
        assertEquals(1.0, function.apply(1.0), 1e-10);
        assertEquals(4.0, function.apply(2.0), 1e-10);
        assertEquals(9.0, function.apply(3.0), 1e-10);
    }
}