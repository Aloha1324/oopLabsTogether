package com.example.LAB5.functions;

@SimpleFunction(name = "function.unit", priority = 30)
public class UnitFunction implements MathFunction {
    public double apply(double x) {
        return 1.0;
    }
}
