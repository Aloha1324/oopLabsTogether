package com.example.LAB5.operations;

import com.example.LAB5.functions.MathFunction;

public class LeftSteppingDifferentialOperator extends SteppingDifferentialOperator {

    public LeftSteppingDifferentialOperator(double step) {
        super(step);
    }

    @Override
    public MathFunction derive(MathFunction func) {
        return new MathFunction() {
            @Override
            public double apply(double x) {
                return (func.apply(x) - func.apply(x - step)) / step;
            }
        };
    }
}
