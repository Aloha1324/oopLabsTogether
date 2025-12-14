package com.example.LAB5.operations;

import com.example.LAB5.concurrent.SynchronizedTabulatedFunction;
import com.example.LAB5.functions.TabulatedFunction;
import com.example.LAB5.functions.factory.TabulatedFunctionFactory;
import com.example.LAB5.functions.factory.ArrayTabulatedFunctionFactory;

public class TabulatedDifferentialOperator implements DifferentialOperator<TabulatedFunction> {

    private TabulatedFunctionFactory factory;

    public TabulatedDifferentialOperator() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    public TabulatedDifferentialOperator(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    @Override
    public TabulatedFunction derive(TabulatedFunction function) {
        int pointCount = function.getCount();

        double[] xValues = new double[pointCount];
        double[] yValues = new double[pointCount];

        for (int i = 0; i < pointCount; i++) {
            xValues[i] = function.getX(i);
        }

        for (int i = 0; i < pointCount - 1; i++) {
            double x1 = function.getX(i);
            double x2 = function.getX(i + 1);
            double y1 = function.getY(i);
            double y2 = function.getY(i + 1);

            yValues[i] = (y2 - y1) / (x2 - x1);
        }

        if (pointCount > 1) {
            yValues[pointCount - 1] = yValues[pointCount - 2];
        } else {
            yValues[0] = 0.0;
        }

        return factory.create(xValues, yValues);
    }

    public TabulatedFunction deriveUsingDirectAccess(TabulatedFunction function) {
        int pointCount = function.getCount();
        double[] xValues = new double[pointCount];
        double[] yValues = new double[pointCount];

        for (int i = 0; i < pointCount; i++) {
            xValues[i] = function.getX(i);
        }

        for (int i = 0; i < pointCount - 1; i++) {
            double deltaX = function.getX(i + 1) - function.getX(i);
            double deltaY = function.getY(i + 1) - function.getY(i);
            yValues[i] = deltaY / deltaX;
        }

        if (pointCount > 1) {
            yValues[pointCount - 1] = yValues[pointCount - 2];
        } else {
            yValues[0] = 0.0;
        }

        return factory.create(xValues, yValues);
    }

    public TabulatedFunction deriveSynchronously(TabulatedFunction function) {
        SynchronizedTabulatedFunction syncFunc;

        if (function instanceof SynchronizedTabulatedFunction) {
            syncFunc = (SynchronizedTabulatedFunction) function;
        } else {
            syncFunc = new SynchronizedTabulatedFunction(function);
        }

        return syncFunc.doSynchronously(func -> this.derive(func));
    }
}