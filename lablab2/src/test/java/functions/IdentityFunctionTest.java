package functions;

import org.junit.jupiter.api.Test; //импорт аннотации @Test из библиотеки JUnit5
import static org.junit.jupiter.api.Assertions.*; //Для использования методов класса Assertions без указания имени класса

public class IdentityFunctionTest {

    @Test
    public void testApplyWithZero() {
        IdentityFunction identity = new IdentityFunction();
        double result = identity.apply(0.0);
        assertEquals(0.0, result, 1e-10, "Ошибка, тожд. функция от нуля есть ноль");
    }

    @Test
    public void testApplyWithPositiveNumber() {
        IdentityFunction identity = new IdentityFunction();
        double result = identity.apply(5.5);
        assertEquals(5.5, result, 1e-10, "Ошибка, тождественная функция от '+' числа должна возвращать то же число");
    }

    @Test
    public void testApplyWithNegativeNumber() {
        IdentityFunction identity = new IdentityFunction();
        double result = identity.apply(-3.2);
        assertEquals(-3.2, result, 1e-10, "Ошибка, тождественная функция от '-' числа должна возвращать то же число");
    }


    @Test
    public void testApplyWithSpecialValues() {
        IdentityFunction identity = new IdentityFunction();

        //не число NaN
        assertEquals(Double.NaN, identity.apply(Double.NaN), "Тождественная функция от NaN должна возвращать NaN");

        //бесконечности
        assertEquals(Double.POSITIVE_INFINITY, identity.apply(Double.POSITIVE_INFINITY),
                "Тождественная функция от '+' бесконечности должна возвращать '+' бесконечность");
        assertEquals(Double.NEGATIVE_INFINITY, identity.apply(Double.NEGATIVE_INFINITY),
                "Тождественная функция от '-' бесконечности должна возвращать '-' бесконечностб=ь");
    }
}
