package functions;

import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;
import exceptions.InterpolationException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Класс табулированной функции, реализованной на основе массивов
 * Наследует абстрактный класс AbstractTabulatedFunction
 */
public class ArrayTabulatedFunction extends AbstractTabulatedFunction implements Cloneable, Insertable {

    // Массивы для хранения значений x и y
    private double[] xValues;
    private double[] yValues;

    /**
     * Конструктор на основе двух массивов (x и y)
     * @param xValues массив значений x
     * @param yValues массив значений y
     * @throws IllegalArgumentException если массивы содержат менее 2 точек
     * @throws DifferentLengthOfArraysException если массивы разной длины
     * @throws ArrayIsNotSortedException если массив x не отсортирован по возрастанию
     */
    public ArrayTabulatedFunction(double[] xValues, double[] yValues) {
        // Проверка минимальной длины
        if (xValues.length < 2) {
            throw new IllegalArgumentException("At least 2 points are required");
        }

        // Проверка одинаковой длины массивов
        checkLengthIsTheSame(xValues, yValues);

        // Проверка отсортированности массива x
        checkSorted(xValues);

        // Создание копий массивов для защиты от внешних изменений
        this.xValues = Arrays.copyOf(xValues, xValues.length);
        this.yValues = Arrays.copyOf(yValues, yValues.length);
    }

    /**
     * Конструктор на основе математической функции с заданным диапазоном и количеством точек
     * @param func математическая функция
     * @param xFrom начальная точка диапазона
     * @param xTo конечная точка диапазона
     * @param count количество точек табуляции
     * @throws IllegalArgumentException если count < 2 или xFrom >= xTo
     */
    public ArrayTabulatedFunction(MathFunction func, double xFrom, double xTo, int count) {
        if (count < 2) {
            throw new IllegalArgumentException("At least 2 points are required");
        }
        if (xFrom >= xTo) {
            throw new IllegalArgumentException("xFrom must be less than xTo");
        }

        this.xValues = new double[count];
        this.yValues = new double[count];

        // Вычисление шага табуляции
        double step = (xTo - xFrom) / (count - 1);

        // Заполнение массивов значениями
        for (int i = 0; i < count; i++) {
            xValues[i] = xFrom + i * step;
            yValues[i] = func.apply(xValues[i]);
        }
    }

    /**
     * Вставляет точку в функцию, сохраняя порядок x
     * @param x координата x
     * @param y координата y
     */
    @Override
    public void insert(double x, double y) {
        int index = indexOfX(x);

        if (index != -1) {
            // Если точка с таким x уже существует, обновляем y
            setY(index, y);
        } else {
            // Создаем новые массивы с увеличенным размером
            double[] newXValues = new double[getCount() + 1];
            double[] newYValues = new double[getCount() + 1];

            // Находим позицию для вставки
            int insertIndex = 0;
            while (insertIndex < getCount() && xValues[insertIndex] < x) {
                insertIndex++;
            }

            // Копируем элементы до позиции вставки
            System.arraycopy(xValues, 0, newXValues, 0, insertIndex);
            System.arraycopy(yValues, 0, newYValues, 0, insertIndex);

            // Вставляем новую точку
            newXValues[insertIndex] = x;
            newYValues[insertIndex] = y;

            // Копируем оставшиеся элементы
            System.arraycopy(xValues, insertIndex, newXValues, insertIndex + 1, getCount() - insertIndex);
            System.arraycopy(yValues, insertIndex, newYValues, insertIndex + 1, getCount() - insertIndex);

            // Заменяем старые массивы новыми
            xValues = newXValues;
            yValues = newYValues;
        }
    }

    /**
     * Возвращает количество точек в функции
     * @return количество точек
     */
    @Override
    public int getCount() {
        return xValues.length;
    }

    /**
     * Возвращает значение x по указанному индексу
     * @param index индекс точки
     * @return значение x
     * @throws IllegalArgumentException если индекс вне диапазона
     */
    @Override
    public double getX(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        return xValues[index];
    }

    /**
     * Возвращает значение y по указанному индексу
     * @param index индекс точки
     * @return значение y
     * @throws IllegalArgumentException если индекс вне диапазона
     */
    @Override
    public double getY(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        return yValues[index];
    }

    /**
     * Устанавливает значение y по указанному индексу
     * @param index индекс точки
     * @param value новое значение y
     * @throws IllegalArgumentException если индекс вне диапазона
     */
    @Override
    public void setY(int index, double value) {
        if (index < 0 || index >= getCount()) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        yValues[index] = value;
    }

    /**
     * Возвращает индекс точки с заданным значением x
     * @param x значение x для поиска
     * @return индекс точки или -1 если точка не найдена
     */
    @Override
    public int indexOfX(double x) {
        for (int i = 0; i < getCount(); i++) {
            if (Double.compare(xValues[i], x) == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Возвращает индекс точки с заданным значением y
     * @param y значение y для поиска
     * @return индекс точки или -1 если точка не найдена
     */
    @Override
    public int indexOfY(double y) {
        for (int i = 0; i < getCount(); i++) {
            if (Double.compare(yValues[i], y) == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Возвращает наименьшее значение x (левая граница)
     * @return минимальное значение x
     */
    @Override
    public double leftBound() {
        return xValues[0];
    }

    /**
     * Возвращает наибольшее значение x (правая граница)
     * @return максимальное значение x
     */
    @Override
    public double rightBound() {
        return xValues[getCount() - 1];
    }

    /**
     * Находит индекс максимального x, который меньше заданного x
     * @param x значение x для поиска
     * @return индекс "пола" для интерполяции
     */
    @Override
    protected int floorIndexOfX(double x) {
        int count = getCount();

        // Если x меньше всех точек, возвращаем 0
        if (x < xValues[0]) {
            return 0;
        }

        // Если x больше всех точек, возвращаем count-2 (последний интервал)
        if (x > xValues[count - 1]) {
            return count - 2;
        }

        // Бинарный поиск нужного интервала
        int left = 0;
        int right = count - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (xValues[mid] == x) {
                return mid;
            } else if (xValues[mid] < x) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        // left указывает на первую точку, которая больше x
        // значит, интервал для интерполяции - от left-1 до left
        return left - 1;
    }

    /**
     * Выполняет экстраполяцию слева от минимального x
     * @param x значение x для экстраполяции
     * @return экстраполированное значение y
     */
    @Override
    protected double extrapolateLeft(double x) {
        // Линейная экстраполяция по первым двум точкам
        return interpolate(x, xValues[0], xValues[1], yValues[0], yValues[1]);
    }

    /**
     * Выполняет экстраполяцию справа от максимального x
     * @param x значение x для экстраполяции
     * @return экстраполированное значение y
     */
    @Override
    protected double extrapolateRight(double x) {
        int count = getCount();
        // Линейная экстраполяция по последним двум точкам
        return interpolate(x, xValues[count - 2], xValues[count - 1], yValues[count - 2], yValues[count - 1]);
    }

    /**
     * Выполняет интерполяцию внутри диапазона по указанному индексу интервала
     * @param x значение x для интерполяции
     * @param floorIndex индекс левой границы интервала
     * @return интерполированное значение y
     * @throws InterpolationException если x вне интервала [x[floorIndex], x[floorIndex+1]]
     */
    @Override
    protected double interpolate(double x, int floorIndex) {
        // Проверка корректности индекса
        if (floorIndex < 0 || floorIndex >= getCount() - 1) {
            throw new IllegalArgumentException("Invalid floor index: " + floorIndex);
        }

        // Проверка, что x находится в пределах интервала
        double leftX = xValues[floorIndex];
        double rightX = xValues[floorIndex + 1];

        if (x < leftX || x > rightX) {
            throw new InterpolationException("x is outside the interpolation interval [" + leftX + ", " + rightX + "]");
        }

        // Линейная интерполяция между floorIndex и floorIndex+1
        return interpolate(x, leftX, rightX, yValues[floorIndex], yValues[floorIndex + 1]);
    }

    /**
     * Создает и возвращает копию объекта
     * @return копия табулированной функции
     */
    @Override
    public ArrayTabulatedFunction clone() {
        try {
            ArrayTabulatedFunction cloned = (ArrayTabulatedFunction) super.clone();
            // Глубокое копирование массивов
            cloned.xValues = Arrays.copyOf(this.xValues, this.getCount());
            cloned.yValues = Arrays.copyOf(this.yValues, this.getCount());
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone should be supported");
        }
    }

    /**
     * Сравнивает две функции на равенство
     * @param obj объект для сравнения
     * @return true если функции идентичны
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ArrayTabulatedFunction that = (ArrayTabulatedFunction) obj;

        // Используем реализацию из родительского класса
        return super.equals(that);
    }

    /**
     * Вычисляет хэш-код функции
     * @return хэш-код
     */
    @Override
    public int hashCode() {
        // Используем реализацию из родительского класса
        return super.hashCode();
    }

    /**
     * Возвращает строковое представление функции
     * @return строковое представление
     */
    @Override
    public String toString() {
        // Используем реализацию из родительского класса
        return super.toString();
    }

    /**
     * Возвращает итератор для прохода по точкам функции
     * @return итератор точек
     */
    @Override
    public Iterator<Point> iterator() {
        return new Iterator<Point>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < getCount();
            }

            @Override
            public Point next() {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException("No more points available");
                }
                Point point = new Point(xValues[currentIndex], yValues[currentIndex]);
                currentIndex++;
                return point;
            }
        };
    }
}