package functions;

import exceptions.InconsistentFunctionsException;
import org.junit.jupiter.api.Test;
import operations.TabulatedFunctionOperationService;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;

import static org.junit.jupiter.api.Assertions.*;

public class TabulatedFunctionOperationServiceTest {

    @Test
    public void testAsPoints() {
        double[] x = {0.0, 1.0, 2.0};
        double[] y = {0.0, 1.0, 4.0};
        TabulatedFunction func = new ArrayTabulatedFunction(x, y);
        Point[] points = TabulatedFunctionOperationService.asPoints(func);

        assertEquals(3, points.length);
        for (int i = 0; i < points.length; i++) {
            assertEquals(x[i], points[i].x, 1e-10);
            assertEquals(y[i], points[i].y, 1e-10);
        }
    }

    @Test
    public void testAddSameFactory() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(new ArrayTabulatedFunctionFactory());

        TabulatedFunction a = new ArrayTabulatedFunction(new double[]{0, 1, 2}, new double[]{0, 1, 4});
        TabulatedFunction b = new ArrayTabulatedFunction(new double[]{0, 1, 2}, new double[]{1, 2, 3});
        TabulatedFunction c = service.add(a, b);

        assertEquals(3, c.getCount());
        assertEquals(1, c.getY(0), 1e-10);
        assertEquals(3, c.getY(1), 1e-10);
        assertEquals(7, c.getY(2), 1e-10);
        assertTrue(c instanceof ArrayTabulatedFunction);
    }

    @Test
    public void testSubtractDifferentFactories() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(new LinkedListTabulatedFunctionFactory());

        TabulatedFunction a = new ArrayTabulatedFunction(new double[]{0, 1, 2}, new double[]{0, 1, 4});
        TabulatedFunction b = new LinkedListTabulatedFunction(new double[]{0, 1, 2}, new double[]{1, 2, 3});
        TabulatedFunction c = service.subtract(a, b);

        assertEquals(3, c.getCount());
        assertEquals(-1, c.getY(0), 1e-10);
        assertEquals(-1, c.getY(1), 1e-10);
        assertEquals(1, c.getY(2), 1e-10);
        assertTrue(c instanceof LinkedListTabulatedFunction);
    }

    @Test
    public void testInconsistentFunctionsExceptionOnAdd() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(new ArrayTabulatedFunctionFactory());

        TabulatedFunction a = new ArrayTabulatedFunction(new double[]{0, 1}, new double[]{0, 1});
        TabulatedFunction b = new ArrayTabulatedFunction(new double[]{0}, new double[]{1});

        assertThrows(InconsistentFunctionsException.class, () -> service.add(a, b));
    }

    @Test
    public void testInconsistentFunctionsExceptionOnMismatchX() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(new ArrayTabulatedFunctionFactory());

        TabulatedFunction a = new ArrayTabulatedFunction(new double[]{0, 1}, new double[]{0, 1});
        TabulatedFunction b = new ArrayTabulatedFunction(new double[]{0, 2}, new double[]{0, 1});

        assertThrows(InconsistentFunctionsException.class, () -> service.add(a, b));
    }

    @Test
    public void testDefaultConstructor() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        assertTrue(service.getFactory() instanceof ArrayTabulatedFunctionFactory);
    }

    @Test
    public void testConstructorWithFactory() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(factory);
        assertEquals(factory, service.getFactory());
    }

    @Test
    public void testSetFactory() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        TabulatedFunctionFactory newFactory = new LinkedListTabulatedFunctionFactory();
        service.setFactory(newFactory);
        assertEquals(newFactory, service.getFactory());
    }
}