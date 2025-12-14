package com.example.LAB5.functions.factory;

import com.example.LAB5.functions.LinkedListTabulatedFunction;
import com.example.LAB5.functions.TabulatedFunction;

/**
 * Фабрика для создания табулированных функций на основе связного списка
 */
public class LinkedListTabulatedFunctionFactory implements TabulatedFunctionFactory {

    /**
     * Создает табулированную функцию на основе связного списка
     *
     *xValues массив значений x
     *yValues массив значений y
     * новая LinkedListTabulatedFunction
     */
    @Override
    public TabulatedFunction create(double[] xValues, double[] yValues) {
        return new LinkedListTabulatedFunction(xValues, yValues);
    }
}
