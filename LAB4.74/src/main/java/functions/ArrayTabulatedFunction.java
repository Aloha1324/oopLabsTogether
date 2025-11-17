package functions;

import exceptions.InterpolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

public class ArrayTabulatedFunction extends AbstractTabulatedFunction
        implements Insertable, Removable, Iterable<Point>, Cloneable, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(ArrayTabulatedFunction.class);
    private static final long serialVersionUID = 8305720685834923448L;

    private double[] xValues;
    private double[] yValues;

    public ArrayTabulatedFunction(double[] xValues, double[] yValues) {
        logger.debug("Создание ArrayTabulatedFunction из массивов: xValues.length={}, yValues.length={}",
                xValues != null ? xValues.length : "null", yValues != null ? yValues.length : "null");

        if (xValues == null || yValues == null) {
            logger.error("Один из массивов равен null");
            throw new NullPointerException("Arrays must not be null");
        }
        if (xValues.length < 2) {
            logger.error("Недостаточно точек: {}", xValues.length);
            throw new IllegalArgumentException("At least 2 points are required");
        }

        checkLengthIsTheSame(xValues, yValues);
        checkSorted(xValues);
        this.xValues = Arrays.copyOf(xValues, xValues.length);
        this.yValues = Arrays.copyOf(yValues, yValues.length);

        logger.info("Создан ArrayTabulatedFunction с {} точками", this.xValues.length);
    }

    public ArrayTabulatedFunction(MathFunction func, double xFrom, double xTo, int count) {
        logger.debug("Создание ArrayTabulatedFunction из функции: xFrom={}, xTo={}, count={}", xFrom, xTo, count);

        if (func == null) {
            logger.error("Функция равна null");
            throw new NullPointerException("Function must not be null");
        }
        if (count < 2) {
            logger.error("Недостаточно точек: {}", count);
            throw new IllegalArgumentException("At least 2 points are required");
        }
        if (xFrom >= xTo) {
            logger.error("Некорректный диапазон: xFrom={} >= xTo={}", xFrom, xTo);
            throw new IllegalArgumentException("xFrom must be less than xTo");
        }

        this.xValues = new double[count];
        this.yValues = new double[count];
        double step = (xTo - xFrom) / (count - 1);

        for (int i = 0; i < count; i++) {
            this.xValues[i] = xFrom + i * step;
            this.yValues[i] = func.apply(this.xValues[i]);
        }

        logger.info("Создан ArrayTabulatedFunction из функции с {} точками", count);
    }

    public void insert(double x, double y) {
        logger.debug("Вставка точки: x={}, y={}", x, y);

        int index = indexOfX(x);
        if (index != -1) {
            logger.debug("Точка с x={} существует, обновление Y", x);
            setY(index, y);
        } else {
            int count = getCount();
            double[] newXValues = new double[count + 1];
            double[] newYValues = new double[count + 1];
            int insertIndex = 0;

            while (insertIndex < count && xValues[insertIndex] < x) {
                insertIndex++;
            }

            System.arraycopy(xValues, 0, newXValues, 0, insertIndex);
            System.arraycopy(yValues, 0, newYValues, 0, insertIndex);
            newXValues[insertIndex] = x;
            newYValues[insertIndex] = y;
            System.arraycopy(xValues, insertIndex, newXValues, insertIndex + 1, count - insertIndex);
            System.arraycopy(yValues, insertIndex, newYValues, insertIndex + 1, count - insertIndex);
            xValues = newXValues;
            yValues = newYValues;

            logger.debug("Вставлена новая точка, count={}", getCount());
        }
    }

    public void remove(int index) {
        logger.debug("Удаление точки по индексу: {}", index);

        if (index < 0 || index >= getCount()) {
            logger.error("Индекс вне диапазона: {} (допустимо: 0..{})", index, getCount() - 1);
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }

        if (getCount() <= 2) {
            logger.error("Нельзя удалить точку - требуется минимум 2 точки, текущее количество: {}", getCount());
            throw new IllegalStateException("Cannot remove point - at least 2 points are required");
        }

        int newCount = getCount() - 1;
        double[] newXValues = new double[newCount];
        double[] newYValues = new double[newCount];

        System.arraycopy(xValues, 0, newXValues, 0, index);
        System.arraycopy(yValues, 0, newYValues, 0, index);
        System.arraycopy(xValues, index + 1, newXValues, index, newCount - index);
        System.arraycopy(yValues, index + 1, newYValues, index, newCount - index);

        xValues = newXValues;
        yValues = newYValues;

        logger.debug("Точка удалена, новый count={}", getCount());
    }

    public int getCount() {
        logger.trace("Получение количества точек: {}", xValues != null ? xValues.length : 0);
        return xValues.length;
    }

    public double getX(int index) {
        logger.trace("Получение X по индексу: {}", index);
        if (index < 0 || index >= getCount()) {
            logger.error("Индекс вне диапазона: {} (допустимо: 0..{})", index, getCount() - 1);
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        double result = xValues[index];
        logger.trace("X[{}] = {}", index, result);
        return result;
    }

    public double getY(int index) {
        logger.trace("Получение Y по индексу: {}", index);
        if (index < 0 || index >= getCount()) {
            logger.error("Индекс вне диапазона: {} (допустимо: 0..{})", index, getCount() - 1);
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        double result = yValues[index];
        logger.trace("Y[{}] = {}", index, result);
        return result;
    }

    public void setY(int index, double value) {
        logger.debug("Установка Y[{}] = {}", index, value);
        if (index < 0 || index >= getCount()) {
            logger.error("Индекс вне диапазона: {} (допустимо: 0..{})", index, getCount() - 1);
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        double oldValue = yValues[index];
        yValues[index] = value;
        logger.debug("Y[{}] изменен с {} на {}", index, oldValue, value);
    }

    public int indexOfX(double x) {
        logger.trace("Поиск индекса по X: {}", x);
        for (int i = 0; i < getCount(); i++) {
            if (Double.compare(xValues[i], x) == 0) {
                logger.trace("Найден индекс {} для X={}", i, x);
                return i;
            }
        }
        logger.trace("X={} не найден", x);
        return -1;
    }

    public int indexOfY(double y) {
        logger.trace("Поиск индекса по Y: {}", y);
        for (int i = 0; i < getCount(); i++) {
            if (Double.compare(yValues[i], y) == 0) {
                logger.trace("Найден индекс {} для Y={}", i, y);
                return i;
            }
        }
        logger.trace("Y={} не найден", y);
        return -1;
    }

    public double leftBound() {
        double result = xValues[0];
        logger.trace("Левая граница: {}", result);
        return result;
    }

    public double rightBound() {
        double result = xValues[getCount() - 1];
        logger.trace("Правая граница: {}", result);
        return result;
    }

    protected int floorIndexOfX(double x) {
        logger.trace("Поиск floorIndexOfX для x={}", x);

        int count = getCount();
        if (x < xValues[0]) {
            logger.trace("x={} меньше левой границы, возвращаем 0", x);
            return 0;
        }
        if (x >= xValues[count - 1]) {
            logger.trace("x={} больше правой границы, возвращаем {}", x, count - 2);
            return count - 2;
        }

        for (int i = 0; i < count - 1; i++) {
            if (x >= xValues[i] && x < xValues[i + 1]) {
                logger.trace("Найден floorIndexOfX: {} для x={}", i, x);
                return i;
            }
        }

        logger.trace("Возвращаем floorIndexOfX: {} для x={}", count - 2, x);
        return count - 2;
    }

    protected double extrapolateLeft(double x) {
        logger.debug("Экстраполяция слева для x={}", x);
        double result = interpolate(x, xValues[0], xValues[1], yValues[0], yValues[1]);
        logger.debug("Результат экстраполяции слева: {}", result);
        return result;
    }

    protected double extrapolateRight(double x) {
        logger.debug("Экстраполяция справа для x={}", x);
        int count = getCount();
        double result = interpolate(x, xValues[count - 2], xValues[count - 1], yValues[count - 2], yValues[count - 1]);
        logger.debug("Результат экстраполяции справа: {}", result);
        return result;
    }

    protected double interpolate(double x, int floorIndex) {
        logger.debug("Интерполяция для x={} в интервале {}", x, floorIndex);

        if (floorIndex < 0 || floorIndex >= getCount() - 1) {
            logger.error("Некорректный floorIndex: {} (допустимо: 0..{})", floorIndex, getCount() - 2);
            throw new IllegalArgumentException("Invalid floor index: " + floorIndex);
        }

        double leftX = xValues[floorIndex], rightX = xValues[floorIndex + 1];
        if (x < leftX || x > rightX) {
            logger.error("x={} вне интервала интерполяции [{}, {}]", x, leftX, rightX);
            throw new InterpolationException("x is outside the interpolation interval [" + leftX + ", " + rightX + "]");
        }

        double result = interpolate(x, leftX, rightX, yValues[floorIndex], yValues[floorIndex + 1]);
        logger.debug("Результат интерполяции: {}", result);
        return result;
    }

    protected double interpolate(double x, double leftX, double rightX, double leftY, double rightY) {
        logger.trace("Линейная интерполяция: x={}, x0={}, x1={}, y0={}, y1={}", x, leftX, rightX, leftY, rightY);

        if (leftX == rightX) {
            logger.debug("Граничные точки совпадают, возвращаем среднее значение");
            return (leftY + rightY) / 2.0;
        }

        double result = leftY + ((x - leftX) * (rightY - leftY)) / (rightX - leftX);
        logger.trace("Результат линейной интерполяции: {}", result);
        return result;
    }

    public double apply(double x) {
        logger.debug("Вычисление apply для x={}", x);

        if (x < xValues[0]) {
            logger.debug("x={} меньше левой границы, используем экстраполяцию слева", x);
            return extrapolateLeft(x);
        }
        if (x > xValues[getCount() - 1]) {
            logger.debug("x={} больше правой границы, используем экстраполяцию справа", x);
            return extrapolateRight(x);
        }

        int exactIndex = indexOfX(x);
        if (exactIndex != -1) {
            logger.debug("Точное совпадение x={} найдено в индексе {}", x, exactIndex);
            return yValues[exactIndex];
        }

        int floorIndex = floorIndexOfX(x);
        logger.debug("Интерполяция x={} в интервале с индексом {}", x, floorIndex);
        return interpolate(x, floorIndex);
    }

    public ArrayTabulatedFunction clone() {
        logger.debug("Клонирование ArrayTabulatedFunction");

        try {
            ArrayTabulatedFunction cloned = (ArrayTabulatedFunction) super.clone();
            cloned.xValues = Arrays.copyOf(this.xValues, this.getCount());
            cloned.yValues = Arrays.copyOf(this.yValues, this.getCount());
            logger.debug("Клонирована функция с {} точками", cloned.getCount());
            return cloned;
        } catch (CloneNotSupportedException e) {
            logger.error("Ошибка клонирования", e);
            throw new AssertionError("Clone should be supported");
        }
    }

    public boolean equals(Object obj) {
        logger.trace("Сравнение объектов: this={}, other={}", this, obj);

        if (this == obj) {
            logger.debug("Сравнение с тем же объектом - равны");
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            logger.debug("Объекты разных классов или другой объект null - не равны");
            return false;
        }

        boolean result = super.equals(obj);
        logger.debug("Результат сравнения: {}", result);
        return result;
    }

    public int hashCode() {
        logger.trace("Вычисление хэш-кода");
        int result = super.hashCode();
        logger.trace("Вычисленный хэш-код: {}", result);
        return result;
    }

    public String toString() {
        logger.trace("Генерация строкового представления");
        String result = super.toString();
        logger.debug("Сгенерировано строковое представление: {}", result);
        return result;
    }

    public Iterator<Point> iterator() {
        logger.trace("Создание итератора");
        return new Iterator<Point>() {
            private int i = 0;

            public boolean hasNext() {
                boolean hasNext = i < getCount();
                logger.trace("Проверка наличия следующей точки: {}", hasNext);
                return hasNext;
            }

            public Point next() {
                if (!hasNext()) {
                    logger.error("Попытка получить следующую точку при отсутствии элементов");
                    throw new java.util.NoSuchElementException();
                }
                Point point = new Point(xValues[i], yValues[i]);
                logger.trace("Итератор вернул точку {}: [{}, {}]", i, point.x, point.y);
                i++;
                return point;
            }

            public void remove() {
                logger.error("Попытка вызвать remove() у итератора");
                throw new UnsupportedOperationException("remove");
            }
        };
    }
}