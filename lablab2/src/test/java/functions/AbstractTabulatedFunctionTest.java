package functions;

import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;
import org.junit.jupiter.api.Test;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

public class AbstractTabulatedFunctionTest {

    @Test
    public void testToStringWithThreePoints() {
        AbstractTabulatedFunction function = new TestTabulatedFunction(
                new double[]{0.0, 0.5, 1.0},
                new double[]{0.0, 0.25, 1.0}
        );
        String expected = "TestTabulatedFunction size = 3\n[0.0; 0.0]\n[0.5; 0.25]\n[1.0; 1.0]";
        assertEquals(expected, function.toString());
    }

    @Test
    public void testToStringWithEmptyFunction() {
        AbstractTabulatedFunction function = new TestTabulatedFunction(
                new double[]{},
                new double[]{}
        );
        String expected = "TestTabulatedFunction size = 0";
        assertEquals(expected, function.toString());
    }

    @Test
    public void testToStringWithSinglePoint() {
        AbstractTabulatedFunction function = new TestTabulatedFunction(
                new double[]{2.5},
                new double[]{3.7}
        );
        String expected = "TestTabulatedFunction size = 1\n[2.5; 3.7]";
        assertEquals(expected, function.toString());
    }

    @Test
    public void testCheckLengthIsTheSame() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0, 6.0};
        assertDoesNotThrow(() -> AbstractTabulatedFunction.checkLengthIsTheSame(xValues, yValues));
    }

    @Test
    public void testCheckLengthIsTheSameThrowsException() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0};
        assertThrows(DifferentLengthOfArraysException.class,
                () -> AbstractTabulatedFunction.checkLengthIsTheSame(xValues, yValues));
    }

    @Test
    public void testCheckSorted() {
        double[] sortedValues = {1.0, 2.0, 3.0, 4.0};
        assertDoesNotThrow(() -> AbstractTabulatedFunction.checkSorted(sortedValues));
    }

    @Test
    public void testCheckSortedThrowsException() {
        double[] unsortedValues = {1.0, 3.0, 2.0, 4.0};
        assertThrows(ArrayIsNotSortedException.class,
                () -> AbstractTabulatedFunction.checkSorted(unsortedValues));
    }

    @Test
    public void testCheckSortedWithEqualValues() {
        double[] equalValues = {1.0, 2.0, 2.0, 3.0};
        assertThrows(ArrayIsNotSortedException.class,
                () -> AbstractTabulatedFunction.checkSorted(equalValues));
    }

    @Test
    public void testCheckSortedWithSingleElement() {
        double[] singleValue = {1.0};
        assertDoesNotThrow(() -> AbstractTabulatedFunction.checkSorted(singleValue));
    }

    @Test
    public void testCheckSortedWithTwoElements() {
        double[] sortedTwo = {1.0, 2.0};
        double[] unsortedTwo = {2.0, 1.0};
        assertDoesNotThrow(() -> AbstractTabulatedFunction.checkSorted(sortedTwo));
        assertThrows(ArrayIsNotSortedException.class,
                () -> AbstractTabulatedFunction.checkSorted(unsortedTwo));
    }

    @Test
    public void testInterpolate() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{0.0, 1.0},
                new double[]{0.0, 2.0}
        );
        double result = function.testInterpolate(0.5, 0.0, 1.0, 0.0, 2.0);
        assertEquals(1.0, result, 1e-10);
    }

    @Test
    public void testInterpolateWithEqualX() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{1.0, 1.0},
                new double[]{2.0, 4.0}
        );
        double result = function.testInterpolate(1.0, 1.0, 1.0, 2.0, 4.0);
        assertEquals(3.0, result, 1e-10);
    }

    @Test
    public void testInterpolateWithLeftGreaterThanRight() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{1.0, 2.0},
                new double[]{2.0, 4.0}
        );
        double result = function.testInterpolate(1.5, 2.0, 1.0, 4.0, 2.0);
        assertEquals(3.0, result, 1e-10);
    }

    @Test
    public void testApplyWithExactX() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{0.0, 1.0, 2.0},
                new double[]{0.0, 1.0, 4.0}
        );
        assertEquals(1.0, function.apply(1.0), 1e-10);
    }

    @Test
    public void testApplyWithInterpolation() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{0.0, 1.0, 2.0},
                new double[]{0.0, 1.0, 4.0}
        ) {
            protected double interpolate(double x, int floorIndex) {
                return getY(floorIndex) + (getY(floorIndex + 1) - getY(floorIndex)) *
                        (x - getX(floorIndex)) / (getX(floorIndex + 1) - getX(floorIndex));
            }
            protected int floorIndexOfX(double x) {
                if (x < getX(0)) return 0;
                if (x > getX(getCount() - 1)) return getCount() - 1;
                for (int i = 0; i < getCount() - 1; i++) {
                    if (x >= getX(i) && x < getX(i + 1)) return i;
                }
                return getCount() - 2;
            }
            protected double extrapolateLeft(double x) {
                return getY(0) + (getY(1) - getY(0)) * (x - getX(0)) / (getX(1) - getX(0));
            }
            protected double extrapolateRight(double x) {
                int last = getCount() - 1;
                return getY(last) + (getY(last) - getY(last - 1)) * (x - getX(last)) / (getX(last) - getX(last - 1));
            }
        };
        double result = function.apply(0.5);
        assertEquals(0.5, result, 1e-10);
    }

    @Test
    public void testApplyWithExtrapolation() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{0.0, 1.0, 2.0},
                new double[]{0.0, 1.0, 4.0}
        ) {
            protected double extrapolateLeft(double x) {
                return -1.0;
            }
            protected double extrapolateRight(double x) {
                return 5.0;
            }
            protected int floorIndexOfX(double x) {
                return 0;
            }
            protected double interpolate(double x, int floorIndex) {
                return 0.0;
            }
        };
        assertEquals(-1.0, function.apply(-1.0), 1e-10);
        assertEquals(5.0, function.apply(3.0), 1e-10);
    }

    @Test
    public void testApplyWithEmptyFunction() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{},
                new double[]{}
        );
        assertThrows(IllegalStateException.class, () -> function.apply(1.0));
    }

    @Test
    public void testApplyWithInvalidFloorIndex() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{1.0, 4.0, 9.0}
        ) {
            protected int floorIndexOfX(double x) {
                return -1;
            }
            protected double extrapolateLeft(double x) {
                return 0.0;
            }
            protected double extrapolateRight(double x) {
                return 0.0;
            }
            protected double interpolate(double x, int floorIndex) {
                return 0.0;
            }
        };
        assertThrows(IllegalStateException.class, () -> function.apply(1.5));
    }

    @Test
    public void testApplyWithFloorIndexGreaterThanCount() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{1.0, 4.0, 9.0}
        ) {
            protected int floorIndexOfX(double x) {
                return getCount();
            }
            protected double extrapolateLeft(double x) {
                return 0.0;
            }
            protected double extrapolateRight(double x) {
                return 0.0;
            }
            protected double interpolate(double x, int floorIndex) {
                return 0.0;
            }
        };
        assertThrows(IllegalStateException.class, () -> function.apply(1.5));
    }

    @Test
    public void testApplyWithFloorIndexEqualToCount() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{1.0, 2.0},
                new double[]{1.0, 4.0}
        ) {
            protected int floorIndexOfX(double x) {
                return 2;
            }
            protected double extrapolateLeft(double x) {
                return 0.0;
            }
            protected double extrapolateRight(double x) {
                return 0.0;
            }
            protected double interpolate(double x, int floorIndex) {
                return 0.0;
            }
        };
        assertThrows(IllegalStateException.class, () -> function.apply(1.5));
    }

    @Test
    public void testApplyWithExactBoundary() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{1.0, 4.0, 9.0}
        ) {
            protected int floorIndexOfX(double x) {
                if (x <= getX(0)) return 0;
                if (x >= getX(getCount() - 1)) return getCount() - 2;
                for (int i = 0; i < getCount() - 1; i++) {
                    if (x >= getX(i) && x < getX(i + 1)) return i;
                }
                return getCount() - 2;
            }
            protected double extrapolateLeft(double x) {
                return getY(0);
            }
            protected double extrapolateRight(double x) {
                return getY(getCount() - 1);
            }
            protected double interpolate(double x, int floorIndex) {
                return getY(floorIndex) + (getY(floorIndex + 1) - getY(floorIndex)) *
                        (x - getX(floorIndex)) / (getX(floorIndex + 1) - getX(floorIndex));
            }
        };
        assertEquals(1.0, function.apply(1.0), 1e-10);
        assertEquals(9.0, function.apply(3.0), 1e-10);
    }

    @Test
    public void testEquals() {
        TestTabulatedFunction function1 = new TestTabulatedFunction(
                new double[]{0.0, 1.0, 2.0},
                new double[]{0.0, 1.0, 4.0}
        );
        TestTabulatedFunction function2 = new TestTabulatedFunction(
                new double[]{0.0, 1.0, 2.0},
                new double[]{0.0, 1.0, 4.0}
        );
        TestTabulatedFunction function3 = new TestTabulatedFunction(
                new double[]{0.0, 1.0, 3.0},
                new double[]{0.0, 1.0, 4.0}
        );
        TestTabulatedFunction function4 = new TestTabulatedFunction(
                new double[]{0.0, 1.0, 2.0},
                new double[]{0.0, 1.0, 5.0}
        );
        TestTabulatedFunction function5 = new TestTabulatedFunction(
                new double[]{0.0, 1.0},
                new double[]{0.0, 1.0}
        );
        assertEquals(function1, function1);
        assertEquals(function1, function2);
        assertEquals(function2, function1);
        assertNotEquals(function1, function3);
        assertNotEquals(function1, function4);
        assertNotEquals(function1, function5);
        assertNotEquals(function1, null);
        assertNotEquals(function1, "not a function");
    }

    @Test
    public void testEqualsWithDifferentImplementation() {
        TestTabulatedFunction function1 = new TestTabulatedFunction(
                new double[]{0.0, 1.0, 2.0},
                new double[]{0.0, 1.0, 4.0}
        );
        AbstractTabulatedFunction function2 = new AbstractTabulatedFunction() {
            protected int floorIndexOfX(double x) { return 0; }
            protected double extrapolateLeft(double x) { return 0; }
            protected double extrapolateRight(double x) { return 0; }
            protected double interpolate(double x, int floorIndex) { return 0; }
            public int getCount() { return 3; }
            public double getX(int index) { return index; }
            public double getY(int index) { return index * index; }
            public void setY(int index, double value) {}
            public int indexOfX(double x) { return (int)x; }
            public int indexOfY(double y) { return (int)Math.sqrt(y); }
            public double leftBound() { return 0; }
            public double rightBound() { return 2; }
        };
        assertNotEquals(function1, function2);
    }

    @Test
    public void testHashCode() {
        TestTabulatedFunction function1 = new TestTabulatedFunction(
                new double[]{0.0, 1.0, 2.0},
                new double[]{0.0, 1.0, 4.0}
        );
        TestTabulatedFunction function2 = new TestTabulatedFunction(
                new double[]{0.0, 1.0, 2.0},
                new double[]{0.0, 1.0, 4.0}
        );
        TestTabulatedFunction function3 = new TestTabulatedFunction(
                new double[]{0.0, 1.0, 3.0},
                new double[]{0.0, 1.0, 4.0}
        );
        assertEquals(function1.hashCode(), function2.hashCode());
        assertNotEquals(function1.hashCode(), function3.hashCode());
        assertTrue(function1.hashCode() != 0);
        assertTrue(function1.hashCode() != 1);
        int hashCode1 = function1.hashCode();
        int hashCode2 = function1.hashCode();
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    public void testHashCodeWithEmptyFunction() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{},
                new double[]{}
        );
        assertEquals(1, function.hashCode());
    }

    @Test
    public void testHashCodeWithSinglePoint() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{1.0},
                new double[]{2.0}
        );
        int hashCode = function.hashCode();
        assertTrue(hashCode != 0);
        assertTrue(hashCode != 1);
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{0.0, 1.0},
                new double[]{2.0, 3.0}
        );
        Object cloned = function.clone();
        assertNotNull(cloned);
        assertTrue(cloned instanceof TestTabulatedFunction);
        TestTabulatedFunction clonedFunction = (TestTabulatedFunction) cloned;
        assertEquals(function.getCount(), clonedFunction.getCount());
        for (int i = 0; i < function.getCount(); i++) {
            assertEquals(function.getX(i), clonedFunction.getX(i), 1e-10);
            assertEquals(function.getY(i), clonedFunction.getY(i), 1e-10);
        }
    }

    @Test
    public void testIterator() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{0.0, 1.0, 2.0},
                new double[]{0.0, 1.0, 4.0}
        );
        Iterator<Point> iterator = function.iterator();
        assertTrue(iterator.hasNext());
        Point point1 = iterator.next();
        assertEquals(0.0, point1.x, 1e-10);
        assertEquals(0.0, point1.y, 1e-10);
        assertTrue(iterator.hasNext());
        Point point2 = iterator.next();
        assertEquals(1.0, point2.x, 1e-10);
        assertEquals(1.0, point2.y, 1e-10);
        assertTrue(iterator.hasNext());
        Point point3 = iterator.next();
        assertEquals(2.0, point3.x, 1e-10);
        assertEquals(4.0, point3.y, 1e-10);
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testIteratorWithEmptyFunction() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{},
                new double[]{}
        );
        Iterator<Point> iterator = function.iterator();
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testIteratorRemoveNotSupported() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{1.0, 2.0},
                new double[]{1.0, 4.0}
        );
        Iterator<Point> iterator = function.iterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, () -> iterator.remove());
    }

    @Test
    public void testIteratorForEachLoop() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{1.0, 4.0, 9.0}
        );
        int count = 0;
        for (Point point : function) {
            assertNotNull(point);
            assertEquals(function.getX(count), point.x, 1e-10);
            assertEquals(function.getY(count), point.y, 1e-10);
            count++;
        }
        assertEquals(function.getCount(), count);
    }

    @Test
    public void testPointConstructorAndFields() {
        Point point = new Point(2.5, 3.7);
        assertEquals(2.5, point.x, 1e-10);
        assertEquals(3.7, point.y, 1e-10);
    }

    @Test
    public void testPointToString() {
        Point point = new Point(1.5, 2.5);
        assertEquals("(1.5, 2.5)", point.toString());
        Point pointZero = new Point(0.0, 0.0);
        assertEquals("(0.0, 0.0)", pointZero.toString());
        Point pointNegative = new Point(-1.5, -2.5);
        assertEquals("(-1.5, -2.5)", pointNegative.toString());
    }

    @Test
    public void testPointEquals() {
        Point point1 = new Point(1.0, 2.0);
        Point point2 = new Point(1.0, 2.0);
        Point point3 = new Point(1.1, 2.0);
        Point point4 = new Point(1.0, 2.1);
        assertTrue(point1.equals(point1));
        assertTrue(point1.equals(point2));
        assertTrue(point2.equals(point1));
        assertFalse(point1.equals(point3));
        assertFalse(point1.equals(point4));
        assertFalse(point1.equals(null));
        assertFalse(point1.equals("string"));
    }

    @Test
    public void testPointEqualsWithPrecision() {
        Point point1 = new Point(1.0, 2.0);
        Point point2 = new Point(1.0, 2.0);
        Point point3 = new Point(1.0000000001, 2.0);
        Point point4 = new Point(1.1, 2.0);
        assertTrue(point1.equals(point2));
        assertFalse(point1.equals(point3));
        assertFalse(point1.equals(point4));
    }

    @Test
    public void testPointHashCode() {
        Point point1 = new Point(1.0, 2.0);
        Point point2 = new Point(1.0, 2.0);
        Point point3 = new Point(1.1, 2.0);
        int hashCode1 = point1.hashCode();
        int hashCode2 = point1.hashCode();
        assertEquals(hashCode1, hashCode2);
        assertEquals(point1.hashCode(), point2.hashCode());
        assertDoesNotThrow(() -> point3.hashCode());
    }

    @Test
    public void testPointHashCodeContract() {
        Point point1 = new Point(1.0, 2.0);
        Point point2 = new Point(1.0, 2.0);
        assertTrue(point1.equals(point2));
        assertEquals(point1.hashCode(), point2.hashCode());
    }

    @Test
    public void testPointSerialVersionUID() throws Exception {
        Point point = new Point(1.0, 2.0);
        java.lang.reflect.Field field = Point.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        long serialVersionUID = field.getLong(null);
        assertEquals(1L, serialVersionUID);
    }

    @Test
    public void testPointSerialization() throws IOException, ClassNotFoundException {
        Point original = new Point(1.5, 2.5);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Point deserialized = (Point) ois.readObject();
        ois.close();
        assertEquals(original.x, deserialized.x, 1e-10);
        assertEquals(original.y, deserialized.y, 1e-10);
        assertEquals(original, deserialized);
    }

    @Test
    public void testPointSerializationWithDifferentValues() throws IOException, ClassNotFoundException {
        Point[] testPoints = {
                new Point(0.0, 0.0),
                new Point(-1.0, -2.0),
                new Point(1.5, 3.7),
                new Point(Double.MAX_VALUE, Double.MIN_VALUE)
        };
        for (Point original : testPoints) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(original);
            oos.close();
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            Point deserialized = (Point) ois.readObject();
            ois.close();
            assertEquals(original.x, deserialized.x, 1e-10);
            assertEquals(original.y, deserialized.y, 1e-10);
            assertEquals(original, deserialized);
        }
    }

    @Test
    public void testPointPublicFieldsAccess() {
        Point point = new Point(1.0, 2.0);
        point.x = 3.0;
        point.y = 4.0;
        assertEquals(3.0, point.x, 1e-10);
        assertEquals(4.0, point.y, 1e-10);
    }

    @Test
    public void testPointInIterator() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{1.0, 4.0, 9.0}
        );
        Iterator<Point> iterator = function.iterator();
        Point point = iterator.next();
        assertEquals(1.0, point.x, 1e-10);
        assertEquals(1.0, point.y, 1e-10);
        assertEquals("(1.0, 1.0)", point.toString());
        Point samePoint = new Point(1.0, 1.0);
        assertTrue(point.equals(samePoint));
        assertEquals(point.hashCode(), samePoint.hashCode());
    }

    @Test
    public void testApplyWithAllBranches() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{1.0, 4.0, 9.0}
        ) {
            protected int floorIndexOfX(double x) {
                if (x < 1.0) return 0;
                if (x > 3.0) return 1;
                if (x == 2.0) return 1;
                return 0;
            }
            protected double extrapolateLeft(double x) {
                return -1.0;
            }
            protected double extrapolateRight(double x) {
                return 10.0;
            }
            protected double interpolate(double x, int floorIndex) {
                return 5.0;
            }
        };
        assertEquals(-1.0, function.apply(0.0), 1e-10);
        assertEquals(10.0, function.apply(4.0), 1e-10);
        assertEquals(4.0, function.apply(2.0), 1e-10);
        assertEquals(5.0, function.apply(1.5), 1e-10);
    }

    @Test
    public void testHashCodeConsistency() {
        TestTabulatedFunction function = new TestTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{1.0, 4.0, 9.0}
        );
        int hashCode1 = function.hashCode();
        int hashCode2 = function.hashCode();
        assertEquals(hashCode1, hashCode2);
        TestTabulatedFunction sameFunction = new TestTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{1.0, 4.0, 9.0}
        );
        assertEquals(hashCode1, sameFunction.hashCode());
    }

    private static class TestTabulatedFunction extends AbstractTabulatedFunction implements Cloneable {
        private final double[] xValues;
        private final double[] yValues;

        public TestTabulatedFunction(double[] xValues, double[] yValues) {
            this.xValues = xValues;
            this.yValues = yValues;
        }

        protected int floorIndexOfX(double x) {
            return 0;
        }

        protected double extrapolateLeft(double x) {
            return 0;
        }

        protected double extrapolateRight(double x) {
            return 0;
        }

        protected double interpolate(double x, int floorIndex) {
            return 0;
        }

        public int getCount() {
            return xValues.length;
        }

        public double getX(int index) {
            return xValues[index];
        }

        public double getY(int index) {
            return yValues[index];
        }

        public void setY(int index, double value) {
            yValues[index] = value;
        }

        public int indexOfX(double x) {
            for (int i = 0; i < xValues.length; i++) {
                if (Math.abs(xValues[i] - x) < 1e-10) return i;
            }
            return -1;
        }

        public int indexOfY(double y) {
            for (int i = 0; i < yValues.length; i++) {
                if (Math.abs(yValues[i] - y) < 1e-10) return i;
            }
            return -1;
        }

        public double leftBound() {
            return xValues.length > 0 ? xValues[0] : 0;
        }

        public double rightBound() {
            return xValues.length > 0 ? xValues[xValues.length - 1] : 0;
        }

        public double testInterpolate(double x, double leftX, double rightX, double leftY, double rightY) {
            return interpolate(x, leftX, rightX, leftY, rightY);
        }

        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }
}