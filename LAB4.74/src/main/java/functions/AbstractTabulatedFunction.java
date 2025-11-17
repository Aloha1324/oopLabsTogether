package functions;

import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTabulatedFunction implements TabulatedFunction {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTabulatedFunction.class);

    protected abstract int floorIndexOfX(double x);
    protected abstract double extrapolateLeft(double x);
    protected abstract double extrapolateRight(double x);
    protected abstract double interpolate(double x, int floorIndex);

    protected double interpolate(double x, double leftX, double rightX, double leftY, double rightY) {
        logger.trace("Интерполяция: x={}, leftX={}, rightX={}, leftY={}, rightY={}",
                x, leftX, rightX, leftY, rightY);

        if (leftX == rightX) {
            logger.debug("Граничные точки совпадают, возвращаем среднее значение");
            return (leftY + rightY) / 2.0;
        }

        double result = leftY + (rightY - leftY) * (x - leftX) / (rightX - leftX);
        logger.debug("Результат интерполяции: {}", result);
        return result;
    }

    public double apply(double x) {
        logger.debug("Вычисление apply для x={}", x);

        if (getCount() == 0) {
            logger.error("Попытка вычислить значение для пустой табулированной функции");
            throw new IllegalStateException("Tabulated function is empty");
        }

        if (x < leftBound()) {
            logger.debug("x={} меньше левой границы {}, используем экстраполяцию слева", x, leftBound());
            return extrapolateLeft(x);
        } else if (x > rightBound()) {
            logger.debug("x={} больше правой границы {}, используем экстраполяцию справа", x, rightBound());
            return extrapolateRight(x);
        } else {
            int exactIndex = indexOfX(x);
            if (exactIndex != -1) {
                logger.debug("Точное совпадение x={} найдено в индексе {}", x, exactIndex);
                return getY(exactIndex);
            } else {
                int floorIndex = floorIndexOfX(x);
                logger.debug("Интерполяция x={} в интервале с индексом {}", x, floorIndex);

                if (floorIndex < 0 || floorIndex >= getCount()) {
                    logger.error("Некорректный floorIndex: {} для x={}", floorIndex, x);
                    throw new IllegalStateException("Invalid floor index calculated: " + floorIndex);
                }
                return interpolate(x, floorIndex);
            }
        }
    }

    public static void checkLengthIsTheSame(double[] xValues, double[] yValues) {
        logger.debug("Проверка длины массивов: xValues.length={}, yValues.length={}",
                xValues.length, yValues.length);

        if (xValues.length != yValues.length) {
            logger.error("Массивы разной длины: xValues={}, yValues={}",
                    xValues.length, yValues.length);
            throw new DifferentLengthOfArraysException("Arrays xValues and yValues must have the same length");
        }
        logger.debug("Проверка длины массивов пройдена успешно");
    }

    public static void checkSorted(double[] xValues) {
        logger.debug("Проверка отсортированности массива xValues длиной {}", xValues.length);

        for (int i = 1; i < xValues.length; i++) {
            if (xValues[i] <= xValues[i - 1]) {
                logger.error("Массив не отсортирован: xValues[{}]={} <= xValues[{}]={}",
                        i, xValues[i], i-1, xValues[i-1]);
                throw new ArrayIsNotSortedException("xValues array must be sorted in ascending order");
            }
        }
        logger.debug("Проверка отсортированности пройдена успешно");
    }

    public String toString() {
        logger.trace("Генерация строкового представления функции");

        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" size = ").append(getCount());

        for (Point point : this) {
            sb.append("\n[").append(point.x).append("; ").append(point.y).append("]");
        }

        String result = sb.toString();
        logger.debug("Сгенерировано строковое представление: {}", result);
        return result;
    }

    public boolean equals(Object o) {
        logger.trace("Сравнение объектов: this={}, other={}", this, o);

        if (this == o) {
            logger.debug("Сравнение с тем же объектом - равны");
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            logger.debug("Объекты разных классов или другой объект null - не равны");
            return false;
        }

        TabulatedFunction that = (TabulatedFunction) o;

        if (getCount() != that.getCount()) {
            logger.debug("Разное количество точек: this.count={}, that.count={}",
                    getCount(), that.getCount());
            return false;
        }

        for (int i = 0; i < getCount(); i++) {
            if (Double.compare(getX(i), that.getX(i)) != 0 ||
                    Double.compare(getY(i), that.getY(i)) != 0) {
                logger.debug("Точки различаются в индексе {}: this[x={}, y={}], that[x={}, y={}]",
                        i, getX(i), getY(i), that.getX(i), that.getY(i));
                return false;
            }
        }

        logger.debug("Объекты идентичны");
        return true;
    }

    public int hashCode() {
        logger.trace("Вычисление хэш-кода функции");

        int result = 1;

        for (int i = 0; i < getCount(); i++) {
            long xBits = Double.doubleToLongBits(getX(i));
            long yBits = Double.doubleToLongBits(getY(i));

            result = 31 * result + (int) (xBits ^ (xBits >>> 32));
            result = 31 * result + (int) (yBits ^ (yBits >>> 32));
        }

        logger.debug("Вычисленный хэш-код: {}", result);
        return result;
    }

    protected Object clone() throws CloneNotSupportedException {
        logger.trace("Клонирование объекта");
        return super.clone();
    }

    public Iterator<Point> iterator() {
        logger.trace("Создание итератора для функции");
        return new Iterator<Point>() {
            private int currentIndex = 0;

            public boolean hasNext() {
                boolean hasNext = currentIndex < getCount();
                logger.trace("Проверка наличия следующей точки: {}", hasNext);
                return hasNext;
            }

            public Point next() {
                if (!hasNext()) {
                    logger.error("Попытка получить следующую точку при отсутствии элементов");
                    throw new NoSuchElementException("No more points available");
                }

                Point point = new Point(getX(currentIndex), getY(currentIndex));
                logger.trace("Возвращена точка {}: [{}, {}]", currentIndex, point.x, point.y);
                currentIndex++;
                return point;
            }
        };
    }
}