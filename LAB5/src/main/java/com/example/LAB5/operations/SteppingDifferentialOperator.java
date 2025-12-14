package com.example.LAB5.operations;

import com.example.LAB5.functions.MathFunction;

public abstract class SteppingDifferentialOperator implements DifferentialOperator<MathFunction> {

    protected double step;

    public SteppingDifferentialOperator(double step) {
        if (step <= 0 || Double.isNaN(step) || Double.isInfinite(step))
            throw new IllegalArgumentException("Step must be positive finite number");
        this.step = step;
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        if (step <= 0 || Double.isNaN(step) || Double.isInfinite(step))
            throw new IllegalArgumentException("Step must be positive finite number");
        this.step = step;
    }
}
