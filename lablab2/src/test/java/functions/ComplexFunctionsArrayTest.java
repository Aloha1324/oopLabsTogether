package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ComplexFunctionsArrayTest {

    @Test
    public void testArrayFunctionComposition() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);

        MathFunction square = x -> x * x;
        MathFunction composed = x -> arrayFunc.apply(square.apply(x));

        assertEquals(1.0, composed.apply(1.0), 1e-12,
                "Композиция f(g(1.0)) должна возвращать 1.0, где f - табулированная функция, g - квадрат");
        assertEquals(4.0, composed.apply(Math.sqrt(2)), 1e-10, // Увеличил допуск для интерполяции
                "Композиция f(g(√2)) должна возвращать приближенно 4.0 через интерполяцию");
    }

    @Test
    public void testMultipleArrayFunctionsSum() {
        double[] xValues1 = {0.0, 1.0, 2.0};
        double[] yValues1 = {1.0, 2.0, 3.0};
        ArrayTabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues1);

        double[] xValues2 = {0.0, 1.0, 2.0};
        double[] yValues2 = {2.0, 3.0, 4.0};
        ArrayTabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues2);

        MathFunction sum = x -> func1.apply(x) + func2.apply(x);

        assertEquals(3.0, sum.apply(0.0), 1e-12,
                "Сумма двух табулированных функций в точке 0.0 должна быть 3.0");
        assertEquals(5.0, sum.apply(1.0), 1e-12,
                "Сумма двух табулированных функций в точке 1.0 должна быть 5.0");
        assertEquals(7.0, sum.apply(2.0), 1e-12,
                "Сумма двух табулированных функций в точке 2.0 должна быть 7.0");
    }

    @Test
    public void testMultipleArrayFunctionsProduct() {
        double[] xValues1 = {0.0, 1.0, 2.0};
        double[] yValues1 = {1.0, 2.0, 3.0};
        ArrayTabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues1);

        double[] xValues2 = {0.0, 1.0, 2.0};
        double[] yValues2 = {2.0, 3.0, 4.0};
        ArrayTabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues2);

        MathFunction product = x -> func1.apply(x) * func2.apply(x);

        assertEquals(2.0, product.apply(0.0), 1e-12,
                "Произведение двух табулированных функций в точке 0.0 должно быть 2.0");
        assertEquals(6.0, product.apply(1.0), 1e-12,
                "Произведение двух табулированных функций в точке 1.0 должно быть 6.0");
        assertEquals(12.0, product.apply(2.0), 1e-12,
                "Произведение двух табулированных функций в точке 2.0 должно быть 12.0");
    }

    @Test
    public void testArrayFunctionWithMathFunctionComposition() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);

        MathFunction sin = Math::sin;
        MathFunction chain = x -> arrayFunc.apply(sin.apply(x));

        assertEquals(0.0, chain.apply(0.0), 1e-12,
                "Композиция табулированной функции и sin в точке 0.0 должна возвращать 0.0");
        assertEquals(1.0, chain.apply(Math.PI / 2), 1e-12,
                "Композиция табулированной функции и sin в точке π/2 должна возвращать 1.0");
    }

    @Test
    public void testArrayFunctionWithLinearTransformation() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {2.0, 3.0, 4.0};
        ArrayTabulatedFunction tabulatedFunc = new ArrayTabulatedFunction(xValues, yValues);

        MathFunction linear = x -> 2 * x + 1;
        MathFunction product = x -> tabulatedFunc.apply(x) * linear.apply(x);

        assertEquals(6.0, product.apply(1.0), 1e-12,
                "Произведение табулированной функции и линейной функции в точке 1.0 должно быть 6.0");
        assertEquals(15.0, product.apply(2.0), 1e-12,
                "Произведение табулированной функции и линейной функции в точке 2.0 должно быть 15.0");
        assertEquals(28.0, product.apply(3.0), 1e-12,
                "Произведение табулированной функции и линейной функции в точке 3.0 должно быть 28.0");
    }

    @Test
    public void testArrayFunctionDifference() {
        double[] xValues1 = {0.0, 1.0, 2.0};
        double[] yValues1 = {1.0, 2.0, 3.0};
        ArrayTabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues1);

        double[] xValues2 = {0.0, 1.0, 2.0};
        double[] yValues2 = {2.0, 4.0, 6.0};
        ArrayTabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues2);

        MathFunction difference = x -> func2.apply(x) - func1.apply(x);

        assertEquals(1.0, difference.apply(0.0), 1e-12,
                "Разность двух табулированных функций в точке 0.0 должна быть 1.0");
        assertEquals(2.0, difference.apply(1.0), 1e-12,
                "Разность двух табулированных функций в точке 1.0 должна быть 2.0");
        assertEquals(3.0, difference.apply(2.0), 1e-12,
                "Разность двух табулированных функций в точке 2.0 должна быть 3.0");
    }

    @Test
    public void testArrayFunctionRatio() {
        double[] xValues1 = {0.0, 1.0, 2.0};
        double[] yValues1 = {1.0, 2.0, 3.0};
        ArrayTabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues1);

        double[] xValues2 = {0.0, 1.0, 2.0};
        double[] yValues2 = {2.0, 4.0, 6.0};
        ArrayTabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues2);

        MathFunction ratio = x -> func2.apply(x) / func1.apply(x);

        assertEquals(2.0, ratio.apply(0.0), 1e-12,
                "Отношение двух табулированных функций в точке 0.0 должно быть 2.0");
        assertEquals(2.0, ratio.apply(1.0), 1e-12,
                "Отношение двух табулированных функций в точке 1.0 должно быть 2.0");
        assertEquals(2.0, ratio.apply(2.0), 1e-12,
                "Отношение двух табулированных функций в точке 2.0 должно быть 2.0");
    }

    @Test
    public void testArrayFunctionWithConstantFunction() {
        MathFunction constant = x -> 5.0;
        ArrayTabulatedFunction constTabulated = new ArrayTabulatedFunction(constant, -5, 5, 10);

        MathFunction doubleConst = x -> constTabulated.apply(x) + constTabulated.apply(x);

        assertEquals(10.0, doubleConst.apply(0.0), 1e-12,
                "Сумма табулированной константной функции с самой собой в любой точке должна быть 10.0");
        assertEquals(10.0, doubleConst.apply(-3.0), 1e-12,
                "Сумма табулированной константной функции с самой собой в точке -3.0 должна быть 10.0");
        assertEquals(10.0, doubleConst.apply(4.0), 1e-12,
                "Сумма табулированной константной функции с самой собой в точке 4.0 должна быть 10.0");
    }

    @Test
    public void testArrayFunctionInterpolationAccuracy() {
        MathFunction source = x -> x * x;

        ArrayTabulatedFunction coarseFunc = new ArrayTabulatedFunction(source, 0, 2, 3);
        ArrayTabulatedFunction fineFunc = new ArrayTabulatedFunction(source, 0, 2, 10);

        double testX = 1.5;
        double exactValue = source.apply(testX);
        double coarseValue = coarseFunc.apply(testX);
        double fineValue = fineFunc.apply(testX);

        double coarseError = Math.abs(coarseValue - exactValue);
        double fineError = Math.abs(fineValue - exactValue);

        // Более мягкое условие для численной стабильности
        assertTrue(fineError <= coarseError + 1e-10,
                "Функция с большим количеством точек дискретизации должна давать более точный результат интерполяции");
    }
}