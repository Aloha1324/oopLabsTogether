package functions.factory;

import functions.ArrayTabulatedFunction;
import functions.TabulatedFunction;

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