package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UnitFunctionTest {

    @Test
    public void testApplyWithZero() {
        UnitFunction unit = new UnitFunction();
        double result = unit.apply(0.0);
        assertEquals(1.0, result, 1e-10, "Ошибка, UnitFunction должна возвращать 1.0 для нуля");
    }

    @Test
    public void testApplyWithPositiveNumber() {
        UnitFunction unit = new UnitFunction();
        double result = unit.apply(5.5);
        assertEquals(1.0, result, 1e-10, "Ошибка, UnitFunction должна возвращать 1.0 для положительных чисел");
    }

    @Test
    public void testApplyWithNegativeNumber() {
        UnitFunction unit = new UnitFunction();
        double result = unit.apply(-3.2);
        assertEquals(1.0, result, 1e-10, "Ошибка, UnitFunction должна возвращать 1.0 для отрицательных чисел");
    }

    @Test
    public void testApplyWithOne() {
        UnitFunction unit = new UnitFunction();
        double result = unit.apply(1.0);
        assertEquals(1.0, result, 1e-10, "Ошибка, UnitFunction должна возвращать 1.0 для единицы");
    }

    @Test
    public void testApplyWithFractionalNumber() {
        UnitFunction unit = new UnitFunction();
        double result = unit.apply(1.5);
        assertEquals(1.0, result, 1e-10, "Ошибка, UnitFunction должна возвращать 1.0 для дробных чисел");
    }

    @Test
    public void testApplyWithSpecialValues() {
        UnitFunction unit = new UnitFunction();

        assertEquals(1.0, unit.apply(Double.NaN), 1e-10,
                "UnitFunction должна возвращать 1.0 для NaN");

        assertEquals(1.0, unit.apply(Double.POSITIVE_INFINITY), 1e-10,
                "UnitFunction должна возвращать 1.0 для положительной бесконечности");

        assertEquals(1.0, unit.apply(Double.NEGATIVE_INFINITY), 1e-10,
                "UnitFunction должна возвращать 1.0 для отрицательной бесконечности");
    }

    @Test
    public void testApplyWithLargeNumber() {
        UnitFunction unit = new UnitFunction();
        double result = unit.apply(1000.0);
        assertEquals(1.0, result, 1e-10,
                "Ошибка, UnitFunction должна возвращать 1.0 для больших чисел");
    }

    @Test
    public void testApplyWithSmallNumber() {
        UnitFunction unit = new UnitFunction();
        double result = unit.apply(0.001);
        assertEquals(1.0, result, 1e-10,
                "Ошибка, UnitFunction должна возвращать 1.0 для малых чисел");
    }

    @Test
    public void testInheritanceFromConstantFunction() {
        UnitFunction unit = new UnitFunction();
        assertInstanceOf(ConstantFunction.class, unit,
                "UnitFunction должна наследоваться от ConstantFunction");
    }
}