package com.example.LAB5.functions;

@SimpleFunction(name = "function.identity", priority = 5)
public class IdentityFunction implements MathFunction {

    public double apply(double x) {
        return x;
    }
}