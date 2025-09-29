package functions;

import java.util.Arrays;

public class ArrayTabulatedFunction extends AbstractTabulatedFunction {

    private final double[] xValues;
    private final double[] yValues;
    private final int count;

    // Конструктор с двумя массивами
    public ArrayTabulatedFunction(double[] xValues, double[] yValues) {
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("Arrays xValues and yValues must have the same length");
        }
        if (xValues.length < 2) {
            throw new IllegalArgumentException("At least 2 points are required");
        }

        // Проверяем, что xValues отсортированы и уникальны
        for (int i = 1; i < xValues.length; i++) {
            if (xValues[i] <= xValues[i - 1]) {
                throw new IllegalArgumentException("xValues must be strictly increasing");
            }
        }

        this.count = xValues.length;
        this.xValues = Arrays.copyOf(xValues, count);
        this.yValues = Arrays.copyOf(yValues, count);
    }

    // Конструктор с функцией и параметрами дискретизации
    public ArrayTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if (count < 2) {
            throw new IllegalArgumentException("At least 2 points are required");
        }

        this.count = count;
        this.xValues = new double[count];
        this.yValues = new double[count];

        // Если xFrom > xTo, меняем местами
        if (xFrom > xTo) {
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }

        // Равномерная дискретизация
        double step = (xTo - xFrom) / (count - 1);
        for (int i = 0; i < count; i++) {
            xValues[i] = xFrom + i * step;
            yValues[i] = source.apply(xValues[i]);
        }
    }

    // Реализация абстрактных методов из AbstractTabulatedFunction

    @Override
    protected int floorIndexOfX(double x) {
        // Если все x больше заданного - возвращаем 0
        if (x < xValues[0]) {
            return 0;
        }

        // Если все x меньше заданного - возвращаем count
        if (x > xValues[count - 1]) {
            return count;
        }

        // Линейный поиск для нахождения интервала
        for (int i = 0; i < count - 1; i++) {
            if (x >= xValues[i] && x < xValues[i + 1]) {
                return i;
            }
        }

        // Если x равен последнему элементу (с учетом погрешности)
        if (Math.abs(x - xValues[count - 1]) < 1e-12) {
            return count - 1;
        }

        return count - 1; // fallback
    }

    @Override
    protected double extrapolateLeft(double x) {
        // Линейная экстраполяция на основе двух первых точек
        return interpolate(x, xValues[0], xValues[1], yValues[0], yValues[1]);
    }

    @Override
    protected double extrapolateRight(double x) {
        // Линейная экстраполяция на основе двух последних точек
        return interpolate(x, xValues[count - 2], xValues[count - 1], yValues[count - 2], yValues[count - 1]);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        // Проверка корректности индекса
        if (floorIndex < 0 || floorIndex >= count - 1) {
            throw new IllegalArgumentException("Invalid floor index: " + floorIndex);
        }

        // Интерполяция между floorIndex и floorIndex + 1
        return interpolate(x, xValues[floorIndex], xValues[floorIndex + 1],
                yValues[floorIndex], yValues[floorIndex + 1]);
    }

    // Реализация методов интерфейса TabulatedFunction

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public double getX(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }
        return xValues[index];
    }

    @Override
    public double getY(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }
        return yValues[index];
    }

    @Override
    public void setY(int index, double value) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }
        yValues[index] = value;
    }

    @Override
    public int indexOfX(double x) {
        for (int i = 0; i < count; i++) {
            if (Math.abs(xValues[i] - x) < 1e-12) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        for (int i = 0; i < count; i++) {
            if (Math.abs(yValues[i] - y) < 1e-12) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public double leftBound() {
        return xValues[0];
    }

    @Override
    public double rightBound() {
        return xValues[count - 1];
    }
}