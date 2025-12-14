package com.example.LAB5.functions.factory;

import com.example.LAB5.functions.ArrayTabulatedFunction;
import com.example.LAB5.functions.TabulatedFunction;

/**
 * Фабрика для создания табулированных функций на основе массива
 */
public class ArrayTabulatedFunctionFactory implements TabulatedFunctionFactory {

    /**
     * Создает табулированную функцию на основе массива
     * xValues массив значений x
     *yValues массив значений y
     *новая ArrayTabulatedFunction
     */
    @Override
    public TabulatedFunction create(double[] xValues, double[] yValues) {
        return new ArrayTabulatedFunction(xValues, yValues);
    }
}