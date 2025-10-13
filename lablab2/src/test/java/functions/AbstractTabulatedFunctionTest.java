package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AbstractTabulatedFunctionTest {

    @Test
    public void testToStringWithThreePoints() {
        // Создаем тестовую реализацию абстрактного класса
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

    // Тестовая реализация для тестирования
    private static class TestTabulatedFunction extends AbstractTabulatedFunction {
        private final double[] xValues;
        private final double[] yValues;

        public TestTabulatedFunction(double[] xValues, double[] yValues) {
            this.xValues = xValues;
            this.yValues = yValues;
        }

        @Override
        protected int floorIndexOfX(double x) {
            return 0;
        }

        @Override
        protected double extrapolateLeft(double x) {
            return 0;
        }

        @Override
        protected double extrapolateRight(double x) {
            return 0;
        }

        @Override
        protected double interpolate(double x, int floorIndex) {
            return 0;
        }

        @Override
        public int getCount() {
            return xValues.length;
        }

        @Override
        public double getX(int index) {
            return xValues[index];
        }

        @Override
        public double getY(int index) {
            return yValues[index];
        }

        @Override
        public void setY(int index, double value) {
            yValues[index] = value;
        }

        @Override
        public int indexOfX(double x) {
            for (int i = 0; i < xValues.length; i++) {
                if (xValues[i] == x) return i;
            }
            return -1;
        }

        @Override
        public int indexOfY(double y) {
            for (int i = 0; i < yValues.length; i++) {
                if (yValues[i] == y) return i;
            }
            return -1;
        }

        @Override
        public double leftBound() {
            return xValues.length > 0 ? xValues[0] : 0;
        }

        @Override
        public double rightBound() {
            return xValues.length > 0 ? xValues[xValues.length - 1] : 0;
        }
    }
}