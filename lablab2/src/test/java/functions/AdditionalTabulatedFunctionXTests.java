package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Дополнительные тесты для комбинаций функций
 * Тестирует сложные функции, композиции и комбинации табулированных функций
 */
class AdditionalFunctionTests {

    // Тест композиции табулированной функции с обычной функцией
    @Test
    void testTabulatedWithMathFunctionComposition() {
        // Создаем табулированную функцию f(x) = x
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 2.0, 3.0};
        LinkedListTabulatedFunctionX tabulated = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Создаем квадратичную функцию g(x) = x²
        MathFunction square = new MathFunction() {
            public double apply(double x) {
                return x * x;
            }
        };

        // Композиция: g(f(x)) = (x)²
        CompositeFunction composition = new CompositeFunction(tabulated, square);

        assertEquals(0.0, composition.apply(0.0), 1e-10);  // 0² = 0
        assertEquals(1.0, composition.apply(1.0), 1e-10);  // 1² = 1
        assertEquals(4.0, composition.apply(2.0), 1e-10);  // 2² = 4
        assertEquals(9.0, composition.apply(3.0), 1e-10);  // 3² = 9

        // Проверяем интерполированные значения
        assertEquals(2.25, composition.apply(1.5), 1e-10); // 1.5² = 2.25
    }

    // Тест композиции двух табулированных функций
    @Test
    void testTwoTabulatedFunctionsComposition() {
        // Первая функция: f(x) = 2x
        double[] xValues1 = {0.0, 1.0, 2.0};
        double[] yValues1 = {0.0, 2.0, 4.0};
        LinkedListTabulatedFunctionX f = new LinkedListTabulatedFunctionX(xValues1, yValues1);

        // Вторая функция: g(x) = x + 1
        double[] xValues2 = {0.0, 2.0, 4.0};
        double[] yValues2 = {1.0, 3.0, 5.0};
        LinkedListTabulatedFunctionX g = new LinkedListTabulatedFunctionX(xValues2, yValues2);

        // Композиция: g(f(x)) = (2x) + 1
        CompositeFunction composition = new CompositeFunction(f, g);

        assertEquals(1.0, composition.apply(0.0), 1e-10);  // 2*0 + 1 = 1
        assertEquals(3.0, composition.apply(1.0), 1e-10);  // 2*1 + 1 = 3
        assertEquals(5.0, composition.apply(2.0), 1e-10);  // 2*2 + 1 = 5

        // Проверяем интерполированные значения
        assertEquals(2.0, composition.apply(0.5), 1e-10);  // 2*0.5 + 1 = 2
    }

    // Тест метода andThen с табулированными функциями
    @Test
    void testAndThenWithTabulatedFunctions() {
        // f(x) = x
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 2.0};
        LinkedListTabulatedFunctionX f = new LinkedListTabulatedFunctionX(xValues, yValues);

        // g(x) = 3x
        MathFunction g = new MathFunction() {
            public double apply(double x) {
                return 3 * x;
            }
        };

        // h(x) = x²
        MathFunction h = new MathFunction() {
            public double apply(double x) {
                return x * x;
            }
        };

        // Цепочка: h(g(f(x))) = (3x)²
        MathFunction chain = f.andThen(g).andThen(h);

        assertEquals(0.0, chain.apply(0.0), 1e-10);   // (3*0)² = 0
        assertEquals(9.0, chain.apply(1.0), 1e-10);   // (3*1)² = 9
        assertEquals(36.0, chain.apply(2.0), 1e-10);  // (3*2)² = 36
    }

    // Тест комбинации табулированной функции с методом Ньютона
    @Test
    void testTabulatedFunctionWithNewtonMethod() {
        // Создаем функцию f(x) = x² - 4 (корни x = ±2)
        MathFunction squareMinusFour = new MathFunction() {
            public double apply(double x) {
                return x * x - 4;
            }
        };

        // Производная: f'(x) = 2x
        MathFunction derivative = new MathFunction() {
            public double apply(double x) {
                return 2 * x;
            }
        };

        // Создаем решатель Ньютона
        NewtonSolver newton = new NewtonSolver(squareMinusFour, derivative);

        // Создаем табулированную функцию, которая добавляет 10 к результату
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {10.0, 11.0, 12.0};
        LinkedListTabulatedFunctionX adder = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Композиция: adder(newton(x)) ≈ 2 + 10 = 12
        CompositeFunction composition = new CompositeFunction(newton, adder);

        // Метод Ньютона найдет корень ≈2.0, затем добавится 10
        double result = composition.apply(3.0); // Начальное приближение 3.0
        assertEquals(12.0, result, 1e-6);
    }

    // Тест вложенных композиций табулированных функций
    @Test
    void testNestedTabulatedCompositions() {
        // f(x) = x + 1
        double[] xValues1 = {0.0, 1.0, 2.0};
        double[] yValues1 = {1.0, 2.0, 3.0};
        LinkedListTabulatedFunctionX plusOne = new LinkedListTabulatedFunctionX(xValues1, yValues1);

        // g(x) = 2x
        double[] xValues2 = {1.0, 2.0, 3.0};
        double[] yValues2 = {2.0, 4.0, 6.0};
        LinkedListTabulatedFunctionX timesTwo = new LinkedListTabulatedFunctionX(xValues2, yValues2);

        // h(x) = x²
        MathFunction square = new MathFunction() {
            public double apply(double x) {
                return x * x;
            }
        };

        // Сложная композиция: square(timesTwo(plusOne(x))) = (2(x+1))²
        CompositeFunction inner = new CompositeFunction(plusOne, timesTwo); // 2(x+1)
        CompositeFunction outer = new CompositeFunction(inner, square);     // (2(x+1))²

        assertEquals(4.0, outer.apply(0.0), 1e-10);   // (2(0+1))² = 4
        assertEquals(16.0, outer.apply(1.0), 1e-10);  // (2(1+1))² = 16
        assertEquals(36.0, outer.apply(2.0), 1e-10);  // (2(2+1))² = 36
    }

    // Тест идентичной функции с табулированной функцией
    @Test
    void testIdentityWithTabulatedFunction() {
        // Табулированная функция f(x) = 3x
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 3.0, 6.0};
        LinkedListTabulatedFunctionX triple = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Идентичная функция
        MathFunction identity = new MathFunction() {
            public double apply(double x) {
                return x;
            }
        };

        // Композиция в обоих направлениях
        CompositeFunction comp1 = new CompositeFunction(identity, triple); // 3x
        CompositeFunction comp2 = new CompositeFunction(triple, identity); // 3x

        assertEquals(0.0, comp1.apply(0.0), 1e-10);
        assertEquals(3.0, comp1.apply(1.0), 1e-10);
        assertEquals(6.0, comp1.apply(2.0), 1e-10);

        assertEquals(0.0, comp2.apply(0.0), 1e-10);
        assertEquals(3.0, comp2.apply(1.0), 1e-10);
        assertEquals(6.0, comp2.apply(2.0), 1e-10);
    }

    // Тест постоянной функции с табулированной функцией
    @Test
    void testConstantWithTabulatedFunction() {
        // Постоянная функция всегда возвращает 5
        MathFunction constant = new MathFunction() {
            public double apply(double x) {
                return 5.0;
            }
        };

        // Табулированная функция f(x) = 2x
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 2.0, 4.0};
        LinkedListTabulatedFunctionX doubleX = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Композиция: constant(doubleX(x)) = 5 (всегда)
        CompositeFunction comp1 = new CompositeFunction(doubleX, constant);
        assertEquals(5.0, comp1.apply(0.0), 1e-10);
        assertEquals(5.0, comp1.apply(1.0), 1e-10);
        assertEquals(5.0, comp1.apply(2.0), 1e-10);
        assertEquals(5.0, comp1.apply(100.0), 1e-10);

        // Композиция: doubleX(constant(x)) = doubleX(5) = интерполированное значение
        CompositeFunction comp2 = new CompositeFunction(constant, doubleX);
        // Для x=5 нужно экстраполировать (последние точки: (2,4) и (1,2))
        // Экстраполяция: 4 + (4-2)/(2-1) * (5-2) = 4 + 2 * 3 = 10
        assertEquals(10.0, comp2.apply(0.0), 1e-10);
    }

    // Тест цепочки из трех табулированных функций
    @Test
    void testThreeTabulatedFunctionChain() {
        // f(x) = x + 1
        double[] x1 = {0.0, 1.0, 2.0};
        double[] y1 = {1.0, 2.0, 3.0};
        LinkedListTabulatedFunctionX plusOne = new LinkedListTabulatedFunctionX(x1, y1);

        // g(x) = 2x
        double[] x2 = {1.0, 2.0, 3.0};
        double[] y2 = {2.0, 4.0, 6.0};
        LinkedListTabulatedFunctionX timesTwo = new LinkedListTabulatedFunctionX(x2, y2);

        // h(x) = x - 1
        double[] x3 = {2.0, 4.0, 6.0};
        double[] y3 = {1.0, 3.0, 5.0};
        LinkedListTabulatedFunctionX minusOne = new LinkedListTabulatedFunctionX(x3, y3);

        // Цепочка: h(g(f(x))) = (2(x+1)) - 1 = 2x + 1
        CompositeFunction chain = new CompositeFunction(
                new CompositeFunction(plusOne, timesTwo),
                minusOne
        );

        assertEquals(1.0, chain.apply(0.0), 1e-10);  // 2*0 + 1 = 1
        assertEquals(3.0, chain.apply(1.0), 1e-10);  // 2*1 + 1 = 3
        assertEquals(5.0, chain.apply(2.0), 1e-10);  // 2*2 + 1 = 5
    }

    // Тест экстраполяции в композициях
    @Test
    void testExtrapolationInCompositions() {
        // Табулированная функция определена на [1, 3]
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0}; // Примерно x²
        LinkedListTabulatedFunctionX tabulated = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Функция умножает на 2
        MathFunction timesTwo = new MathFunction() {
            public double apply(double x) {
                return 2 * x;
            }
        };

        // Композиция: timesTwo(tabulated(x))
        CompositeFunction composition = new CompositeFunction(tabulated, timesTwo);

        // Внутри интервала
        assertEquals(2.0, composition.apply(1.0), 1e-10);   // 2*1 = 2
        assertEquals(8.0, composition.apply(2.0), 1e-10);   // 2*4 = 8
        assertEquals(18.0, composition.apply(3.0), 1e-10);  // 2*9 = 18

        // Экстраполяция слева (x=0)
        // Табулированная функция экстраполирует: использует точки (1,1) и (2,4)
        // Для x=0: 1 + (4-1)/(2-1) * (0-1) = 1 + 3*(-1) = -2
        // Затем умножаем на 2: -2 * 2 = -4
        assertEquals(-4.0, composition.apply(0.0), 1e-10);

        // Экстраполяция справа (x=4)
        // Табулированная функция экстраполирует: использует точки (2,4) и (3,9)
        // Для x=4: 4 + (9-4)/(3-2) * (4-2) = 4 + 5*2 = 14
        // Затем умножаем на 2: 14 * 2 = 28
        assertEquals(28.0, composition.apply(4.0), 1e-10);
    }

    // Тест производительности с длинными цепочками
    @Test
    void testPerformanceWithLongChains() {
        // Создаем простую табулированную функцию
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 2.0};
        LinkedListTabulatedFunctionX base = new LinkedListTabulatedFunctionX(xValues, yValues);

        // Создаем несколько простых функций для цепочки
        MathFunction[] functions = new MathFunction[5];
        for (int i = 0; i < functions.length; i++) {
            final int multiplier = i + 1;
            functions[i] = new MathFunction() {
                public double apply(double x) {
                    return x * multiplier;
                }
            };
        }

        // Строим длинную цепочку: f5(f4(f3(f2(f1(base(x))))))
        MathFunction chain = base;
        for (MathFunction func : functions) {
            chain = chain.andThen(func);
        }

        // Проверяем, что цепочка работает корректно
        // base(1.0) = 1.0, затем умножаем на 1, 2, 3, 4, 5: 1 * 1 * 2 * 3 * 4 * 5 = 120
        assertEquals(120.0, chain.apply(1.0), 1e-10);
    }
}