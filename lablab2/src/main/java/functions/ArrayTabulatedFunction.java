package functions;

import exceptions.InterpolationException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

public class ArrayTabulatedFunction extends AbstractTabulatedFunction
        implements Insertable, Removable, Iterable<Point>, Cloneable, Serializable {

    private static final long serialVersionUID = 8305720685834923448L;

    private double[] xValues;
    private double[] yValues;

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

    public void remove(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }

        if (getCount() <= 2) {
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
    }

    public int getCount() {
        return xValues.length;
    }

    public double getX(int index) {
        if (index < 0 || index >= getCount())
            throw new IllegalArgumentException("Index out of bounds: " + index);
        return xValues[index];
    }

    public double getY(int index) {
        if (index < 0 || index >= getCount())
            throw new IllegalArgumentException("Index out of bounds: " + index);
        return yValues[index];
    }

    public void setY(int index, double value) {
        if (index < 0 || index >= getCount())
            throw new IllegalArgumentException("Index out of bounds: " + index);
        yValues[index] = value;
    }

    public int indexOfX(double x) {
        for (int i = 0; i < getCount(); i++) {
            if (Double.compare(xValues[i], x) == 0) {
                return i;
            }
        }
        return -1;
    }

    public int indexOfY(double y) {
        for (int i = 0; i < getCount(); i++) {
            if (Double.compare(yValues[i], y) == 0) {
                return i;
            }
        }
        return -1;
    }

    public double leftBound() {
        return xValues[0];
    }

    public double rightBound() {
        return xValues[getCount() - 1];
    }

    protected int floorIndexOfX(double x) {
        int count = getCount();
        if (x < xValues[0]) {
            return 0;
        }
        if (x >= xValues[count - 1]) {
            return count - 2;
        }

        for (int i = 0; i < count - 1; i++) {
            if (x >= xValues[i] && x < xValues[i + 1]) {
                return i;
            }
        }
        return count - 2;
    }

    protected double extrapolateLeft(double x) {
        return interpolate(x, xValues[0], xValues[1], yValues[0], yValues[1]);
    }

    protected double extrapolateRight(double x) {
        int count = getCount();
        return interpolate(x, xValues[count - 2], xValues[count - 1], yValues[count - 2], yValues[count - 1]);
    }

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

    public double apply(double x) {
        if (x < xValues[0]) {
            return extrapolateLeft(x);
        }
        if (x > xValues[getCount() - 1]) {
            return extrapolateRight(x);
        }

        int exactIndex = indexOfX(x);
        if (exactIndex != -1) {
            return yValues[exactIndex];
        }

        int floorIndex = floorIndexOfX(x);
        return interpolate(x, floorIndex);
    }

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

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ArrayTabulatedFunction that = (ArrayTabulatedFunction) obj;
        return super.equals(that);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        return super.toString();
    }

    public Iterator<Point> iterator() {
        return new Iterator<Point>() {
            private int i = 0;

            public boolean hasNext() {
                return i < getCount();
            }

            public Point next() {
                if (!hasNext())
                    throw new java.util.NoSuchElementException();
                Point point = new Point(xValues[i], yValues[i]);
                i++;
                return point;
            }

            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }
}