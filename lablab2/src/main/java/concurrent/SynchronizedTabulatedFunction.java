package concurrent;

import functions.Point;
import functions.TabulatedFunction;
import operations.TabulatedFunctionOperationService;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SynchronizedTabulatedFunction implements TabulatedFunction {
    private final TabulatedFunction function;

    public SynchronizedTabulatedFunction(TabulatedFunction function) {
        this.function = function;
    }

    public interface Operation<T> {
        T apply(SynchronizedTabulatedFunction func);
    }

    public synchronized <T> T doSynchronously(Operation<? extends T> operation) {
        return operation.apply(this);
    }

    @Override
    public synchronized int getCount() {
        return function.getCount();
    }

    @Override
    public synchronized double getX(int index) {
        return function.getX(index);
    }

    @Override
    public synchronized double getY(int index) {
        return function.getY(index);
    }

    @Override
    public synchronized void setY(int index, double value) {
        function.setY(index, value);
    }

    @Override
    public synchronized int indexOfX(double x) {
        return function.indexOfX(x);
    }

    @Override
    public synchronized int indexOfY(double y) {
        return function.indexOfY(y);
    }

    @Override
    public synchronized double leftBound() {
        return function.leftBound();
    }

    @Override
    public synchronized double rightBound() {
        return function.rightBound();
    }

    @Override
    public synchronized double apply(double x) {
        return function.apply(x);
    }

    @Override
    public Iterator<Point> iterator() {
        synchronized (function) {
            Point[] copyPoints = TabulatedFunctionOperationService.asPoints(function);

            return new Iterator<Point>() {
                private int currentIndex = 0;
                private final Point[] points = copyPoints;

                @Override
                public boolean hasNext() {
                    return currentIndex < points.length;
                }

                @Override
                public Point next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException("No more elements in iterator");
                    }
                    return points[currentIndex++];
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Remove operation is not supported");
                }
            };
        }
    }
}