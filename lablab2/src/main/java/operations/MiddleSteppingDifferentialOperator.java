package operations;

import functions.MathFunction;

public class MiddleSteppingDifferentialOperator extends SteppingDifferentialOperator {

    public MiddleSteppingDifferentialOperator(double step) {
        super(step);
    }

    @Override
    public MathFunction derive(MathFunction func) {
        return new MathFunction() {
            @Override
            public double apply(double x) {
                return (func.apply(x + step) - func.apply(x - step)) / (2 * step);
            }
        };
    }
}
