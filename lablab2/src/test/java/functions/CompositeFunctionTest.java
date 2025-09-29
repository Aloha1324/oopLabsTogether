package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CompositeFunctionTest {

    @Test
    void testSimpleComposition() {
        // f(x) = x + 1, g(x) = x * 2
        // h(x) = g(f(x)) = (x + 1) * 2
        MathFunction plusOne = new MathFunction() {
            @Override
            public double apply(double x) {
                return x + 1;
            }
        };

        MathFunction timesTwo = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * 2;
            }
        };

        CompositeFunction composite = new CompositeFunction(plusOne, timesTwo);

        assertEquals(4.0, composite.apply(1.0), 1e-10);  // (1 + 1) * 2 = 4
        assertEquals(6.0, composite.apply(2.0), 1e-10);  // (2 + 1) * 2 = 6
        assertEquals(0.0, composite.apply(-1.0), 1e-10); // (-1 + 1) * 2 = 0
    }

    @Test
    void testIdentityFunctionComposition() {
        // f(x) = x (тождественная), g(x) = x * x
        // h(x) = g(f(x)) = x * x
        MathFunction identity = new MathFunction() {
            @Override
            public double apply(double x) {
                return x;
            }
        };

        MathFunction square = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * x;
            }
        };

        CompositeFunction composite = new CompositeFunction(identity, square);

        assertEquals(0.0, composite.apply(0.0), 1e-10);
        assertEquals(1.0, composite.apply(1.0), 1e-10);
        assertEquals(4.0, composite.apply(2.0), 1e-10);
        assertEquals(9.0, composite.apply(3.0), 1e-10);
    }

    @Test
    void testNestedComposition() {
        // f(x) = x + 1, g(x) = x * 2, h(x) = x * x
        // composite1 = g(f(x)) = (x + 1) * 2
        // composite2 = h(composite1(x)) = [(x + 1) * 2] * [(x + 1) * 2]

        MathFunction plusOne = new MathFunction() {
            @Override
            public double apply(double x) {
                return x + 1;
            }
        };

        MathFunction timesTwo = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * 2;
            }
        };

        MathFunction square = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * x;
            }
        };

        // Первая композиция: (x + 1) * 2
        CompositeFunction composite1 = new CompositeFunction(plusOne, timesTwo);

        // Вторая композиция: [(x + 1) * 2] * [(x + 1) * 2]
        CompositeFunction composite2 = new CompositeFunction(composite1, square);

        assertEquals(16.0, composite2.apply(1.0), 1e-10);  // [(1 + 1) * 2] * [(1 + 1) * 2]= 16
        assertEquals(36.0, composite2.apply(2.0), 1e-10);  // [(2 + 1) * 2] * [(2 + 1) * 2] = 36
        assertEquals(0.0, composite2.apply(-1.0), 1e-10);  // [(-1 + 1) * 2] * [(-1 + 1) * 2]= 0
    }

    @Test
    void testSameFunctionComposition() {
        // f(x) = x + 1
        // h(x) = f(f(x)) = (x + 1) + 1 = x + 2
        MathFunction plusOne = new MathFunction() {
            @Override
            public double apply(double x) {
                return x + 1;
            }
        };

        CompositeFunction composite = new CompositeFunction(plusOne, plusOne);

        assertEquals(2.0, composite.apply(0.0), 1e-10);  // 0 + 1 + 1 = 2
        assertEquals(3.0, composite.apply(1.0), 1e-10);  // 1 + 1 + 1 = 3
        assertEquals(0.0, composite.apply(-2.0), 1e-10); // -2 + 1 + 1 = 0
    }

    @Test
    void testComplexNestedComposition() {
        // Создаем цепочку: f(x) = x * x, g(x) = x + 1, h(x) = x * 3
        // composite1 = g(f(x)) = x * x + 1
        // composite2 = h(composite1(x)) = (x * x + 1) * 3

        MathFunction square = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * x;
            }
        };

        MathFunction plusOne = new MathFunction() {
            @Override
            public double apply(double x) {
                return x + 1;
            }
        };

        MathFunction timesThree = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * 3;
            }
        };

        CompositeFunction composite1 = new CompositeFunction(square, plusOne);
        CompositeFunction composite2 = new CompositeFunction(composite1, timesThree);

        assertEquals(3.0, composite2.apply(0.0), 1e-10);  // (0*0 + 1) * 3 = 3
        assertEquals(6.0, composite2.apply(1.0), 1e-10);  // (1*1 + 1) * 3 = 6
        assertEquals(15.0, composite2.apply(2.0), 1e-10); // (2*2 + 1) * 3 = 15
    }

    @Test
    void testNullFunctions() {
        // Проверяем обработку null аргументов
        MathFunction validFunction = new MathFunction() {
            @Override
            public double apply(double x) {
                return x;
            }
        };

        assertThrows(IllegalArgumentException.class, () -> {
            new CompositeFunction(null, validFunction);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new CompositeFunction(validFunction, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new CompositeFunction(null, null);
        });
    }

    @Test
    void testGetters() {
        MathFunction first = new MathFunction() {
            @Override
            public double apply(double x) {
                return x + 1;
            }
        };

        MathFunction second = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * 2;
            }
        };

        CompositeFunction composite = new CompositeFunction(first, second);

        assertEquals(first, composite.getFirstFunction());
        assertEquals(second, composite.getSecondFunction());
    }

    @Test
    void testMultipleLevelComposition() {
        // Многоуровневая композиция: f(g(h(x)))
        // h(x) = x + 1
        // g(x) = x * 2
        // f(x) = x * x
        // result = f(g(h(x))) = [(x + 1) * 2] * [(x + 1) * 2]

        MathFunction plusOne = new MathFunction() {
            @Override
            public double apply(double x) {
                return x + 1;
            }
        };

        MathFunction timesTwo = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * 2;
            }
        };

        MathFunction square = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * x;
            }
        };

        CompositeFunction inner = new CompositeFunction(plusOne, timesTwo); // (x + 1) * 2
        CompositeFunction outer = new CompositeFunction(inner, square);     // [(x + 1) * 2] * [(x + 1) * 2]

        assertEquals(16.0, outer.apply(1.0), 1e-10);  // [(1 + 1) * 2] * [(1 + 1) * 2]  = 16
        assertEquals(36.0, outer.apply(2.0), 1e-10);  // [(2 + 1) * 2] * [(2 + 1) * 2]  = 36
        assertEquals(0.0, outer.apply(-1.0), 1e-10);  // [(-1 + 1) * 2] * [(-1 + 1) * 2]  = 0
    }
}