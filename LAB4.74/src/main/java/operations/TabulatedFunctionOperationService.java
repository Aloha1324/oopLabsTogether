package operations;

import exceptions.InconsistentFunctionsException;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.Point;
import functions.TabulatedFunction;
import functions.factory.TabulatedFunctionFactory;

public class TabulatedFunctionOperationService {

    private TabulatedFunctionFactory factory;

    public TabulatedFunctionOperationService() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    public TabulatedFunctionOperationService(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    public static Point[] asPoints(TabulatedFunction tabulatedFunction) {
        int size = 0;
        for (Point ignored : tabulatedFunction) {
            size++;
        }
        Point[] points = new Point[size];
        int i = 0;
        for (Point point : tabulatedFunction) {
            points[i++] = point;
        }
        return points;
    }

    private interface BiOperation {
        double apply(double u, double v);
    }

    private TabulatedFunction doOperation(TabulatedFunction a, TabulatedFunction b, BiOperation operation) {
        if (a.getCount() != b.getCount()) {
            throw new InconsistentFunctionsException("Functions have different number of points");
        }

        Point[] aPoints = asPoints(a);
        Point[] bPoints = asPoints(b);

        int n = aPoints.length;

        double[] xValues = new double[n];
        double[] yValues = new double[n];

        for (int i = 0; i < n; i++) {
            if (Double.compare(aPoints[i].x, bPoints[i].x) != 0) {
                throw new InconsistentFunctionsException("X values differ at index " + i);
            }
            xValues[i] = aPoints[i].x;
            yValues[i] = operation.apply(aPoints[i].y, bPoints[i].y);
        }

        return factory.create(xValues, yValues);
    }

    public TabulatedFunction add(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> u + v);
    }

    public TabulatedFunction subtract(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> u - v);
    }

    /**
     * Умножает две табулированные функции покоординатно
     * a первая функция
     *  b вторая функция
     * новая функция, где y[i] = a.y[i] * b.y[i]
     * InconsistentFunctionsException если функции имеют разное количество точек или разные x-значения
     */
    public TabulatedFunction multiply(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> u * v);
    }

    /**
     * Делит две табулированные функции покоординатно
     * a функция-числитель
     *  b функция-знаменатель
     *  новая функция, где y[i] = a.y[i] / b.y[i]
     * InconsistentFunctionsException если функции имеют разное количество точек или разные x-значения
     * ArithmeticException если b.y[i] = 0 для любого i
     */
    public TabulatedFunction divide(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> {
            if (Math.abs(v) < 1e-12) {
                throw new ArithmeticException("Division by zero at point with y = " + v);
            }
            return u / v;
        });
    }
}
