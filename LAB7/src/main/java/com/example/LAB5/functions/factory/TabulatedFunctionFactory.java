package com.example.LAB5.functions.factory;

import com.example.LAB5.functions.MathFunction;
import com.example.LAB5.functions.TabulatedFunction;

/**
 * Фабрика для создания табулированных функций
 * Паттерн Абстрактная Фабрика
 */
public interface TabulatedFunctionFactory {

    /**
     * Создает табулированную функцию на основе массивов значений
     * xValues массив значений x
     *  yValues массив значений y
     *  новая табулированная функция
     */
    TabulatedFunction create(double[] xValues, double[] yValues);

    TabulatedFunction create(MathFunction function,
                             double xFrom,
                             double xTo,
                             int count);
}