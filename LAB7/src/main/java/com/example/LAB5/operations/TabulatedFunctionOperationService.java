package com.example.LAB5.operations;

import com.example.LAB5.exceptions.InconsistentFunctionsException;
import com.example.LAB5.functions.TabulatedFunction;
import com.example.LAB5.functions.factory.TabulatedFunctionFactory;

import java.util.ArrayList;
import java.util.List;

public class TabulatedFunctionOperationService {

    private final TabulatedFunctionFactory factory;

    public TabulatedFunctionOperationService(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    // Валидация совместимости доменов
    private void validateCompatibleDomains(TabulatedFunction a, TabulatedFunction b) {
        int n = a.getCount();
        if (n != b.getCount()) {
            throw new InconsistentFunctionsException("Функции имеют разное количество точек: " + a.getCount() + " ≠ " + b.getCount());
        }
        final double EPS = 1e-9;
        for (int i = 0; i < n; i++) {
            if (Math.abs(a.getX(i) - b.getX(i)) > EPS) {
                throw new InconsistentFunctionsException("Несовпадение x-координат в точке " + i + ": " + a.getX(i) + " ≠ " + b.getX(i));
            }
        }
    }

    // Безопасное деление с проверкой на ноль
    private void validateNoDivisionByZero(TabulatedFunction divisor) {
        final double EPS = 1e-12;
        int n = divisor.getCount();
        for (int i = 0; i < n; i++) {
            if (Math.abs(divisor.getY(i)) < EPS) {
                throw new ArithmeticException("Деление на ноль в точке x = " + divisor.getX(i));
            }
        }
    }

    // Обобщённый метод выполнения бинарной операции
    private TabulatedFunction performOperation(TabulatedFunction a, TabulatedFunction b, java.util.function.DoubleBinaryOperator op, boolean isDivision) {
        validateCompatibleDomains(a, b);
        if (isDivision) {
            validateNoDivisionByZero(b);
        }

        int n = a.getCount();
        double[] x = new double[n];
        double[] y = new double[n];

        for (int i = 0; i < n; i++) {
            x[i] = a.getX(i);
            y[i] = op.applyAsDouble(a.getY(i), b.getY(i));
        }

        return factory.create(x, y);
    }

    public TabulatedFunction add(TabulatedFunction a, TabulatedFunction b) {
        return performOperation(a, b, Double::sum, false);
    }

    public TabulatedFunction subtract(TabulatedFunction a, TabulatedFunction b) {
        return performOperation(a, b, (u, v) -> u - v, false);
    }

    public TabulatedFunction multiply(TabulatedFunction a, TabulatedFunction b) {
        return performOperation(a, b, (u, v) -> u * v, false);
    }

    public TabulatedFunction divide(TabulatedFunction a, TabulatedFunction b) {
        return performOperation(a, b, (u, v) -> u / v, true);
    }
}