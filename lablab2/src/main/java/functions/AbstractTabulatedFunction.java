package functions;

import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Абстрактный класс для табулированных функций, реализующий общий функционал
 * для различных способов хранения данных (массивы, связные списки)
 */
public abstract class AbstractTabulatedFunction implements TabulatedFunction {

    /**
     * Метод поиска индекса максимального значения x, которое меньше заданного x
     * Для набора значений x [-3., 4., 6.] метод, применённый к 4.5, должен вернуть 1
     * Если все x больше заданного, метод должен вернуть 0
     * Если все x меньше заданного, метод должен вернуть count
     */
    protected abstract int floorIndexOfX(double x);

    /**
     * Метод экстраполяции для значений слева от минимального x
     */
    protected abstract double extrapolateLeft(double x);

    /**
     * Метод экстраполяции для значений справа от максимального x
     */
    protected abstract double extrapolateRight(double x);

    /**
     * Метод интерполяции с указанием индекса интервала
     */
    protected abstract double interpolate(double x, int floorIndex);

    /**
     * Защищенный метод с реализацией линейной интерполяции
     */
    protected double interpolate(double x, double leftX, double rightX, double leftY, double rightY) {
        // Проверка на совпадение граничных точек
        if (leftX == rightX) {
            return (leftY + rightY) / 2.0;
        }
        // Формула линейной интерполяции
        return leftY + (rightY - leftY) * (x - leftX) / (rightX - leftX);
    }

    /**
     * Реализация метода apply для вычисления значения функции в любой точке x.
     * Использует интерполяцию и экстраполяцию на основе табличных значений
     */
    @Override
    public double apply(double x) {
        // Проверка на пустую таблицу
        if (getCount() == 0) {
            throw new IllegalStateException("Tabulated function is empty");
        }

        // Определение положения x относительно границ функции
        if (x < leftBound()) {
            return extrapolateLeft(x);
        } else if (x > rightBound()) {
            return extrapolateRight(x);
        } else {
            int exactIndex = indexOfX(x);
            if (exactIndex != -1) {
                return getY(exactIndex);
            } else {
                int floorIndex = floorIndexOfX(x);
                // Дополнительная проверка корректности индекса
                if (floorIndex < 0 || floorIndex >= getCount()) {
                    throw new IllegalStateException("Invalid floor index calculated: " + floorIndex);
                }
                return interpolate(x, floorIndex);
            }
        }
    }

    /**
     * Статический метод для проверки одинаковой длины массивов x и y
     * @param xValues массив значений x
     * @param yValues массив значений y
     * @throws DifferentLengthOfArraysException если массивы разной длины
     */
    public static void checkLengthIsTheSame(double[] xValues, double[] yValues) {
        if (xValues.length != yValues.length) {
            throw new DifferentLengthOfArraysException("Arrays xValues and yValues must have the same length");
        }
    }

    /**
     * Статический метод для проверки отсортированности массива x по возрастанию
     * @param xValues массив значений x
     * @throws ArrayIsNotSortedException если массив не отсортирован по возрастанию
     */
    public static void checkSorted(double[] xValues) {
        for (int i = 1; i < xValues.length; i++) {
            if (xValues[i] <= xValues[i - 1]) {
                throw new ArrayIsNotSortedException("xValues array must be sorted in ascending order");
            }
        }
    }

    /**
     * Возвращает строковое представление табулированной функции
     * @return строка в формате "ИмяКласса size = [количество точек]\n[x1; y1]\n[x2; y2]..."
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" size = ").append(getCount()).append("\n");

        for (int i = 0; i < getCount(); i++) {
            sb.append("[").append(getX(i)).append("; ").append(getY(i)).append("]\n");
        }

        return sb.toString();
    }

    /**
     * Сравнивает две табулированные функции на равенство
     * @param o объект для сравнения
     * @return true если функции идентичны (одинаковые точки в одинаковом порядке), false в противном случае
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TabulatedFunction that = (TabulatedFunction) o;

        // Проверка количества точек
        if (getCount() != that.getCount()) return false;

        // Поэлементное сравнение всех точек
        for (int i = 0; i < getCount(); i++) {
            if (Double.compare(getX(i), that.getX(i)) != 0 ||
                    Double.compare(getY(i), that.getY(i)) != 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Вычисляет хэш-код функции на основе всех её точек
     * @return хэш-код функции
     */
    @Override
    public int hashCode() {
        int result = 1;

        for (int i = 0; i < getCount(); i++) {
            long xBits = Double.doubleToLongBits(getX(i));
            long yBits = Double.doubleToLongBits(getY(i));

            result = 31 * result + (int) (xBits ^ (xBits >>> 32));
            result = 31 * result + (int) (yBits ^ (yBits >>> 32));
        }

        return result;
    }

    /**
     * Создает и возвращает копию объекта
     * @return копия табулированной функции
     * @throws CloneNotSupportedException если клонирование не поддерживается
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Возвращает итератор для прохода по всем точкам функции
     * @return итератор точек
     */
    public Iterator<Point> iterator() {
        return new Iterator<Point>() {
            private int currentIndex = 0;

            /**
             * Проверяет наличие следующей точки
             * @return true если есть следующая точка, false в противном случае
             */
            @Override
            public boolean hasNext() {
                return currentIndex < getCount();
            }

            /**
             * Возвращает следующую точку функции
             * @return следующая точка
             * @throws NoSuchElementException если больше нет точек
             */
            @Override
            public Point next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No more points available");
                }

                Point point = new Point(getX(currentIndex), getY(currentIndex));
                currentIndex++;
                return point;
            }
        };
    }
}