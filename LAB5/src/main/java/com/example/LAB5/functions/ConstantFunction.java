package com.example.LAB5.functions;

public class ConstantFunction implements MathFunction {
    private final double value;

    public ConstantFunction(double value) {
        this.value = value;
    }

    public double apply(double x) {return value;}
}

