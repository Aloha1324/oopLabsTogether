package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArrayTabulatedFunctionRemovableTest {
    private static final long serialVersionUID = 1L;

    @Test
    public void testRemoveFromBeginning() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0, 3.0, 4.0},
                new double[]{1.0, 4.0, 9.0, 16.0}
        );

        function.remove(0);

        assertEquals(3, function.getCount(), 1e-10);
        assertEquals(2.0, function.getX(0), 1e-10);
        assertEquals(3.0, function.getX(1), 1e-10);
        assertEquals(4.0, function.getX(2), 1e-10);
    }

    @Test
    public void testRemoveFromEnd() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0, 3.0, 4.0},
                new double[]{1.0, 4.0, 9.0, 16.0}
        );

        function.remove(3);

        assertEquals(3, function.getCount(), 1e-10);
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
    }

    @Test
    public void testRemoveFromMiddle() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0, 3.0, 4.0},
                new double[]{1.0, 4.0, 9.0, 16.0}
        );

        function.remove(1);

        assertEquals(3, function.getCount(), 1e-10);
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(3.0, function.getX(1), 1e-10);
        assertEquals(4.0, function.getX(2), 1e-10);
    }

    @Test
    public void testRemoveInvalidIndex() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0, 3.0},
                new double[]{1.0, 4.0, 9.0}
        );

        assertThrows(IllegalArgumentException.class, () -> function.remove(-1));
        assertThrows(IllegalArgumentException.class, () -> function.remove(3));
    }

    @Test
    public void testRemoveFromMinimumSizeArray() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0},
                new double[]{1.0, 4.0}
        );

        assertThrows(IllegalStateException.class, () -> function.remove(0));
        assertThrows(IllegalStateException.class, () -> function.remove(1));
    }

    @Test
    public void testRemoveUpdatesCount() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0, 3.0, 4.0},
                new double[]{1.0, 4.0, 9.0, 16.0}
        );

        assertEquals(4, function.getCount(), 1e-10);
        function.remove(1);
        assertEquals(3, function.getCount(), 1e-10);
        function.remove(0);
        assertEquals(2, function.getCount(), 1e-10);
        assertThrows(IllegalStateException.class, () -> function.remove(0));
    }

    @Test
    public void testRemoveMaintainsArrayStructure() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0, 3.0, 4.0, 5.0},
                new double[]{1.0, 4.0, 9.0, 16.0, 25.0}
        );

        function.remove(2);

        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(4.0, function.getX(2), 1e-10);
        assertEquals(5.0, function.getX(3), 1e-10);
        assertEquals(1.0, function.getY(0), 1e-10);
        assertEquals(4.0, function.getY(1), 1e-10);
        assertEquals(16.0, function.getY(2), 1e-10);
        assertEquals(25.0, function.getY(3), 1e-10);
    }

    @Test
    public void testRemoveMultipleElements() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0, 3.0, 4.0, 5.0},
                new double[]{1.0, 4.0, 9.0, 16.0, 25.0}
        );

        function.remove(1);
        assertEquals(4, function.getCount(), 1e-10);
        assertEquals(3.0, function.getX(1), 1e-10);
        function.remove(2);
        assertEquals(3, function.getCount(), 1e-10);
        assertEquals(5.0, function.getX(2), 1e-10);
    }

    @Test
    public void testRemoveBoundaryValues() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{0.0, 1.0, 2.0, 3.0},
                new double[]{10.0, 20.0, 30.0, 40.0}
        );

        function.remove(0);
        assertEquals(1.0, function.leftBound(), 1e-10);
        function.remove(function.getCount() - 1);
        assertEquals(2.0, function.rightBound(), 1e-10);
    }

    @Test
    public void testRemoveWithMathFunctionConstructor() {
        MathFunction sqr = new SqrFunction();
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                sqr, 0.0, 4.0, 5
        );

        assertEquals(5, function.getCount(), 1e-10);
        function.remove(2);
        assertEquals(4, function.getCount(), 1e-10);
        assertTrue(function.getCount() >= 2);
    }

    @Test
    public void testRemovePreservesDataConsistency() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0, 3.0, 4.0},
                new double[]{10.0, 20.0, 30.0, 40.0}
        );

        function.remove(1);
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(30.0, function.getY(1), 1e-10);
        assertEquals(40.0, function.getY(2), 1e-10);
    }

    @Test
    public void testConstructorWithInvalidArrays() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunctionRemovable(
                    new double[]{1.0, 2.0},
                    new double[]{1.0}
            );
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunctionRemovable(
                    new double[]{1.0},
                    new double[]{1.0}
            );
        });
    }

    @Test
    public void testConstructorWithMathFunctionInvalid() {
        MathFunction sqr = new SqrFunction();

        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunctionRemovable(sqr, 0.0, 2.0, 1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunctionRemovable(sqr, 2.0, 0.0, 3);
        });
    }

    @Test
    public void testAccessorMethods() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0, 3.0},
                new double[]{10.0, 20.0, 30.0}
        );

        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(20.0, function.getY(1), 1e-10);
        assertEquals(30.0, function.getY(2), 1e-10);
        function.setY(1, 25.0);
        assertEquals(25.0, function.getY(1), 1e-10);
    }

    @Test
    public void testAccessorMethodsInvalidIndex() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0},
                new double[]{10.0, 20.0}
        );

        assertThrows(IllegalArgumentException.class, () -> function.getX(-1));
        assertThrows(IllegalArgumentException.class, () -> function.getX(2));
        assertThrows(IllegalArgumentException.class, () -> function.getY(-1));
        assertThrows(IllegalArgumentException.class, () -> function.getY(2));
        assertThrows(IllegalArgumentException.class, () -> function.setY(-1, 15.0));
        assertThrows(IllegalArgumentException.class, () -> function.setY(2, 15.0));
    }

    @Test
    public void testBoundaryMethods() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{-1.0, 0.0, 1.0, 2.0},
                new double[]{1.0, 0.0, 1.0, 4.0}
        );

        assertEquals(-1.0, function.leftBound(), 1e-10);
        assertEquals(2.0, function.rightBound(), 1e-10);
        function.remove(0);
        assertEquals(0.0, function.leftBound(), 1e-10);
        function.remove(function.getCount() - 1);
        assertEquals(1.0, function.rightBound(), 1e-10);
    }

    @Test
    public void testToString() {
        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                new double[]{1.0, 2.0},
                new double[]{10.0, 20.0}
        );

        String result = function.toString();
        assertNotNull(result);
        assertTrue(result.contains("ArrayTabulatedFunctionRemovable"));
        assertTrue(result.contains("1.0") && result.contains("10.0"));
        assertTrue(result.contains("2.0") && result.contains("20.0"));
    }

    @Test
    public void testRemoveAllPossiblePositions() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        ArrayTabulatedFunctionRemovable func1 = new ArrayTabulatedFunctionRemovable(xValues, yValues);
        func1.remove(0);
        assertEquals(2, func1.getCount());
        assertEquals(2.0, func1.getX(0));
        assertEquals(3.0, func1.getX(1));

        ArrayTabulatedFunctionRemovable func2 = new ArrayTabulatedFunctionRemovable(xValues, yValues);
        func2.remove(2);
        assertEquals(2, func2.getCount());
        assertEquals(1.0, func2.getX(0));
        assertEquals(2.0, func2.getX(1));

        ArrayTabulatedFunctionRemovable func3 = new ArrayTabulatedFunctionRemovable(xValues, yValues);
        func3.remove(1);
        assertEquals(2, func3.getCount());
        assertEquals(1.0, func3.getX(0));
        assertEquals(3.0, func3.getX(1));
    }

    @Test
    public void testRemoveWithLargeArray() {
        int size = 10;
        double[] xValues = new double[size];
        double[] yValues = new double[size];

        for (int i = 0; i < size; i++) {
            xValues[i] = i;
            yValues[i] = i * i;
        }

        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(xValues, yValues);

        function.remove(5);
        assertEquals(size - 1, function.getCount());
        function.remove(2);
        assertEquals(size - 2, function.getCount());
        assertEquals(0.0, function.getX(0));
        assertEquals(1.0, function.getX(1));
        assertEquals(3.0, function.getX(2));
        assertEquals(4.0, function.getX(3));
        assertEquals(6.0, function.getX(4));
    }

    @Test
    public void testMathFunctionConstructorValues() {
        MathFunction linear = new MathFunction() {
            public double apply(double x) {
                return 2 * x + 1;
            }
        };

        ArrayTabulatedFunctionRemovable function = new ArrayTabulatedFunctionRemovable(
                linear, 0.0, 2.0, 3
        );

        assertEquals(3, function.getCount());
        assertEquals(0.0, function.getX(0), 1e-10);
        assertEquals(1.0, function.getX(1), 1e-10);
        assertEquals(2.0, function.getX(2), 1e-10);
        assertEquals(1.0, function.getY(0), 1e-10);
        assertEquals(3.0, function.getY(1), 1e-10);
        assertEquals(5.0, function.getY(2), 1e-10);
    }

}