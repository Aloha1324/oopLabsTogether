package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConstantFunctionTest {

    @Test
    public void testApplyWithPositiveNumber() {
        ConstantFunction constant = new ConstantFunction(5.0);
        double result = constant.apply(10.0);
        assertEquals(5.0, result, 1e-10, "Ошибка, константная функция должна всегда возвращать заданное значение");
    }

    @Test
    public void testApplyWithNegativeNumber() {
        ConstantFunction constant = new ConstantFunction(5.0);
        double result = constant.apply(-5.0);
        assertEquals(5.0, result, 1e-10, "Ошибка, константная функция должна всегда возвращать заданное значение независимо от знака аргумента");
    }

    @Test
    public void testApplyWithZero() {
        ConstantFunction constant = new ConstantFunction(5.0);
        double result = constant.apply(0.0);
        assertEquals(5.0, result, 1e-10, "Ошибка, константная функция должна возвращать заданное значение даже при нулевом аргументе");
    }

    @Test
    public void testApplyWithDifferentConstants() {
        ConstantFunction constant1 = new ConstantFunction(3.14);
        ConstantFunction constant2 = new ConstantFunction(-2.5);
        ConstantFunction constant3 = new ConstantFunction(0.0);

        assertEquals(3.14, constant1.apply(100.0), 1e-10, "Ошибка, константная функция должна возвращать заданное значение 3.14");
        assertEquals(-2.5, constant2.apply(-50.0), 1e-10, "Ошибка, константная функция должна возвращать заданное значение -2.5");
        assertEquals(0.0, constant3.apply(0.0), 1e-10, "Ошибка, константная функция должна возвращать заданное значение 0.0");
    }

    @Test
    public void testApplyWithSpecialValues() {
        ConstantFunction constant = new ConstantFunction(7.5);

        // Тест с NaN
        assertEquals(7.5, constant.apply(Double.NaN), 1e-10,
                "Константная функция должна возвращать заданное значение даже при аргументе NaN");

        // Тест с бесконечностями
        assertEquals(7.5, constant.apply(Double.POSITIVE_INFINITY), 1e-10,
                "Константная функция должна возвращать заданное значение даже при положительной бесконечности");
        assertEquals(7.5, constant.apply(Double.NEGATIVE_INFINITY), 1e-10,
                "Константная функция должна возвращать заданное значение даже при отрицательной бесконечности");
    }

    @Test
    public void testApplyWithLargeNumber() {
        ConstantFunction constant = new ConstantFunction(2.0);
        double result = constant.apply(1e100);
        assertEquals(2.0, result, 1e-10,
                "Ошибка, константная функция должна возвращать заданное значение даже для очень больших чисел");
    }

    @Test
    public void testApplyWithSmallNumber() {
        ConstantFunction constant = new ConstantFunction(2.0);
        double result = constant.apply(1e-100);
        assertEquals(2.0, result, 1e-10,
                "Ошибка, константная функция должна возвращать заданное значение даже для очень малых чисел");
    }

    @Test
    public void testApplyConsistency() {
        ConstantFunction constant = new ConstantFunction(4.2);

        // Проверка согласованности для разных аргументов
        double result1 = constant.apply(1.0);
        double result2 = constant.apply(2.0);
        double result3 = constant.apply(3.0);

        assertEquals(4.2, result1, 1e-10, "Константная функция должна возвращать одинаковое значение для разных аргументов");
        assertEquals(4.2, result2, 1e-10, "Константная функция должна возвращать одинаковое значение для разных аргументов");
        assertEquals(4.2, result3, 1e-10, "Константная функция должна возвращать одинаковое значение для разных аргументов");
        assertEquals(result1, result2, 1e-10, "Все результаты должны быть одинаковыми");
        assertEquals(result2, result3, 1e-10, "Все результаты должны быть одинаковыми");
    }
}