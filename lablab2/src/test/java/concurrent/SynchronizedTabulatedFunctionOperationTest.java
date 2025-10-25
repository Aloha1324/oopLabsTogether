package concurrent;

import functions.ArrayTabulatedFunction;
import functions.TabulatedFunction;
import org.junit.Test;

import static org.junit.Assert.*;

public class SynchronizedTabulatedFunctionOperationTest {

    @Test
    public void testDoSynchronouslyWithReturnValue() {
        double[] xValues = {1, 2, 3};
        double[] yValues = {10, 20, 30};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        Double result = syncFunc.doSynchronously(func -> {
            double sum = 0;
            for (int i = 0; i < func.getCount(); i++) {
                sum += func.getY(i);
            }
            return sum;
        });

        assertEquals(60.0, result, 0.0001);
    }

    @Test
    public void testDoSynchronouslyWithVoid() {
        double[] xValues = {1, 2, 3};
        double[] yValues = {10, 20, 30};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        Void result = syncFunc.doSynchronously(func -> {
            for (int i = 0; i < func.getCount(); i++) {
                func.setY(i, func.getY(i) * 2);
            }
            return null;
        });

        assertNull(result);
        assertEquals(20.0, syncFunc.getY(0), 0.0001);
        assertEquals(40.0, syncFunc.getY(1), 0.0001);
        assertEquals(60.0, syncFunc.getY(2), 0.0001);
    }

    @Test
    public void testDoSynchronouslyWithComplexOperation() {
        double[] xValues = {1, 2, 3, 4};
        double[] yValues = {1, 4, 9, 16};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        String result = syncFunc.doSynchronously(func -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Count: ").append(func.getCount())
                    .append(", Left: ").append(func.leftBound())
                    .append(", Right: ").append(func.rightBound())
                    .append(", Apply(2): ").append(func.apply(2.0));
            return sb.toString();
        });

        assertEquals("Count: 4, Left: 1.0, Right: 4.0, Apply(2): 4.0", result);
    }

    @Test
    public void testDoSynchronouslyWithIndexOperations() {
        double[] xValues = {1, 2, 3};
        double[] yValues = {10, 20, 30};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        Boolean result = syncFunc.doSynchronously(func -> {
            boolean hasX2 = func.indexOfX(2.0) != -1;
            boolean hasY50 = func.indexOfY(50.0) != -1;
            return hasX2 && !hasY50;
        });

        assertTrue(result);
    }

    @Test
    public void testDoSynchronouslyWithMultipleMethodCalls() {
        double[] xValues = {1, 2, 3};
        double[] yValues = {10, 20, 30};

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(arrayFunc);

        Integer result = syncFunc.doSynchronously(func -> {
            int modifiedCount = 0;
            for (int i = 0; i < func.getCount(); i++) {
                if (func.getY(i) > 15) {
                    func.setY(i, func.getY(i) + 5);
                    modifiedCount++;
                }
            }
            return modifiedCount;
        });

        assertEquals(2, result.intValue());
        assertEquals(10.0, syncFunc.getY(0), 0.0001);
        assertEquals(25.0, syncFunc.getY(1), 0.0001);
        assertEquals(35.0, syncFunc.getY(2), 0.0001);
    }
}