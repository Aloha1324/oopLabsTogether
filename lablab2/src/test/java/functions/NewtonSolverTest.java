package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NewtonSolverTest {

    @Test
    void testApplyMethod() {
        // f(x) = x² - 4, корень x = 2
        MathFunction square = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * x - 4;
            }
        };

        MathFunction derivative = new MathFunction() {
            @Override
            public double apply(double x) {
                return 2 * x;
            }
        };

        NewtonSolver solver = new NewtonSolver(square, derivative);

        // Используем apply с начальным приближением 3.0
        double root = solver.apply(3.0);
        assertEquals(2.0, root, 1e-6);

        // Проверяем, что найденный корень обнуляет функцию
        assertEquals(0.0, square.apply(root), 1e-6);
    }

    @Test
    void testSolveWithDifferentInitialGuesses() {
        MathFunction square = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * x - 4;
            }
        };

        MathFunction derivative = new MathFunction() {
            @Override
            public double apply(double x) {
                return 2 * x;
            }
        };

        NewtonSolver solver = new NewtonSolver(square, derivative);

        // Тестируем с разными начальными приближениями
        assertEquals(2.0, solver.solve(3.0), 1e-6);
        assertEquals(2.0, solver.solve(5.0), 1e-6);
        assertEquals(-2.0, solver.solve(-3.0), 1e-6);
    }

    @Test
    void testLinearFunction() {
        // f(x) = 2x - 6, корень x = 3
        MathFunction linear = new MathFunction() {
            @Override
            public double apply(double x) {
                return 2 * x - 6;
            }
        };

        MathFunction derivative = new MathFunction() {
            @Override
            public double apply(double x) {
                return 2.0;
            }
        };

        NewtonSolver solver = new NewtonSolver(linear, derivative);

        double root = solver.apply(1.0);
        assertEquals(3.0, root, 1e-6);
    }

    @Test
    void testZeroDerivative() {
        MathFunction constant = new MathFunction() {
            @Override
            public double apply(double x) {
                return 5.0; // Константа
            }
        };

        MathFunction zeroDerivative = new MathFunction() {
            @Override
            public double apply(double x) {
                return 0.0; // Нулевая производная
            }
        };

        NewtonSolver solver = new NewtonSolver(constant, zeroDerivative);

        assertThrows(IllegalArgumentException.class, () -> {
            solver.apply(1.0);
        });
    }

    @Test
    void testNullFunctions() {
        MathFunction validFunction = new MathFunction() {
            @Override
            public double apply(double x) {
                return x;
            }
        };

        assertThrows(IllegalArgumentException.class, () -> {
            new NewtonSolver(null, validFunction);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new NewtonSolver(validFunction, null);
        });
    }

    @Test
    void testGetters() {
        MathFunction function = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * x - 4;
            }
        };

        MathFunction derivative = new MathFunction() {
            @Override
            public double apply(double x) {
                return 2 * x;
            }
        };

        double tolerance = 1e-12;
        int maxIterations = 50;

        NewtonSolver solver = new NewtonSolver(function, derivative, tolerance, maxIterations);

        assertEquals(function, solver.getFunction());
        assertEquals(derivative, solver.getDerivative());
        assertEquals(tolerance, solver.getTolerance(), 1e-15);
        assertEquals(maxIterations, solver.getMaxIterations());
    }
}