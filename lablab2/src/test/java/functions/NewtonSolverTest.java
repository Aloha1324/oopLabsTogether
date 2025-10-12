package functions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class NewtonSolverTest {

    @Test
    @DisplayName("Тестирование метода apply - основного метода интерфейса")
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
    @DisplayName("Тестирование метода solve с разными начальными приближениями")
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
    @DisplayName("Тестирование линейной функции")
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
    @DisplayName("Тестирование случая с нулевой производной")
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
    @DisplayName("Тестирование с null функциями в конструкторе")
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

        assertThrows(IllegalArgumentException.class, () -> {
            new NewtonSolver(null, null);
        });
    }

    @Test
    @DisplayName("Тестирование геттеров")
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

    @Test
    @DisplayName("Тестирование конструктора с параметрами по умолчанию")
    void testDefaultConstructor() {
        MathFunction function = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * x - 9; // f(x) = x² - 9, корень x = 3
            }
        };

        MathFunction derivative = new MathFunction() {
            @Override
            public double apply(double x) {
                return 2 * x;
            }
        };

        NewtonSolver solver = new NewtonSolver(function, derivative);

        // Проверяем, что конструктор по умолчанию работает корректно
        double root = solver.solve(4.0);
        assertEquals(3.0, root, 1e-6);

        // Проверяем значения по умолчанию через геттеры
        assertEquals(1e-10, solver.getTolerance(), 1e-15);
        assertEquals(100, solver.getMaxIterations());
    }

    @Test
    @DisplayName("Тестирование невалидных параметров в конструкторе")
    void testInvalidConstructorParameters() {
        MathFunction function = new MathFunction() {
            @Override
            public double apply(double x) {
                return x;
            }
        };

        MathFunction derivative = new MathFunction() {
            @Override
            public double apply(double x) {
                return 1.0;
            }
        };

        // Тестируем невалидную точность
        assertThrows(IllegalArgumentException.class, () -> {
            new NewtonSolver(function, derivative, 0.0, 100);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new NewtonSolver(function, derivative, -1.0, 100);
        });

        // Тестируем невалидное количество итераций
        assertThrows(IllegalArgumentException.class, () -> {
            new NewtonSolver(function, derivative, 1e-10, 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new NewtonSolver(function, derivative, 1e-10, -10);
        });
    }

    @Test
    @DisplayName("Тестирование расходимости метода при включенной строгой проверке")
    void testStrictConvergenceDivergence() {
        // Функция, которая заведомо расходится при методе Ньютона: f(x) = x^(1/3)
        // У нее вертикальная касательная в точке x=0
        MathFunction problematicFunction = new MathFunction() {
            @Override
            public double apply(double x) {
                return Math.cbrt(x); // x^(1/3)
            }
        };

        MathFunction derivative = new MathFunction() {
            @Override
            public double apply(double x) {
                return 1.0 / (3.0 * Math.cbrt(x * x)); // 1/(3*x^(2/3))
            }
        };

        NewtonSolver solver = new NewtonSolver(problematicFunction, derivative, 1e-10, 5);

        // Начальное приближение близко к точке, где метод расходится
        assertThrows(ArithmeticException.class, () -> {
            solver.solve(0.1, true); // requireStrictConvergence = true
        });
    }

    @Test
    @DisplayName("Тестирование превышения максимального количества итераций")
    void testMaxIterationsExceeded() {
        // Функция с очень медленной сходимостью
        MathFunction slowConvergingFunction = new MathFunction() {
            @Override
            public double apply(double x) {
                return Math.exp(x) - 2; // f(x) = e^x - 2, корень x = ln(2)
            }
        };

        MathFunction derivative = new MathFunction() {
            @Override
            public double apply(double x) {
                return Math.exp(x);
            }
        };

        // Создаем решатель с очень маленьким максимальным количеством итераций
        NewtonSolver solver = new NewtonSolver(slowConvergingFunction, derivative, 1e-15, 2);

        assertThrows(ArithmeticException.class, () -> {
            solver.solve(10.0); // Начальное приближение далеко от корня
        });
    }

    @Test
    @DisplayName("Тестирование строгой проверки сходимости при успешном решении")
    void testStrictConvergenceSuccess() {
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

        // Тестируем с включенной строгой проверкой сходимости
        double root = solver.solve(3.0, true);
        assertEquals(2.0, root, 1e-6);
    }

    @Test
    @DisplayName("Тестирование критерия остановки по значению функции")
    void testFunctionValueStoppingCriterion() {
        // Функция, которая сразу возвращает значение меньше tolerance
        MathFunction function = new MathFunction() {
            @Override
            public double apply(double x) {
                // Всегда возвращаем значение меньше tolerance
                return 1e-11; // < 1e-10
            }
        };

        MathFunction derivative = new MathFunction() {
            @Override
            public double apply(double x) {
                return 1.0; // произвольная производная
            }
        };

        NewtonSolver solver = new NewtonSolver(function, derivative, 1e-10, 100);

        // Метод должен сразу остановиться по критерию значения функции
        // так как |f(x)| = 1e-11 < 1e-10 = tolerance
        double root = solver.solve(5.0);

        // Проверяем, что метод завершился (возвращенное значение равно начальному приближению
        // минус f(x)/f'(x) = 5.0 - 1e-11/1.0 = 5.0 - 1e-11 ≈ 5.0)
        assertEquals(5.0 - 1e-11, root, 1e-6);
    }

    @Test
    @DisplayName("Комплексный тест с тригонометрической функцией")
    void testTrigonometricFunction() {
        // f(x) = sin(x), корень x = π
        MathFunction sinFunction = new MathFunction() {
            @Override
            public double apply(double x) {
                return Math.sin(x);
            }
        };

        MathFunction cosDerivative = new MathFunction() {
            @Override
            public double apply(double x) {
                return Math.cos(x);
            }
        };

        NewtonSolver solver = new NewtonSolver(sinFunction, cosDerivative, 1e-10, 100);

        double root = solver.solve(3.0);
        assertEquals(Math.PI, root, 1e-6);
    }
}