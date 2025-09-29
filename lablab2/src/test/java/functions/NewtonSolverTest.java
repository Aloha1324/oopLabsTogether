package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NewtonSolverTest {

    // Тест нахождения положительного корня (x = 2)
    @Test
    public void testSolvePositiveRoot() {

        MathFunction quadraticFunction = x -> x * x - 4;
        MathFunction quadraticDerivative = x -> 2 * x;
        NewtonSolver solver = new NewtonSolver(quadraticFunction, quadraticDerivative);
        // Задаем начальное приближение близко к положительному корню
        double initialGuess = 3.0;
        double result = solver.solve(initialGuess);
        // Ожидаемый результат - корень 2.0
        double expected = 2.0;
        // Проверяем, что результат близок к ожидаемому с точностью 1e-8
        assertEquals(expected, result, 1e-8);
    }

    // Тест нахождения отрицательного корня (x = -2)
    @Test
    public void testSolveNegativeRoot() {

        MathFunction quadraticFunction = x -> x * x - 4;
        MathFunction quadraticDerivative = x -> 2 * x;
        NewtonSolver solver = new NewtonSolver(quadraticFunction, quadraticDerivative);
        // Задаем начальное приближение близко к отрицательному корню
        double initialGuess = -3.0;
        double result = solver.solve(initialGuess);
        // Ожидаемый результат - корень -2.0
        double expected = -2.0;
        // Проверяем, что результат близок к ожидаемому с точностью 1e-8
        assertEquals(expected, result, 1e-8);
    }

    // Тест обработки нулевой производной
    @Test
    public void testZeroDerivative() {

        MathFunction constantFunction = x -> x * x * x; // f(x) = x^3
        MathFunction zeroDerivative = x -> 3 * x * x; // f'(x) = 3x^2

        NewtonSolver problemSolver = new NewtonSolver(constantFunction, zeroDerivative);
        // Пытаемся решить с начальным приближением 0 (должно вызвать исключение)
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            problemSolver.solve(0.0);
        });
        // Проверяем, что сообщение исключения содержит нужный текст
        assertTrue(exception.getMessage().contains("Производная близка к нулю"));
    }

    // Тест расходимости метода
    @Test
    public void testNonConvergence() {
        // Создаем сложную функцию, которая может не сходиться
        MathFunction oscillatingFunction = x -> Math.sin(x) + 1; // Не имеет вещественных корней
        // Создаем производную
        MathFunction oscillatingDerivative = x -> Math.cos(x);
        // Создаем решатель с очень малым числом итераций
        NewtonSolver nonConvergentSolver = new NewtonSolver(
                oscillatingFunction, oscillatingDerivative, 1e-10, 5
        );
        // Пытаемся решить (должно вызвать исключение о не сходимости)
        Exception exception = assertThrows(ArithmeticException.class, () -> {
            nonConvergentSolver.solve(1.0);
        });
        // Проверяем, что сообщение исключения содержит нужный текст
        assertTrue(exception.getMessage().contains("не сошелся"));
    }

    // Тест строгой проверки сходимости
    @Test
    public void testStrictConvergenceCheck() {
        MathFunction divergingFunction = x -> x * x * x - 2 * x + 2;
        MathFunction divergingDerivative = x -> 3 * x * x - 2;
        NewtonSolver strictSolver = new NewtonSolver(divergingFunction, divergingDerivative);
        // Запускаем с включенной строгой проверкой сходимости (должно вызвать исключение)
        Exception exception = assertThrows(ArithmeticException.class, () -> {
            strictSolver.solve(0.0, true);
        });
        // Проверяем, что сообщение исключения содержит нужный текст
        assertTrue(exception.getMessage().contains("расходится"));
    }

    // Тест работы с пользовательскими параметрами
    @Test
    public void testCustomParameters() {
        MathFunction quadraticFunction = x -> x * x - 4;
        MathFunction quadraticDerivative = x -> 2 * x;
        NewtonSolver customSolver = new NewtonSolver(
                quadraticFunction, quadraticDerivative, 1e-12, 50
        );
        // Проверяем, что параметры установились правильно
        assertEquals(1e-12, customSolver.getTolerance(), 1e-15);
        assertEquals(50, customSolver.getMaxIterations());
        // Проверяем, что решатель работает с этими параметрами
        double result = customSolver.solve(3.0);
        assertEquals(2.0, result, 1e-10);
    }

    // Тест получения функций
    @Test
    public void testGetters() {
        // Создаем функцию и производную для теста
        MathFunction function = x -> x * x - 4;
        MathFunction derivative = x -> 2 * x;
        // Создаем решатель
        NewtonSolver solver = new NewtonSolver(function, derivative);
        // Проверяем геттер функции
        assertSame(function, solver.getFunction());
        // Проверяем геттер производной
        assertSame(derivative, solver.getDerivative());
        // Проверяем геттер точности (значение по умолчанию)
        assertEquals(1e-10, solver.getTolerance(), 1e-15);
        // Проверяем геттер максимального числа итераций (значение по умолчанию)
        assertEquals(100, solver.getMaxIterations());
    }

    // Тест обработки невалидных параметров конструктора
    @Test
    public void testNullFunction() {
        MathFunction derivative = x -> 2 * x;
        // Пытаемся создать решатель с null функцией (должно вызвать исключение)
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new NewtonSolver(null, derivative);
        });
        // Проверяем, что сообщение исключения содержит нужный текст
        assertTrue(exception.getMessage().contains("не могут быть null"));
    }

    // Тест обработки невалидной производной
    @Test
    public void testNullDerivative() {
        MathFunction function = x -> x * x - 4;
        // Пытаемся создать решатель с null производной (должно вызвать исключение)
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new NewtonSolver(function, null);
        });
        // Проверяем, что сообщение исключения содержит нужный текст
        assertTrue(exception.getMessage().contains("не могут быть null"));
    }

    // Тест обработки невалидной точности
    @Test
    public void testInvalidTolerance() {
        // Создаем функцию и производную для теста
        MathFunction function = x -> x * x - 4;
        MathFunction derivative = x -> 2 * x;
        // Пытаемся создать решатель с отрицательной точностью
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new NewtonSolver(function, derivative, -1.0, 100);
        });
        // Проверяем, что сообщение исключения содержит нужный текст
        assertTrue(exception.getMessage().contains("положительными"));
    }

    // Тест обработки невалидного числа итераций
    @Test
    public void testInvalidMaxIterations() {
        // Создаем функцию и производную для теста
        MathFunction function = x -> x * x - 4;
        MathFunction derivative = x -> 2 * x;
        // Пытаемся создать решатель с нулевым числом итераций
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new NewtonSolver(function, derivative, 1e-10, 0);
        });
        // Проверяем, что сообщение исключения содержит нужный текст
        assertTrue(exception.getMessage().contains("положительными"));
    }

    // Тест линейной функции
    @Test
    public void testLinearFunction() {
        MathFunction linearFunction = x -> 2 * x - 6;
        MathFunction constantDerivative = x -> 2;
        NewtonSolver linearSolver = new NewtonSolver(linearFunction, constantDerivative);
        // Метод Ньютона для линейной функции должен сойтись за одну итерацию
        double result = linearSolver.solve(0.0);
        // Проверяем, что нашли правильный корень
        assertEquals(3.0, result, 1e-10);
    }
}