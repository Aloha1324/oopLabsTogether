package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AndThenTest {

    @Test
    public void testAndThenWithSqrAndConstant() {
        SqrFunction sqr = new SqrFunction();
        ConstantFunction constant = new ConstantFunction(2.0);
        MathFunction composite = sqr.andThen(constant);
        double result = composite.apply(3.0);
        assertEquals(2.0, result, 1e-10, "Ошибка, композиция sqr.andThen(constant) должна возвращать константу");
    }

    @Test
    public void testAndThenWithMultipleCompositions() {
        SqrFunction sqr = new SqrFunction();
        MathFunction composite = sqr.andThen(sqr).andThen(sqr);
        double result = composite.apply(2.0);
        assertEquals(256.0, result, 1e-10, "Ошибка, цепочка sqr.andThen(sqr).andThen(sqr) должна вычислять ((2²)²)² = 256");
    }

    @Test
    public void testAndThenWithZeroFunction() {
        SqrFunction sqr = new SqrFunction();
        ZeroFunction zero = new ZeroFunction();
        MathFunction composite = sqr.andThen(zero);
        double result = composite.apply(5.0);
        assertEquals(0.0, result, 1e-10, "Ошибка, композиция с ZeroFunction должна всегда возвращать 0");
    }

    @Test
    public void testAndThenWithUnitFunction() {
        SqrFunction sqr = new SqrFunction();
        UnitFunction unit = new UnitFunction();
        MathFunction composite = sqr.andThen(unit);
        double result = composite.apply(5.0);
        assertEquals(1.0, result, 1e-10, "Ошибка, композиция с UnitFunction должна всегда возвращать 1");
    }

    @Test
    public void testAndThenWithIdentityFunction() {
        SqrFunction sqr = new SqrFunction();
        IdentityFunction identity = new IdentityFunction();
        MathFunction composite = sqr.andThen(identity);
        double result = composite.apply(4.0);
        assertEquals(16.0, result, 1e-10, "Ошибка, композиция с IdentityFunction должна возвращать квадрат числа");
    }

    @Test
    public void testAndThenOrderOfExecution() {
        SqrFunction sqr = new SqrFunction();
        MathFunction triple = x -> 3 * x;
        MathFunction composite1 = sqr.andThen(triple);
        MathFunction composite2 = triple.andThen(sqr);

        double result1 = composite1.apply(2.0); // (2²) * 3 = 4 * 3 = 12
        double result2 = composite2.apply(2.0); // (3 * 2)² = 6² = 36

        assertEquals(12.0, result1, 1e-10, "Ошибка, порядок выполнения f.andThen(g) должен быть сначала f, потом g");
        assertEquals(36.0, result2, 1e-10, "Ошибка, порядок выполнения g.andThen(f) должен быть сначала g, потом f");
    }

    @Test
    public void testAndThenWithSpecialValues() {
        SqrFunction sqr = new SqrFunction();
        ConstantFunction constant = new ConstantFunction(5.0);
        MathFunction composite = sqr.andThen(constant);

        assertEquals(5.0, composite.apply(Double.NaN), 1e-10,
                "Композиция должна возвращать константу даже для NaN");

        assertEquals(5.0, composite.apply(Double.POSITIVE_INFINITY), 1e-10,
                "Композиция должна возвращать константу даже для положительной бесконечности");

        assertEquals(5.0, composite.apply(Double.NEGATIVE_INFINITY), 1e-10,
                "Композиция должна возвращать константу даже для отрицательной бесконечности");
    }

    @Test
    public void testAndThenWithLargeNumber() {
        SqrFunction sqr = new SqrFunction();
        MathFunction half = x -> x / 2;
        MathFunction composite = sqr.andThen(half);
        double result = composite.apply(1000.0);
        assertEquals(500000.0, result, 1e-10,
                "Ошибка, композиция должна корректно работать с большими числами");
    }

    @Test
    public void testAndThenWithSmallNumber() {
        SqrFunction sqr = new SqrFunction();
        MathFunction doubleFunc = x -> 2 * x;
        MathFunction composite = sqr.andThen(doubleFunc);
        double result = composite.apply(0.001);
        assertEquals(0.000002, result, 1e-12,
                "Ошибка, композиция должна корректно работать с малыми числами");
    }

    @Test
    public void testAndThenReturnsMathFunction() {
        SqrFunction sqr = new SqrFunction();
        ConstantFunction constant = new ConstantFunction(2.0);
        MathFunction composite = sqr.andThen(constant);

        assertInstanceOf(MathFunction.class, composite,
                "Метод andThen должен возвращать объект типа MathFunction");
    }
}