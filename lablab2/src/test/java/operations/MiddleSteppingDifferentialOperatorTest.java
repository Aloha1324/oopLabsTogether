package operations;

import functions.MathFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MiddleSteppingDifferentialOperatorTest {

    private static class SqrFunction implements MathFunction {
        public double apply(double x) {
            return x * x;
        }
    }

    @Test
    public void testMiddleDerivative() {
        MiddleSteppingDifferentialOperator operator = new MiddleSteppingDifferentialOperator(0.001);
        MathFunction sqr = new SqrFunction();
        MathFunction derived = operator.derive(sqr);

        double x = 4.0;
        double expected = 2 * x;
        assertEquals(expected, derived.apply(x), 1e-3);
    }

    @Test
    public void testInvalidStepThrows() {
        assertThrows(IllegalArgumentException.class, () -> new MiddleSteppingDifferentialOperator(0));
        assertThrows(IllegalArgumentException.class, () -> new MiddleSteppingDifferentialOperator(-0.1));
        assertThrows(IllegalArgumentException.class, () -> new MiddleSteppingDifferentialOperator(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new MiddleSteppingDifferentialOperator(Double.POSITIVE_INFINITY));
    }
}
