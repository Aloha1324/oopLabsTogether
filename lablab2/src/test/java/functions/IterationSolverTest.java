package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class IterationSolverTest {

    @Test
    public void testSolveLinearEquation() {
        MathFunction phi = x -> 0.5 * x + 1;
        double result = IterationSolver.solve(phi, 0.0);
        assertEquals(2.0, result, 0.001, "Ошибка, метод простой итерации должен находить корень линейного уравнения");
    }

    @Test
    public void testSolveQuadraticEquation() {
        MathFunction phi = x -> 0.5 * (x + 4.0 / x);
        double result = IterationSolver.solve(phi, 3.0);
        assertEquals(2.0, result, 0.001, "Ошибка, метод простой итерации должен находить корень квадратного уравнения");
    }

    @Test
    public void testSolveTrigonometricEquation() {
        MathFunction phi = Math::cos;
        double result = IterationSolver.solve(phi, 0.5);
        double equationValue = Math.cos(result) - result;
        assertEquals(0.0, equationValue, 0.001, "Ошибка, найденный корень должен удовлетворять уравнению cos(x) - x = 0");
    }

    @Test
    @SuppressWarnings("unused")
    public void testSolveWithConstantFunction() {
        MathFunction constant = x -> 5.0;
        double result = IterationSolver.solve(constant, 0.0);
        assertEquals(5.0, result, 1e-10, "Ошибка, для постоянной функции метод должен сразу сходиться к значению константы");
    }

    @Test
    @SuppressWarnings("unused")
    public void testSolveWithUnitFunction() {
        MathFunction unit = x -> 1.0;
        double result = IterationSolver.solve(unit, 0.0);
        assertEquals(1.0, result, 1e-10, "Ошибка, для функции, возвращающей 1.0, метод должен сходиться к 1.0");
    }

    @Test
    @SuppressWarnings("unused")
    public void testSolveWithZeroFunction() {
        MathFunction zero = x -> 0.0;
        double result = IterationSolver.solve(zero, 5.0);
        assertEquals(0.0, result, 1e-10, "Ошибка, для функции, возвращающей 0.0, метод должен сходиться к 0.0");
    }

    @Test
    public void testSolveWithIdentityFunction() {
        MathFunction identity = x -> x;
        double result = IterationSolver.solve(identity, 5.0);
        assertEquals(5.0, result, 1e-10, "Ошибка, для тождественной функции метод должен сходиться к начальному приближению");
    }

    @Test
    public void testSolveWithDifferentInitialGuesses() {
        MathFunction phi = x -> 0.5 * x + 1;
        double result1 = IterationSolver.solve(phi, 0.0);
        double result2 = IterationSolver.solve(phi, 10.0);
        double result3 = IterationSolver.solve(phi, -5.0);

        assertEquals(2.0, result1, 0.001, "Ошибка, метод должен сходиться к правильному корню с начального приближения 0.0");
        assertEquals(2.0, result2, 0.001, "Ошибка, метод должен сходиться к правильному корню с начального приближения 10.0");
        assertEquals(2.0, result3, 0.001, "Ошибка, метод должен сходиться к правильному корню с начального приближения -5.0");
    }

    @Test
    public void testSolveWithHighPrecision() {
        MathFunction phi = x -> 0.5 * x + 1;
        double result = IterationSolver.solve(phi, 0.0, 1e-12, 1000);
        assertEquals(2.0, result, 1e-11, "Ошибка, метод должен обеспечивать высокую точность вычислений");
    }

    @Test
    public void testSolveDivergentCase() {
        MathFunction divergentPhi = x -> 2.0 * x;
        assertThrows(RuntimeException.class, () -> IterationSolver.solve(divergentPhi, 0.1, 1e-10, 50),
                "Ошибка, метод должен выбрасывать исключение для расходящегося процесса");
    }

    @Test
    @SuppressWarnings("unused")
    public void testSolveWithSpecialValues() {
        MathFunction constant = x -> 5.0;

        assertEquals(5.0, IterationSolver.solve(constant, Double.NaN), 1e-10,
                "Метод должен работать даже с начальным приближением NaN");

        assertEquals(5.0, IterationSolver.solve(constant, Double.POSITIVE_INFINITY), 1e-10,
                "Метод должен работать даже с начальным приближением положительной бесконечности");

        assertEquals(5.0, IterationSolver.solve(constant, Double.NEGATIVE_INFINITY), 1e-10,
                "Метод должен работать даже с начальным приближением отрицательной бесконечности");
    }

    @Test
    public void testSolveWithLargeNumber() {
        MathFunction phi = x -> 0.5 * x + 1;
        double result = IterationSolver.solve(phi, 1000.0);
        assertEquals(2.0, result, 0.001,
                "Ошибка, метод должен корректно работать с большими начальными приближениями");
    }

    @Test
    public void testSolveWithSmallNumber() {
        MathFunction phi = x -> 0.5 * x + 1;
        double result = IterationSolver.solve(phi, 0.001);
        assertEquals(2.0, result, 0.001,
                "Ошибка, метод должен корректно работать с малыми начальными приближениями");
    }
}