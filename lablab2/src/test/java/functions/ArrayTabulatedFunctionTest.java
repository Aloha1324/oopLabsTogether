package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Iterator;
import java.io.*;

public class ArrayTabulatedFunctionTest {

    @Test
    public void testConstructorWithArrays() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(4, function.getCount());
        assertEquals(1.0, function.leftBound(), 1e-10);
        assertEquals(4.0, function.rightBound(), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(9.0, function.getY(2), 1e-10);
    }

    @Test
    public void testConstructorWithFunction() {
        MathFunction source = x -> x * x;
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(source, 0, 4, 5);

        assertEquals(5, function.getCount());
        assertEquals(0.0, function.leftBound(), 1e-10);
        assertEquals(4.0, function.rightBound(), 1e-10);
        assertEquals(0.0, function.getY(0), 1e-10);
        assertEquals(16.0, function.getY(4), 1e-10);
    }

    @Test
    public void testFloorIndexOfX() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0, function.floorIndexOfX(1.5));
        assertEquals(1, function.floorIndexOfX(2.5));
        assertEquals(2, function.floorIndexOfX(3.5));
        assertEquals(2, function.floorIndexOfX(4.0));
        assertEquals(0, function.floorIndexOfX(0.0));
    }

    @Test
    public void testInterpolation() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(6.5, function.apply(2.5), 1e-12);
    }

    @Test
    public void testExtrapolation() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(-2.0, function.apply(0.0), 1e-12);
        assertEquals(14.0, function.apply(4.0), 1e-12);
    }

    @Test
    public void testSetY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.setY(1, 5.0);
        assertEquals(5.0, function.getY(1), 1e-10);
    }

    @Test
    public void testIndexOfX() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(1, function.indexOfX(2.0));
        assertEquals(-1, function.indexOfX(5.0));
    }

    @Test
    public void testIndexOfY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(2, function.indexOfY(9.0));
        assertEquals(-1, function.indexOfY(10.0));
    }

    @Test
    public void testInvalidConstructorWithShortArrays() {
        double[] xValues = {1.0};
        double[] yValues = {2.0};

        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(xValues, yValues));
    }

    @Test
    public void testInvalidConstructorWithNonUniqueX() {
        double[] badXValues = {1.0, 2.0, 2.0};
        double[] badYValues = {2.0, 3.0, 4.0};

        assertThrows(exceptions.ArrayIsNotSortedException.class, () ->
                new ArrayTabulatedFunction(badXValues, badYValues));
    }

    @Test
    public void testApplyWithExactXValues() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(1.0, function.apply(1.0), 1e-12);
        assertEquals(4.0, function.apply(2.0), 1e-12);
        assertEquals(9.0, function.apply(3.0), 1e-12);
    }

    @Test
    public void testApplyWithBoundaryValues() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0.0, function.apply(0.0), 1e-12);
        assertEquals(4.0, function.apply(2.0), 1e-12);
    }

    @Test
    public void testApplyWithReversedXFromXTo() {
        MathFunction source = x -> x * x;

        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(source, 4.0, 0.0, 5));

        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(source, 2.0, 2.0, 5));
    }

    @Test
    public void testApplyWithNegativeValues() {
        double[] xValues = {-2.0, -1.0, 0.0, 1.0, 2.0};
        double[] yValues = {4.0, 1.0, 0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(4.0, function.apply(-2.0), 1e-12);
        assertEquals(1.0, function.apply(-1.0), 1e-12);
    }

    @Test
    public void testApplyWithFractionalInterpolation() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0.5, function.apply(0.5), 1e-12);
        assertEquals(2.5, function.apply(1.5), 1e-12);
    }

    @Test
    public void testApplyWithCloseToBoundaryValues() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(1.0, function.apply(1.0 + 1e-13), 1e-12);
        assertEquals(9.0, function.apply(3.0 - 1e-13), 1e-12);
    }

    @Test
    public void testInvalidIndexAccess() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertThrows(IllegalArgumentException.class, () -> function.getX(-1));
        assertThrows(IllegalArgumentException.class, () -> function.getX(3));
        assertThrows(IllegalArgumentException.class, () -> function.getY(-1));
        assertThrows(IllegalArgumentException.class, () -> function.setY(-1, 10.0));
    }

    @Test
    public void testConstructorWithFunctionAndSmallCount() {
        MathFunction source = x -> x * x;

        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(source, 0, 1, 1));

        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(source, 0, 1, 0));
    }

    @Test
    public void testFindInsertIndexEdgeCases() {
        double[] xValues = {1.0, 3.0, 5.0};
        double[] yValues = {1.0, 9.0, 25.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(0.0, 0.0);
        assertEquals(0.0, function.getX(0), 1e-10);
        assertEquals(1.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(5.0, function.getX(3), 1e-10);
        assertEquals(4, function.getCount());

        function.insert(6.0, 36.0);
        assertEquals(0.0, function.getX(0), 1e-10);
        assertEquals(1.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(5.0, function.getX(3), 1e-10);
        assertEquals(6.0, function.getX(4), 1e-10);
        assertEquals(5, function.getCount());

        function.insert(2.0, 4.0);
        assertEquals(0.0, function.getX(0), 1e-10);
        assertEquals(1.0, function.getX(1), 1e-10);
        assertEquals(2.0, function.getX(2), 1e-10);
        assertEquals(3.0, function.getX(3), 1e-10);
        assertEquals(5.0, function.getX(4), 1e-10);
        assertEquals(6.0, function.getX(5), 1e-10);
        assertEquals(6, function.getCount());
    }

    @Test
    public void testFloorIndexOfXEdgeCases2() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0, function.floorIndexOfX(0.5));
        assertEquals(0, function.floorIndexOfX(1.0));
        assertEquals(0, function.floorIndexOfX(1.5));
        assertEquals(0, function.floorIndexOfX(2.0));
        assertEquals(0, function.floorIndexOfX(3.0));
    }

    @Test
    public void testFloorIndexOfXWithBinarySearch() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0, 16.0, 25.0, 36.0, 49.0, 64.0, 81.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(4, function.floorIndexOfX(4.5));
        assertEquals(7, function.floorIndexOfX(7.0));
        assertEquals(8, function.floorIndexOfX(8.9));
        assertEquals(0, function.floorIndexOfX(-1.0));
        assertEquals(8, function.floorIndexOfX(10.0));
    }

    @Test
    public void testApplyWithInterpolationBetweenAllPoints() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

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
    public void testCloneDeepCopy() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction original = new ArrayTabulatedFunction(xValues, yValues);

        ArrayTabulatedFunction cloned = original.clone();

        assertNotSame(original, cloned);
        assertEquals(original.getCount(), cloned.getCount());
        for (int i = 0; i < original.getCount(); i++) {
            assertEquals(original.getX(i), cloned.getX(i), 1e-10);
            assertEquals(original.getY(i), cloned.getY(i), 1e-10);
        }

        cloned.setY(1, 5.0);
        assertNotEquals(original.getY(1), cloned.getY(1));
        assertEquals(4.0, original.getY(1), 1e-10);
    }

    @Test
    public void testEqualsWithDifferentTypes() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertNotEquals(null, function);
        assertNotEquals("string", function);
        assertNotEquals(new Object(), function);
        assertNotEquals(new LinkedListTabulatedFunction(xValues, yValues), function);
    }

    @Test
    public void testHashCodeConsistency() {
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function1 = new ArrayTabulatedFunction(xValues1, yValues1);

        double[] xValues2 = {1.0, 2.0, 3.0};
        double[] yValues2 = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function2 = new ArrayTabulatedFunction(xValues2, yValues2);

        assertEquals(function1.hashCode(), function2.hashCode());
        int hashCode1 = function1.hashCode();
        int hashCode2 = function1.hashCode();
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    public void testToStringFormat() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        String result = function.toString();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testIteratorUsingWhileLoop() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        Iterator<Point> iterator = function.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Point point = iterator.next();
            assertNotNull(point);
            assertEquals(xValues[index], point.x);
            assertEquals(yValues[index], point.y);
            index++;
        }
        assertEquals(xValues.length, index);
    }

    @Test
    public void testIteratorUsingForEachLoop() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        int index = 0;
        for (Point point : function) {
            assertNotNull(point);
            assertEquals(xValues[index], point.x);
            assertEquals(yValues[index], point.y);
            index++;
        }
        assertEquals(xValues.length, index);
    }

    @Test
    public void testIteratorConcurrentModification() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        Iterator<Point> iterator = function.iterator();
        Point first = iterator.next();
        assertNotNull(first);
        function.setY(1, 5.0);
        assertTrue(iterator.hasNext());
        Point second = iterator.next();
        assertNotNull(second);
    }

    @Test
    public void testSerialization() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertTrue(function instanceof java.io.Serializable);

        try {
            java.lang.reflect.Field field = ArrayTabulatedFunction.class.getDeclaredField("serialVersionUID");
            field.setAccessible(true);
            long serialVersionUID = field.getLong(function);
            assertEquals(8305720685834923448L, serialVersionUID);
        } catch (Exception e) {
            fail("serialVersionUID not found or accessible");
        }
    }

    @Test
    public void testApplyWithExactBoundaryValues() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0.0, function.apply(0.0), 1e-12);
        assertEquals(1.0, function.apply(1.0), 1e-12);
        assertEquals(4.0, function.apply(2.0), 1e-12);
        assertEquals(9.0, function.apply(3.0), 1e-12);
    }

    @Test
    public void testApplyWithPrecisionBoundary() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        double epsilon = 1e-10;
        assertEquals(1.0, function.apply(1.0 + epsilon), 1e-8);
        assertEquals(9.0, function.apply(3.0 - epsilon), 1e-8);
    }

    @Test
    public void testIndexOfXWithPrecision() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0, function.indexOfX(1.0));
        assertEquals(1, function.indexOfX(2.0));
        assertEquals(-1, function.indexOfX(1.0 + 1e-10));
        assertEquals(-1, function.indexOfX(2.0 - 1e-10));
        assertEquals(-1, function.indexOfX(2.0001));
    }

    @Test
    public void testIndexOfYWithPrecision() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0, function.indexOfY(1.0));
        assertEquals(1, function.indexOfY(4.0));
        assertEquals(-1, function.indexOfY(1.0 + 1e-10));
        assertEquals(-1, function.indexOfY(4.0 - 1e-10));
        assertEquals(-1, function.indexOfY(4.0001));
    }

    @Test
    public void testFloorIndexOfXWithSingleInterval() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0, function.floorIndexOfX(0.5));
        assertEquals(0, function.floorIndexOfX(1.0));
        assertEquals(0, function.floorIndexOfX(1.5));
        assertEquals(0, function.floorIndexOfX(2.0));
        assertEquals(0, function.floorIndexOfX(3.0));
    }

    @Test
    public void testEqualsAndHashCode() {
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function1 = new ArrayTabulatedFunction(xValues1, yValues1);

        double[] xValues2 = {1.0, 2.0, 3.0};
        double[] yValues2 = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function2 = new ArrayTabulatedFunction(xValues2, yValues2);

        double[] xValues3 = {1.0, 2.0, 4.0};
        double[] yValues3 = {1.0, 4.0, 16.0};
        ArrayTabulatedFunction function3 = new ArrayTabulatedFunction(xValues3, yValues3);

        assertTrue(function1.equals(function2));
        assertFalse(function1.equals(function3));
        assertTrue(function1.equals(function1));
        assertEquals(function1.hashCode(), function2.hashCode());
    }

    @Test
    public void testApplyWithExactXValue() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(1.0, function.apply(1.0), 1e-10);
        assertEquals(4.0, function.apply(2.0), 1e-10);
        assertEquals(9.0, function.apply(3.0), 1e-10);
    }

    @Test
    public void testInterpolate() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(2.5, function.apply(1.5), 1e-10);
        assertEquals(6.5, function.apply(2.5), 1e-10);
    }

    @Test
    public void testExtrapolate() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(-2.0, function.apply(0.0), 1e-12);
        assertEquals(14.0, function.apply(4.0), 1e-12);
    }

    @Test
    public void testIteratorRemoveNotSupported() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        Iterator<Point> iterator = function.iterator();
        iterator.next();

        assertThrows(UnsupportedOperationException.class, () -> iterator.remove());
    }

    @Test
    public void testInterpolateDirectly() throws Exception {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        java.lang.reflect.Method method = ArrayTabulatedFunction.class.getDeclaredMethod(
                "interpolate", double.class, double.class, double.class, double.class, double.class);
        method.setAccessible(true);

        double result = (Double) method.invoke(function, 1.5, 1.0, 2.0, 1.0, 4.0);
        assertEquals(2.5, result, 1e-10);

        assertEquals(1.0, (Double) method.invoke(function, 1.0, 1.0, 2.0, 1.0, 4.0), 1e-10);
        assertEquals(4.0, (Double) method.invoke(function, 2.0, 1.0, 2.0, 1.0, 4.0), 1e-10);

        assertEquals(2.5, (Double) method.invoke(function, 1.0, 1.0, 1.0, 1.0, 4.0), 1e-10);
    }

    @Test
    public void testIteratorRemoveThrowsException() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        Iterator<Point> iterator = function.iterator();
        iterator.next();

        assertThrows(UnsupportedOperationException.class, () -> iterator.remove());

        assertTrue(iterator.hasNext());
        assertEquals(2.0, iterator.next().x, 1e-10);
    }

    @Test
    public void testLinkedListSerialVersionUID() throws Exception {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{4.0, 5.0, 6.0}
        );

        java.lang.reflect.Field field = LinkedListTabulatedFunction.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        long serialVersionUID = field.getLong(null);

        assertNotNull(serialVersionUID);
    }

    @Test
    public void testLinkedListSerialization() throws IOException, ClassNotFoundException {
        LinkedListTabulatedFunction original = new LinkedListTabulatedFunction(
                new double[]{0.0, 1.0, 2.0},
                new double[]{0.0, 1.0, 4.0}
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        LinkedListTabulatedFunction deserialized = (LinkedListTabulatedFunction) ois.readObject();
        ois.close();

        assertEquals(original.getCount(), deserialized.getCount());
        assertEquals(original, deserialized);
    }

    @Test
    public void testArrayTabulatedFunctionSerialization() throws IOException, ClassNotFoundException {
        ArrayTabulatedFunction original = new ArrayTabulatedFunction(
                new double[]{0.0, 1.0, 2.0, 3.0},
                new double[]{0.0, 1.0, 4.0, 9.0}
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ArrayTabulatedFunction deserialized = (ArrayTabulatedFunction) ois.readObject();
        ois.close();

        assertEquals(original.getCount(), deserialized.getCount());
        for (int i = 0; i < original.getCount(); i++) {
            assertEquals(original.getX(i), deserialized.getX(i), 1e-10);
            assertEquals(original.getY(i), deserialized.getY(i), 1e-10);
        }

        assertEquals(original, deserialized);
    }

    @Test
    public void testSerializationWithModifications() throws IOException, ClassNotFoundException {
        ArrayTabulatedFunction original = new ArrayTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{10.0, 20.0, 30.0}
        );

        original.setY(1, 25.0);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ArrayTabulatedFunction deserialized = (ArrayTabulatedFunction) ois.readObject();
        ois.close();

        assertEquals(25.0, deserialized.getY(1), 1e-10);
        assertEquals(original.getY(1), deserialized.getY(1), 1e-10);
    }

    @Test
    public void testSerializationConsistency() throws IOException, ClassNotFoundException {
        ArrayTabulatedFunction function1 = new ArrayTabulatedFunction(
                new double[]{0.0, 1.0, 2.0},
                new double[]{0.0, 1.0, 4.0}
        );

        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        ObjectOutputStream oos1 = new ObjectOutputStream(baos1);
        oos1.writeObject(function1);
        oos1.close();

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        ObjectOutputStream oos2 = new ObjectOutputStream(baos2);
        oos2.writeObject(function1);
        oos2.close();

        ArrayTabulatedFunction deserialized1 = deserializeFromBytes(baos1.toByteArray());
        ArrayTabulatedFunction deserialized2 = deserializeFromBytes(baos2.toByteArray());

        assertEquals(deserialized1, deserialized2);
        assertEquals(function1, deserialized1);
        assertEquals(function1, deserialized2);
    }

    @Test
    public void testSerializationWithDifferentData() throws IOException, ClassNotFoundException {
        double[][] testData = {
                {1.0, 2.0},
                {0.0, 1.0, 2.0, 3.0},
                {-2.0, -1.0, 0.0, 1.0, 2.0}
        };

        for (double[] xValues : testData) {
            double[] yValues = new double[xValues.length];
            for (int i = 0; i < xValues.length; i++) {
                yValues[i] = xValues[i] * xValues[i];
            }

            ArrayTabulatedFunction original = new ArrayTabulatedFunction(xValues, yValues);
            ArrayTabulatedFunction deserialized = serializeAndDeserialize(original);

            assertEquals(original.getCount(), deserialized.getCount());
            for (int i = 0; i < original.getCount(); i++) {
                assertEquals(original.getX(i), deserialized.getX(i), 1e-10);
                assertEquals(original.getY(i), deserialized.getY(i), 1e-10);
            }
        }
    }

    @Test
    public void testSerializationPreservesFunctionality() throws IOException, ClassNotFoundException {
        ArrayTabulatedFunction original = new ArrayTabulatedFunction(
                new double[]{0.0, 1.0, 2.0},
                new double[]{0.0, 1.0, 4.0}
        );

        ArrayTabulatedFunction deserialized = serializeAndDeserialize(original);

        assertEquals(0.0, deserialized.leftBound(), 1e-10);
        assertEquals(2.0, deserialized.rightBound(), 1e-10);
        assertEquals(3, deserialized.getCount());
        assertEquals(1.0, deserialized.getY(1), 1e-10);
        assertEquals(1, deserialized.indexOfX(1.0));
        assertEquals(2.5, deserialized.apply(1.5), 1e-10);
    }

    @Test
    public void testRemoveMethod() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.remove(1);
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(3.0, function.getX(1), 1e-10);
        assertEquals(4.0, function.getX(2), 1e-10);
        assertEquals(1.0, function.getY(0), 1e-10);
        assertEquals(9.0, function.getY(1), 1e-10);
        assertEquals(16.0, function.getY(2), 1e-10);

        function.remove(0);
        assertEquals(2, function.getCount());
        assertEquals(3.0, function.getX(0), 1e-10);
        assertEquals(4.0, function.getX(1), 1e-10);
    }

    @Test
    public void testRemoveInvalidIndex() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertThrows(IllegalArgumentException.class, () -> function.remove(-1));
        assertThrows(IllegalArgumentException.class, () -> function.remove(3));
    }

    @Test
    public void testRemoveWithMinimumPoints() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertThrows(IllegalStateException.class, () -> function.remove(0));
        assertThrows(IllegalStateException.class, () -> function.remove(1));
    }

    @Test
    public void testInsertUpdateExisting() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(2.0, 10.0);
        assertEquals(3, function.getCount());
        assertEquals(10.0, function.getY(1), 1e-10);
    }

    @Test
    public void testInsertAtBeginning() {
        double[] xValues = {2.0, 3.0, 4.0};
        double[] yValues = {4.0, 9.0, 16.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(1.0, 1.0);
        assertEquals(4, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(1.0, function.getY(0), 1e-10);
    }

    @Test
    public void testInsertAtEnd() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(4.0, 16.0);
        assertEquals(4, function.getCount());
        assertEquals(4.0, function.getX(3), 1e-10);
        assertEquals(16.0, function.getY(3), 1e-10);
    }

    @Test
    public void testInsertInMiddle() {
        double[] xValues = {1.0, 3.0, 4.0};
        double[] yValues = {1.0, 9.0, 16.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(2.0, 4.0);
        assertEquals(4, function.getCount());
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(4.0, function.getY(1), 1e-10);
    }

    @Test
    public void testFloorIndexOfXWithExactValues() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0, function.floorIndexOfX(1.0));
        assertEquals(1, function.floorIndexOfX(2.0));
        assertEquals(2, function.floorIndexOfX(3.0));
        assertEquals(2, function.floorIndexOfX(3.5));
        assertEquals(2, function.floorIndexOfX(4.0));
    }

    @Test
    public void testApplyWithExactXValuesInMiddle() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0.0, function.apply(0.0), 1e-12);
        assertEquals(1.0, function.apply(1.0), 1e-12);
        assertEquals(4.0, function.apply(2.0), 1e-12);
        assertEquals(9.0, function.apply(3.0), 1e-12);
    }

    @Test
    public void testInterpolateWithEqualXValues() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        try {
            java.lang.reflect.Method method = ArrayTabulatedFunction.class.getDeclaredMethod(
                    "interpolate", double.class, double.class, double.class, double.class, double.class);
            method.setAccessible(true);

            double result = (Double) method.invoke(function, 1.0, 1.0, 1.0, 1.0, 4.0);
            assertEquals(2.5, result, 1e-10);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    public void testInterpolateMethodWithInvalidFloorIndex() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertThrows(IllegalArgumentException.class, () -> function.interpolate(1.5, -1));
        assertThrows(IllegalArgumentException.class, () -> function.interpolate(1.5, 2));
    }

    @Test
    public void testInterpolateMethodWithOutOfBoundsX() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertThrows(exceptions.InterpolationException.class, () -> function.interpolate(0.5, 0));
        assertThrows(exceptions.InterpolationException.class, () -> function.interpolate(2.5, 0));
    }

    @Test
    public void testConstructorWithNullArrays() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};

        assertThrows(NullPointerException.class, () -> new ArrayTabulatedFunction(null, yValues));
        assertThrows(NullPointerException.class, () -> new ArrayTabulatedFunction(xValues, null));
    }

    @Test
    public void testConstructorWithNullFunction() {
        assertThrows(NullPointerException.class, () -> new ArrayTabulatedFunction(null, 0, 1, 2));
    }

    @Test
    public void testConstructorWithDifferentLengthArrays() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0};

        assertThrows(exceptions.DifferentLengthOfArraysException.class, () -> new ArrayTabulatedFunction(xValues, yValues));
    }

    @Test
    public void testFloorIndexOfXWithSinglePointEdgeCase() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0, function.floorIndexOfX(0.9));
        assertEquals(0, function.floorIndexOfX(1.0));
        assertEquals(0, function.floorIndexOfX(1.5));
        assertEquals(0, function.floorIndexOfX(2.0));
    }

    @Test
    public void testApplyWithExactMatchInMiddle() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(1.0, function.apply(1.0), 1e-12);
        assertEquals(4.0, function.apply(2.0), 1e-12);
    }

    @Test
    public void testIteratorMultipleElements() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        Iterator<Point> iterator = function.iterator();
        assertTrue(iterator.hasNext());
        Point point1 = iterator.next();
        assertEquals(1.0, point1.x, 1e-10);
        assertEquals(1.0, point1.y, 1e-10);

        assertTrue(iterator.hasNext());
        Point point2 = iterator.next();
        assertEquals(2.0, point2.x, 1e-10);
        assertEquals(4.0, point2.y, 1e-10);

        assertTrue(iterator.hasNext());
        Point point3 = iterator.next();
        assertEquals(3.0, point3.x, 1e-10);
        assertEquals(9.0, point3.y, 1e-10);

        assertFalse(iterator.hasNext());
    }

    @Test
    public void testIteratorNoSuchElement() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        Iterator<Point> iterator = function.iterator();
        iterator.next();
        iterator.next();
        assertThrows(java.util.NoSuchElementException.class, iterator::next);
    }

    private ArrayTabulatedFunction serializeAndDeserialize(ArrayTabulatedFunction original)
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (ArrayTabulatedFunction) ois.readObject();
    }

    private ArrayTabulatedFunction deserializeFromBytes(byte[] bytes)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (ArrayTabulatedFunction) ois.readObject();
    }


}