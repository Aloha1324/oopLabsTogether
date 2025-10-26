package functions;

public class NewtonSolver implements MathFunction {

    private final MathFunction function;    // Хранит функцию, корень которой ищем
    private final MathFunction derivative;  // Хранит производную функции
    private final double tolerance;         // Точность решения (критерий остановки)
    private final int maxIterations;        // Максимальное количество итераций

    // Конструктор с параметрами по умолчанию
    public NewtonSolver(MathFunction function, MathFunction derivative) {
        // Вызываем полный конструктор с значениями по умолчанию
        this(function, derivative, 1e-10, 100);
    }

    // Полный конструктор со всеми параметрами
    public NewtonSolver(MathFunction function, MathFunction derivative,
                        double tolerance, int maxIterations) {
        // Проверяем, что функции не null
        if (function == null || derivative == null) {
            // Бросаем исключение, если функции не заданы
            throw new IllegalArgumentException("Функция и производная не могут быть null");
        }
        // Проверяем, что параметры положительные
        if (tolerance <= 0 || maxIterations <= 0) {
            // Бросаем исключение при невалидных параметрах
            throw new IllegalArgumentException("Параметры должны быть положительными");
        }

        // Инициализируем поле функции
        this.function = function;
        // Инициализируем поле производной
        this.derivative = derivative;
        // Инициализируем поле точности
        this.tolerance = tolerance;
        // Инициализируем поле максимального числа итераций
        this.maxIterations = maxIterations;
    }

    /**
     * Реализация метода apply из интерфейса MathFunction
     * Использует параметр x как начальное приближение для метода Ньютона
     * @param x начальное приближение для поиска корня
     * @return найденный корень уравнения function(x) = 0
     */
    public double apply(double x) {
        // Используем x как начальное приближение и вызываем метод solve
        return solve(x, false);
    }

    // Основной метод решения без строгой проверки сходимости
    public double solve(double initialGuess) {
        // Вызываем версию метода без строгой проверки сходимости
        return solve(initialGuess, false);
    }

    // Метод решения уравнения методом Ньютона
    public double solve(double initialGuess, boolean requireStrictConvergence) {
        // Начинаем с начального приближения
        double x = initialGuess;
        // Инициализируем предыдущую ошибку как бесконечность
        double previousError = Double.POSITIVE_INFINITY;

        // Цикл по максимальному числу итераций
        for (int i = 0; i < maxIterations; i++) {
            // Вычисляем значение функции в текущей точке
            double fx = function.apply(x);
            // Вычисляем значение производной в текущей точке
            double fpx = derivative.apply(x);

            // Проверяем, что производная не слишком мала
            if (Math.abs(fpx) < tolerance) {
                // Бросаем исключение, если производная близка к нулю
                throw new IllegalArgumentException("Производная близка к нулю в точке x = " + x);
            }

            // Вычисляем новое приближение по формуле Ньютона
            double xNew = x - fx / fpx;
            // Вычисляем значение функции в новой точке
            double fxNew = function.apply(xNew);

            // Проверяем критерий остановки по разности приближений
            if (Math.abs(xNew - x) < tolerance) {
                // Возвращаем решение, если приближения достаточно близки
                return xNew;
            }
            // Проверяем критерий остановки по значению функции
            if (Math.abs(fxNew) < tolerance) {
                // Возвращаем решение, если функция достаточно близка к нулю
                return xNew;
            }

            // Если требуется строгая проверка сходимости
            if (requireStrictConvergence) {
                // Вычисляем текущую ошибку как модуль значения функции
                double currentError = Math.abs(fxNew);
                // Проверяем, не увеличилась ли ошибка более чем на 10%
                if (currentError > previousError * 1.1) {
                    // Бросаем исключение при обнаружении расходимости
                    throw new ArithmeticException("Метод расходится на итерации " + i);
                }
                // Обновляем значение предыдущей ошибки
                previousError = currentError;
            }

            // Переходим к следующему приближению
            x = xNew;
        }

        // Бросаем исключение, если метод не сошелся за отведенное время
        throw new ArithmeticException("Метод не сошелся за " + maxIterations + " итераций");
    }

    // Возвращает установленную точность решения
    public double getTolerance() {
        // Возвращаем значение поля tolerance
        return tolerance;
    }

    // Возвращает максимальное количество итераций
    public int getMaxIterations() {
        // Возвращаем значение поля maxIterations
        return maxIterations;
    }

    // Возвращает решаемую функцию
    public MathFunction getFunction() {
        // Возвращаем ссылку на функцию
        return function;
    }

    // Возвращает производную функции
    public MathFunction getDerivative() {
        // Возвращаем ссылку на производную
        return derivative;
    }
}