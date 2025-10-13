package functions;

import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;
import exceptions.InterpolationException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Класс табулированной функции, реализованной на основе массивов.
 */
public class ArrayTabulatedFunction extends AbstractTabulatedFunction
        implements Iterable<Point>, Cloneable, Serializable {

    private static final long serialVersionUID = 8305720685834923448L;

    private double[] xValues;
    private double[] yValues;

    // Конструктор на основе массивов
    public ArrayTabulatedFunction(double[] xValues, double[] yValues) {
        if (xValues == null || yValues == null)
            throw new NullPointerException("Arrays must not be null");
        if (xValues.length < 2)
            throw new IllegalArgumentException("At least 2 points are required");
        checkLengthIsTheSame(xValues, yValues);
        checkSorted(xValues);
        this.xValues = Arrays.copyOf(xValues, xValues.length);
        this.yValues = Arrays.copyOf(yValues, yValues.length);
    }

    // Конструктор на основе функции, диапазона, количества точек
    public ArrayTabulatedFunction(MathFunction func, double xFrom, double xTo, int count) {
        if (func == null)
            throw new NullPointerException("Function must not be null");
        if (count < 2)
            throw new IllegalArgumentException("At least 2 points are required");
        if (xFrom >= xTo)
            throw new IllegalArgumentException("xFrom must be less than xTo");

        this.xValues = new double[count];
        this.yValues = new double[count];
        double step = (xTo - xFrom) / (count - 1);

        for (int i = 0; i < count; i++) {
            this.xValues[i] = xFrom + i * step;
            this.yValues[i] = func.apply(this.xValues[i]);
        }
    }

    // Вставка точки (или обновление, если x уже есть)
    public void insert(double x, double y) {
        int index = indexOfX(x);
        if (index != -1) {
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
        }
    }

    @Override
    public int getCount() {
        return xValues.length;
    }

    @Override
    public double getX(int index) {
        if (index < 0 || index >= getCount())
            throw new IllegalArgumentException("Index out of bounds: " + index);
        return xValues[index];
    }

    @Override
    public double getY(int index) {
        if (index < 0 || index >= getCount())
            throw new IllegalArgumentException("Index out of bounds: " + index);
        return yValues[index];
    }

    @Override
    public void setY(int index, double value) {
        if (index < 0 || index >= getCount())
            throw new IllegalArgumentException("Index out of bounds: " + index);
        yValues[index] = value;
    }

    @Override
    public int indexOfX(double x) {
        for (int i = 0; i < getCount(); i++) {
            if (Double.compare(xValues[i], x) == 0) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        for (int i = 0; i < getCount(); i++) {
            if (Double.compare(yValues[i], y) == 0) {
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
        return xValues[getCount() - 1];
    }

    @Override
    protected int floorIndexOfX(double x) {
        int count = getCount();
        if (x < xValues[0]) {
            return 0;
        }
        if (x > xValues[count - 1]) {
            return count - 2;
        }
        int left = 0, right = count - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (xValues[mid] == x) return mid;
            else if (xValues[mid] < x) left = mid + 1;
            else right = mid - 1;
        }
        return left - 1;
    }

    @Override
    protected double extrapolateLeft(double x) {
        return interpolate(x, xValues[0], xValues[1], yValues[0], yValues[1]);
    }

    @Override
    protected double extrapolateRight(double x) {
        int count = getCount();
        return interpolate(x, xValues[count - 2], xValues[count - 1], yValues[count - 2], yValues[count - 1]);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        if (floorIndex < 0 || floorIndex >= getCount() - 1)
            throw new IllegalArgumentException("Invalid floor index: " + floorIndex);
        double leftX = xValues[floorIndex], rightX = xValues[floorIndex + 1];
        if (x < leftX || x > rightX)
            throw new InterpolationException("x is outside the interpolation interval [" + leftX + ", " + rightX + "]");
        return interpolate(x, leftX, rightX, yValues[floorIndex], yValues[floorIndex + 1]);
    }

    protected double interpolate(double x, double leftX, double rightX, double leftY, double rightY) {
        if (leftX == rightX) return (leftY + rightY) / 2.0;
        return leftY + ((x - leftX) * (rightY - leftY)) / (rightX - leftX);
    }

    @Override
    public double apply(double x) {
        if (x < xValues[0] || x > xValues[getCount() - 1]) {
            throw new UnsupportedOperationException("x is out of bounds");
        }

        // Линейная интерполяция
        for (int i = 0; i < getCount() - 1; i++) {
            if (x >= xValues[i] && x <= xValues[i + 1]) {
                return interpolate(x, xValues[i], xValues[i + 1], yValues[i], yValues[i + 1]);
            }
        }

        return yValues[getCount() - 1];
    }

    @Override
    public ArrayTabulatedFunction clone() {
        try {
            ArrayTabulatedFunction cloned = (ArrayTabulatedFunction) super.clone();
            cloned.xValues = Arrays.copyOf(this.xValues, this.getCount());
            cloned.yValues = Arrays.copyOf(this.yValues, this.getCount());
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone should be supported");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ArrayTabulatedFunction that = (ArrayTabulatedFunction) obj;
        return super.equals(that);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public Iterator<Point> iterator() {
        return new Iterator<Point>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < getCount();
            }

            @Override
            public Point next() {
                if (!hasNext())
                    throw new java.util.NoSuchElementException();
                Point point = new Point(xValues[i], yValues[i]);
                i++;
                return point;
            }
        };
    }
}