package com.example.LAB5.concurrent;

import com.example.LAB5.functions.Point;
import com.example.LAB5.functions.TabulatedFunction;
import com.example.LAB5.operations.TabulatedFunctionOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SynchronizedTabulatedFunction implements TabulatedFunction {
    private static final Logger logger = LoggerFactory.getLogger(SynchronizedTabulatedFunction.class);
    private final TabulatedFunction function;

    public SynchronizedTabulatedFunction(TabulatedFunction function) {
        this.function = function;
        logger.debug("Создан SynchronizedTabulatedFunction для функции: {}", function.getClass().getSimpleName());
    }

    public interface Operation<T> {
        T apply(SynchronizedTabulatedFunction func);
    }

    public synchronized <T> T doSynchronously(Operation<? extends T> operation) {
        logger.trace("Выполнение синхронной операции");
        return operation.apply(this);
    }

    @Override
    public synchronized int getCount() {
        logger.trace("Получение количества точек: {}", function.getCount());
        return function.getCount();
    }

    @Override
    public synchronized double getX(int index) {
        double x = function.getX(index);
        logger.trace("Получение X[{}] = {}", index, x);
        return x;
    }

    @Override
    public synchronized double getY(int index) {
        double y = function.getY(index);
        logger.trace("Получение Y[{}] = {}", index, y);
        return y;
    }

    @Override
    public synchronized void setY(int index, double value) {
        double oldValue = function.getY(index);
        function.setY(index, value);
        logger.debug("Изменение Y[{}] с {} на {}", index, oldValue, value);
    }

    @Override
    public synchronized int indexOfX(double x) {
        int index = function.indexOfX(x);
        logger.trace("Поиск индекса X={}, результат: {}", x, index);
        return index;
    }

    @Override
    public synchronized int indexOfY(double y) {
        int index = function.indexOfY(y);
        logger.trace("Поиск индекса Y={}, результат: {}", y, index);
        return index;
    }

    @Override
    public synchronized double leftBound() {
        double bound = function.leftBound();
        logger.trace("Левая граница: {}", bound);
        return bound;
    }

    @Override
    public synchronized double rightBound() {
        double bound = function.rightBound();
        logger.trace("Правая граница: {}", bound);
        return bound;
    }

    @Override
    public synchronized double apply(double x) {
        double result = function.apply(x);
        logger.trace("Применение функции к X={}, результат: {}", x, result);
        return result;
    }

    @Override
    public Iterator<Point> iterator() {
        logger.trace("Создание итератора для синхронизированной функции");

        // Создаём копию точек без вызова несуществующего метода
        synchronized (function) {
            int n = function.getCount();
            Point[] copyPoints = new Point[n];
            for (int i = 0; i < n; i++) {
                copyPoints[i] = new Point(function.getX(i), function.getY(i));
            }
            logger.debug("Создана копия {} точек для итератора", copyPoints.length);

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