package com.example.LAB5.functions;

@SimpleFunction(name = "function.sqr", priority = 20)
public class SqrFunction implements MathFunction {
    public double apply(double x) {
        return Math.pow(x, 2);
    }
}