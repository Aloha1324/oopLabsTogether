package functions.factory;

import functions.TabulatedFunction;

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
}