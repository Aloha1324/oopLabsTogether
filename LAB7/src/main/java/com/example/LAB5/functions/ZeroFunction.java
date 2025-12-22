package com.example.LAB5.functions;

@SimpleFunction(name = "function.zero", priority = 35)
public class ZeroFunction implements MathFunction {
    public double apply(double x) {
        return 0.0;
    }
}