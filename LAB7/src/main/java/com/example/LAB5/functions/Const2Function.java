package com.example.LAB5.functions;

@SimpleFunction(name = "function.const_2", priority = 40)
public class Const2Function implements MathFunction {
    public double apply(double x) {
        return 2.0;
    }
}