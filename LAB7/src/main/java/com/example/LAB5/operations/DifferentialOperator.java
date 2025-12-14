package com.example.LAB5.operations;

import com.example.LAB5.functions.MathFunction;

/**
 * Дифференциальный оператор для функций
 * Параметризованный интерфейс для типобезопасности
 * <T> тип функции, должен наследоваться от MathFunction
 */
public interface DifferentialOperator<T extends MathFunction> {

    /**
     * Вычисляет производную функции
     * function исходная функция
     * производная функции того же типа T
     */
    T derive(T function);
}