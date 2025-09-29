package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ZeroFunctionTest {

    @Test
    public void testApplyWithZero() {
        ZeroFunction zero = new ZeroFunction();
        double result = zero.apply(0.0);
        assertEquals(0.0, result, 1e-10, "Ошибка, ZeroFunction должна возвращать 0.0 для нуля");
    }

    @Test
    public void testApplyWithPositiveNumber() {
        ZeroFunction zero = new ZeroFunction();
        double result = zero.apply(5.5);
        assertEquals(0.0, result, 1e-10, "Ошибка, ZeroFunction должна возвращать 0.0 для положительных чисел");
    }

    @Test
    public void testApplyWithNegativeNumber() {
        ZeroFunction zero = new ZeroFunction();
        double result = zero.apply(-3.2);
        assertEquals(0.0, result, 1e-10, "Ошибка, ZeroFunction должна возвращать 0.0 для отрицательных чисел");
    }

    @Test
    public void testApplyWithOne() {
        ZeroFunction zero = new ZeroFunction();
        double result = zero.apply(1.0);
        assertEquals(0.0, result, 1e-10, "Ошибка, ZeroFunction должна возвращать 0.0 для единицы");
    }

    @Test
    public void testApplyWithFractionalNumber() {
        ZeroFunction zero = new ZeroFunction();
        double result = zero.apply(1.5);
        assertEquals(0.0, result, 1e-10, "Ошибка, ZeroFunction должна возвращать 0.0 для дробных чисел");
    }

    @Test
    public void testApplyWithSpecialValues() {
        ZeroFunction zero = new ZeroFunction();

        assertEquals(0.0, zero.apply(Double.NaN), 1e-10,
                "ZeroFunction должна возвращать 0.0 для NaN");

        assertEquals(0.0, zero.apply(Double.POSITIVE_INFINITY), 1e-10,
                "ZeroFunction должна возвращать 0.0 для положительной бесконечности");

        assertEquals(0.0, zero.apply(Double.NEGATIVE_INFINITY), 1e-10,
                "ZeroFunction должна возвращать 0.0 для отрицательной бесконечности");
    }

    @Test
    public void testApplyWithLargeNumber() {
        ZeroFunction zero = new ZeroFunction();
        double result = zero.apply(1000.0);
        assertEquals(0.0, result, 1e-10,
                "Ошибка, ZeroFunction должна возвращать 0.0 для больших чисел");
    }

    @Test
    public void testApplyWithSmallNumber() {
        ZeroFunction zero = new ZeroFunction();
        double result = zero.apply(0.001);
        assertEquals(0.0, result, 1e-10,
                "Ошибка, ZeroFunction должна возвращать 0.0 для малых чисел");
    }

    @Test
    public void testInheritanceFromConstantFunction() {
        ZeroFunction zero = new ZeroFunction();
        assertInstanceOf(ConstantFunction.class, zero,
                "ZeroFunction должна наследоваться от ConstantFunction");
    }
}