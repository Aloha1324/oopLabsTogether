package com.example.LAB5.functions;

import java.util.Iterator;

/**
 * Интерфейс для табулированных функций.
 * Расширяет MathFunction и Iterable<Point>.
 */
public interface TabulatedFunction extends MathFunction, Iterable<Point> {

    // ================== ОСНОВНЫЕ МЕТОДЫ ТАБУЛИРОВАННОЙ ФУНКЦИИ ==================

    /**
     * Возвращает количество табулированных значений.
     *
     * @return количество точек табуляции
     */
    int getCount();

    /**
     * Получает значение аргумента x по номеру индекса.
     *
     * @param index индекс значения (от 0 до getCount()-1)
     * @return значение x в указанной точке
     */
    double getX(int index);

    /**
     * Получает значение функции y по номеру индекса.
     *
     * @param index индекс значения (от 0 до getCount()-1)
     * @return значение y в указанной точке
     */
    double getY(int index);

    /**
     * Задает значение функции y по номеру индекса.
     *
     * @param index индекс значения (от 0 до getCount()-1)
     * @param value новое значение y
     */
    void setY(int index, double value);

    // ================== МЕТОДЫ ПОИСКА И ГРАНИЦ ==================

    /**
     * Возвращает индекс аргумента x в таблице.
     * Предполагается, что все x различны.
     *
     * @param x значение аргумента для поиска
     * @return индекс x или -1 если не найден
     */
    int indexOfX(double x);

    /**
     * Возвращает индекс первого вхождения значения y.
     *
     * @param y значение функции для поиска
     * @return индекс y или -1 если не найден
     */
    int indexOfY(double y);

    /**
     * Возвращает самый левый x (минимальное значение аргумента).
     *
     * @return минимальное значение x
     */
    double leftBound();

    /**
     * Возвращает самый правый x (максимальное значение аргумента).
     *
     * @return максимальное значение x
     */
    double rightBound();

    // ================== МЕТОДЫ ИНТЕРПОЛЯЦИИ (требуются для CompositeFunction) ==================

    /**
     * Возвращает индекс точки, которая находится слева от x (или самая правая, если x выходит за правую границу).
     * Метод предполагает, что x-значения строго возрастают.
     *
     * @param x точка для поиска
     * @return индекс левой точки (от 0 до getCount()-2)
     */
    int floorIndexOfX(double x);

    /**
     * Линейно интерполирует значение функции в точке x между двумя соседними табулированными точками.
     *
     * @param x     точка, в которой нужно вычислить значение
     * @param index индекс левой точки (результат floorIndexOfX)
     * @return интерполированное значение
     * @throws IndexOutOfBoundsException если index выходит за допустимые границы
     */
    double interpolate(double x, int index);

    // ================== МЕТОДЫ ИЗ Iterable<Point> ==================

    /**
     * Возвращает итератор по точкам функции.
     *
     * @return итератор
     */
    @Override
    Iterator<Point> iterator();

    /**
     * Применяет функцию к значению x.
     * Реализация по умолчанию использует интерполяцию.
     *
     * @param x аргумент
     * @return значение функции в точке x
     */
    @Override
    default double apply(double x) {
        if (getCount() == 0) {
            throw new IllegalStateException("Функция не содержит точек");
        }
        if (x < leftBound()) {
            return getY(0);
        }
        if (x > rightBound()) {
            return getY(getCount() - 1);
        }
        int idx = floorIndexOfX(x);
        return interpolate(x, idx);
    }
}