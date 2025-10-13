package operations;

import functions.MathFunction;

public class RightSteppingDifferentialOperator extends SteppingDifferentialOperator {

    public RightSteppingDifferentialOperator(double step) {
        super(step);
    }

    @Override
    public MathFunction derive(MathFunction func) {
        return new MathFunction() {
            @Override
            public double apply(double x) {
                return (func.apply(x + step) - func.apply(x)) / step;
            }
        };
    }
}
