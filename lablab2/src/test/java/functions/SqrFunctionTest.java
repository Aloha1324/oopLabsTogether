package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SqrFunctionTest {

    @Test
    public void testApplyWithZero() {
        SqrFunction sqr = new SqrFunction();
        double result = sqr.apply(0.0);
        assertEquals(0.0, result, 1e-10, "Ошибка, квадрат нуля должен быть ноль");
    }

    @Test
    public void testApplyWithPositiveNumber() {
        SqrFunction sqr = new SqrFunction();
        double result = sqr.apply(5.5);
        assertEquals(30.25, result, 1e-10, "Ошибка, квадрат положительного числа вычислен неверно");
    }

    @Test
    public void testApplyWithNegativeNumber() {
        SqrFunction sqr = new SqrFunction();
        double result = sqr.apply(-3.2);
        assertEquals(10.24, result, 1e-10, "Ошибка, квадрат отрицательного числа должен быть положительным");
    }

    @Test
    public void testApplyWithOne() {
        SqrFunction sqr = new SqrFunction();
        double result = sqr.apply(1.0);
        assertEquals(1.0, result, 1e-10, "Ошибка, квадрат единицы должен быть единица");
    }

    @Test
    public void testApplyWithFractionalNumber() {
        SqrFunction sqr = new SqrFunction();
        double result = sqr.apply(1.5);
        assertEquals(2.25, result, 1e-10, "Ошибка, квадрат дробного числа вычислен неверно");
    }

    @Test
    public void testApplyWithSpecialValues() {
        SqrFunction sqr = new SqrFunction();

        // Тест с NaN
        assertEquals(Double.NaN, sqr.apply(Double.NaN),
                "Квадрат NaN должен возвращать NaN");

        // Тест с бесконечностями
        assertEquals(Double.POSITIVE_INFINITY, sqr.apply(Double.POSITIVE_INFINITY),
                "Квадрат положительной бесконечности должен возвращать положительную бесконечность");
        assertEquals(Double.POSITIVE_INFINITY, sqr.apply(Double.NEGATIVE_INFINITY),
                "Квадрат отрицательной бесконечности должен возвращать положительную бесконечность");
    }

    @Test
    public void testApplyUsesMathPow() {
        SqrFunction sqr = new SqrFunction();
        double testValue = 3.7;
        double expected = Math.pow(testValue, 2);
        double actual = sqr.apply(testValue);
        assertEquals(expected, actual, 1e-12,
                "Метод apply должен использовать Math.pow для вычисления квадрата");
    }

    @Test
    public void testApplyWithLargeNumber() {
        SqrFunction sqr = new SqrFunction();
        double result = sqr.apply(1000.0);
        assertEquals(1000000.0, result, 1e-10,
                "Ошибка, квадрат большого числа вычислен неверно");
    }

    @Test
    public void testApplyWithSmallNumber() {
        SqrFunction sqr = new SqrFunction();
        double result = sqr.apply(0.001);
        assertEquals(0.000001, result, 1e-12,
                "Ошибка, квадрат малого числа вычислен неверно");
    }
}